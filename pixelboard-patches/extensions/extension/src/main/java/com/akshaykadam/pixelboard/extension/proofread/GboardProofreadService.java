/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputConnection;

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarSwipeRuntime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Service for proofreading and correcting text via Yandex Speller API and grammar heuristics.
 *
 * Provides Git-style diff highlighting (red for mistakes, green for corrections)
 * and seamless one-tap text replacement via InputConnection.
 */
public final class GboardProofreadService {
    public static final String TAG = "PixelBoardProofread";
    private static final String YANDEX_SPELLER_URL =
            "https://speller.yandex.net/services/spellservice.json/checkText";

    private static Handler sMainHandler;
    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private static void logw(String tag, String msg) {
        try {
            Log.w(tag, msg);
        } catch (Throwable ignored) {
        }
    }

    private static void logw(String tag, String msg, Throwable t) {
        try {
            Log.w(tag, msg, t);
        } catch (Throwable ignored) {
        }
    }

    private static void logi(String tag, String msg) {
        try {
            Log.i(tag, msg);
        } catch (Throwable ignored) {
        }
    }

    private static synchronized Handler getMainHandler() {
        if (sMainHandler == null) {
            sMainHandler = new Handler(Looper.getMainLooper());
        }
        return sMainHandler;
    }

    public enum DiffType {
        UNCHANGED,
        DELETED,
        INSERTED
    }

    public static final class DiffToken {
        public final String text;
        public final DiffType type;

        public DiffToken(String text, DiffType type) {
            this.text = text;
            this.type = type;
        }
    }

    public static final class SpellError {
        public final int pos;
        public final int len;
        public final String word;
        public final List<String> suggestions;

        public SpellError(int pos, int len, String word, List<String> suggestions) {
            this.pos = pos;
            this.len = len;
            this.word = word;
            this.suggestions = suggestions;
        }
    }

    public static final class ProofreadResult {
        public final String originalText;
        public final String correctedText;
        public final List<DiffToken> diffTokens;
        public final boolean wasSelected;
        public final int beforeLength;
        public final int afterLength;
        public final boolean hasChanges;
        public final String errorMessage;

        public ProofreadResult(String originalText, String correctedText, List<DiffToken> diffTokens,
                               boolean wasSelected, int beforeLength, int afterLength,
                               boolean hasChanges, String errorMessage) {
            this.originalText = originalText;
            this.correctedText = correctedText;
            this.diffTokens = diffTokens;
            this.wasSelected = wasSelected;
            this.beforeLength = beforeLength;
            this.afterLength = afterLength;
            this.hasChanges = hasChanges;
            this.errorMessage = errorMessage;
        }
    }

    public interface Callback {
        void onResult(ProofreadResult result);
    }

    private GboardProofreadService() {
    }

    public static volatile boolean sBypassJarvisIntercept = false;

    /**
     * Intercepts native Gboard Jarvis (Writing Tools) button clicks to provide
     * top-quality Russian grammar, syntax heuristics, and spell-checking.
     */
    public static boolean onJarvisClicked(Object caller) {
        if (sBypassJarvisIntercept) {
            return false;
        }
        try {
            View targetView = GboardProofreadButton.getAttachedParent();
            if (targetView == null && caller != null) {
                try {
                    Object obr = caller.getClass().getMethod("ah").invoke(caller);
                    if (obr != null) {
                        Object view = obr.getClass().getMethod("l").invoke(obr);
                        if (view instanceof View) {
                            targetView = (View) view;
                        }
                        if (targetView == null) {
                            view = obr.getClass().getMethod("k").invoke(obr);
                            if (view instanceof View) {
                                targetView = (View) view;
                            }
                        }
                    }
                } catch (Throwable ignored) {
                }
            }

            if (targetView == null) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null && ims.getWindow() != null) {
                    View decor = ims.getWindow().getWindow() != null
                            ? ims.getWindow().getWindow().getDecorView()
                            : null;
                    if (decor != null) {
                        targetView = decor;
                    }
                }
            }

            if (targetView != null) {
                Context context = targetView.getContext();
                String backend = com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.readBackendType(
                        com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.preferences(context));

                // If backend is Yandex, intercept Jarvis to run Yandex Speller + Git Diff:
                if (com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_YANDEX.equals(backend)) {
                    final View finalView = targetView;
                    getMainHandler().post(() -> checkAndShowDiff(finalView));
                    return true;
                }

                // If user selected AICore, Astrea, or Google Cloud Server, allow native Gboard Writing Tools to handle it!
                if (com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_AICORE.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_ASTREA.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_GBOARD_SERVER.equals(backend)) {
                    return false;
                }

                if (GboardProofreadSettings.isEnabled(context)) {
                    final View finalView = targetView;
                    getMainHandler().post(() -> checkAndShowDiff(finalView));
                    return true;
                }
            }
        } catch (Throwable t) {
            logw(TAG, "Failed in onJarvisClicked", t);
        }
        return false;
    }



    /**
     * Checks text around cursor or selected text, and displays the Git-style Diff card.
     */
    public static void checkAndShowDiff(View keyboardView) {
        if (keyboardView == null) return;
        Context context = keyboardView.getContext();
        if (!GboardProofreadSettings.isEnabled(context)) {
            return;
        }

        InputMethodService ims = GboardSpacebarSwipeRuntime.findInputMethodService(keyboardView);
        if (ims == null) {
            ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
        }
        if (ims == null) {
            logw(TAG, "Cannot find InputMethodService for proofread");
            return;
        }

        final InputMethodService finalIms = ims;

        InputConnection ic = ims.getCurrentInputConnection();
        if (ic == null) {
            logw(TAG, "Cannot find InputConnection for proofread");
            return;
        }

        // 1. Capture text
        CharSequence selected = ic.getSelectedText(0);
        final boolean wasSelected;
        final String textToCheck;
        final int beforeLen;
        final int afterLen;

        if (selected != null && selected.length() > 0) {
            wasSelected = true;
            textToCheck = selected.toString();
            beforeLen = 0;
            afterLen = 0;
        } else {
            wasSelected = false;
            CharSequence before = ic.getTextBeforeCursor(2000, 0);
            CharSequence after = ic.getTextAfterCursor(1000, 0);
            String beforeStr = before != null ? before.toString() : "";
            String afterStr = after != null ? after.toString() : "";
            textToCheck = beforeStr + afterStr;
            beforeLen = beforeStr.length();
            afterLen = afterStr.length();
        }

        if (textToCheck.trim().isEmpty()) {
            GboardProofreadOverlay.showEmpty(keyboardView, "Нет текста для проверки");
            return;
        }

        // 2. Show loading card
        GboardProofreadOverlay.showLoading(keyboardView);

        // 3. Run check in background
        sExecutor.execute(() -> {
            ProofreadResult result = checkTextInternal(textToCheck, wasSelected, beforeLen, afterLen);
            getMainHandler().post(() -> {
                GboardProofreadOverlay.showResult(keyboardView, finalIms, result);
            });
        });
    }

    /**
     * Internal text checking logic: calls Yandex Speller API and computes word-level diff.
     */
    public static ProofreadResult checkTextInternal(String text, boolean wasSelected, int beforeLen, int afterLen) {
        if (text == null || text.trim().isEmpty()) {
            return new ProofreadResult(text, text, Collections.emptyList(), wasSelected, beforeLen, afterLen, false, null);
        }

        List<SpellError> errors = null;
        String errorMessage = null;

        try {
            errors = callYandexSpellerApi(text);
        } catch (Throwable t) {
            logw(TAG, "Yandex Speller API call failed", t);
            errorMessage = "Не удалось связаться с сервером проверки";
        }

        if (errors == null) {
            errors = Collections.emptyList();
        }

        // Apply corrections to construct candidate corrected text
        String spellCorrected = applySpellErrors(text, errors);

        // Apply punctuation and casing heuristics
        String finalCorrected = applyGrammarHeuristics(spellCorrected);

        boolean hasChanges = !text.equals(finalCorrected);
        List<DiffToken> diffTokens = computeDiffTokens(text, finalCorrected);

        return new ProofreadResult(text, finalCorrected, diffTokens, wasSelected, beforeLen, afterLen, hasChanges, errorMessage);
    }

    /**
     * Executes HTTPS POST request to Yandex Speller API.
     */
    public static List<SpellError> callYandexSpellerApi(String text) throws Exception {
        URL url = new URL(YANDEX_SPELLER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(6000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("User-Agent", "PixelBoard/18.4 (Android)");

        String postData = "text=" + URLEncoder.encode(text, "UTF-8")
                + "&lang=ru,en&options=14";

        byte[] postBytes = postData.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(postBytes.length);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(postBytes);
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP response error: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (InputStream is = conn.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            conn.disconnect();
        }

        return parseSpellerJson(response.toString());
    }

    /**
     * Parses Yandex Speller JSON response into a list of SpellError objects.
     */
    public static List<SpellError> parseSpellerJson(String jsonStr) {
        List<SpellError> list = new ArrayList<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return list;
        }

        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int pos = obj.optInt("pos", -1);
                int len = obj.optInt("len", 0);
                String word = obj.optString("word", "");
                JSONArray sArray = obj.optJSONArray("s");
                List<String> suggestions = new ArrayList<>();
                if (sArray != null) {
                    for (int j = 0; j < sArray.length(); j++) {
                        suggestions.add(sArray.getString(j));
                    }
                }
                if (pos >= 0 && len > 0 && !suggestions.isEmpty()) {
                    list.add(new SpellError(pos, len, word, suggestions));
                }
            }
        } catch (Throwable t) {
            logw(TAG, "Failed to parse speller json", t);
        }

        // Sort errors in ascending order of pos
        Collections.sort(list, Comparator.comparingInt(a -> a.pos));
        return list;
    }

    /**
     * Replaces misspelled words with their top suggestion while matching the original casing.
     */
    private static String applySpellErrors(String text, List<SpellError> errors) {
        if (errors == null || errors.isEmpty() || text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        int lastIdx = 0;

        for (SpellError err : errors) {
            if (err.pos < lastIdx || err.pos + err.len > text.length()) {
                continue;
            }
            // Append unchanged part before error
            sb.append(text.substring(lastIdx, err.pos));

            // Choose best suggestion and preserve original casing
            String best = err.suggestions.get(0);
            String cased = matchCasing(err.word, best);
            sb.append(cased);

            lastIdx = err.pos + err.len;
        }

        if (lastIdx < text.length()) {
            sb.append(text.substring(lastIdx));
        }

        return sb.toString();
    }

    /**
     * Matches the capitalization style of the source word onto the target word.
     */
    public static String matchCasing(String source, String target) {
        if (source == null || source.isEmpty() || target == null || target.isEmpty()) {
            return target;
        }
        if (isAllUpperCase(source)) {
            return target.toUpperCase();
        }
        if (Character.isUpperCase(source.charAt(0))) {
            return Character.toUpperCase(target.charAt(0)) + (target.length() > 1 ? target.substring(1) : "");
        }
        return target;
    }

    private static boolean isAllUpperCase(String s) {
        int letters = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                letters++;
                if (!Character.isUpperCase(c)) return false;
            }
        }
        return letters > 1;
    }

    /**
     * Comprehensive Russian syntax, grammar, and punctuation engine.
     * Places commas before conjunctions, formats introductory words, hyphenates particles,
     * places question marks on questions, and fixes capitalization/spacing.
     */
    public static String applyGrammarHeuristics(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        // Preserve leading and trailing whitespace
        int startSpaces = 0;
        while (startSpaces < text.length() && Character.isWhitespace(text.charAt(startSpaces))) {
            startSpaces++;
        }
        int endSpaces = 0;
        while (endSpaces < text.length() && Character.isWhitespace(text.charAt(text.length() - 1 - endSpaces))) {
            endSpaces++;
        }

        String res = text.trim();

        // 1. Hyphenated prepositions, adverbs, and particles
        res = res.replaceAll("(?i)\\b(из)\\s+(за)\\b", "$1-$2");
        res = res.replaceAll("(?i)\\b(из)\\s+(под)\\b", "$1-$2");
        res = res.replaceAll("(?i)\\b(вс[её])\\s+(таки)\\b", "всё-таки");
        res = res.replaceAll("(?i)\\b(так)\\s+(таки)\\b", "так-таки");
        res = res.replaceAll("(?i)\\b(точь)\\s+в\\s+(точь)\\b", "точь-в-точь");
        res = res.replaceAll("(?i)\\b(чуть)\\s+(чуть)\\b", "чуть-чуть");
        res = res.replaceAll("(?i)\\b(едва)\\s+(едва)\\b", "едва-едва");
        res = res.replaceAll("(?i)\\b(кое)\\s+(как|кто|что|где|куда)\\b", "кое-$2");
        res = res.replaceAll("(?i)\\b(во|в)\\s+(первых|вторых|третьих)\\b", "$1-$2");
        res = res.replaceAll("(?i)\\b(по)\\s+(русски|английски|немецки|моему|твоему|нашему|прежнему|видимому)\\b", "$1-$2");

        // Particles -то, -нибудь, -либо
        res = res.replaceAll("(?i)\\b(что|кто|где|куда|когда|почему|зачем|как|какой|какая|какое|какие|каком|каких|какую|каким|чей|чья|чь[её]|чьи)\\s+(то)\\b", "$1-$2");
        res = res.replaceAll("(?i)\\b(что|кто|где|куда|когда|как|какой|какая|какое|какие|каком|каких|какую|чей)\\s+(нибудь)\\b", "$1-$2");
        res = res.replaceAll("(?i)\\b(что|кто|где|куда|когда|как|какой)\\s+(либо)\\b", "$1-$2");

        // 2. Compound conjunctions (must be executed before single conjunctions)
        String[] compoundConjunctions = new String[] {
                "потому что", "так как", "так что", "для того чтобы", "из-за того что",
                "несмотря на то что", "в то время как", "с тех пор как", "до тех пор пока",
                "как будто", "как только", "то есть", "а также", "вместо того чтобы",
                "перед тем как", "после того как", "в связи с тем что", "благодаря тому что"
        };
        for (String conj : compoundConjunctions) {
            res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(conj) + ")\\b", "$1, $2");
        }

        // 3. Opposing & coordinating conjunctions
        res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(но|однако|зато|да и)\\b", "$1, $2");
        res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(а)\\b", "$1, $2");

        // 4. Subordinate conjunctions & relative words
        String[] subordinateConjunctions = new String[] {
                "чтобы", "чтоб", "если", "когда", "хотя", "пока", "будто", "словно", "едва",
                "который", "которая", "которое", "которые", "которого", "которой", "которому",
                "которым", "которую", "которых", "которыми", "где", "куда", "откуда"
        };
        for (String conj : subordinateConjunctions) {
            res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(conj) + ")\\b", "$1, $2");
        }

        // Subordinate "что"
        res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(что)\\b", "$1, $2");
        // Revert false comma splits around specific idioms:
        res = res.replaceAll("(?i),\\s*потому,\\s*что\\b", ", потому что");
        res = res.replaceAll("(?i)\\bпотому,\\s*что\\b", "потому что");
        res = res.replaceAll("(?i),\\s*только,\\s*что\\b", ", только что");
        res = res.replaceAll("(?i)\\bтолько,\\s*что\\b", "только что");
        res = res.replaceAll("(?i),\\s*разве,\\s*что\\b", ", разве что");
        res = res.replaceAll("(?i)\\bразве,\\s*что\\b", "разве что");
        res = res.replaceAll("(?i),\\s*кое,\\s*что\\b", "кое-что");
        res = res.replaceAll("(?i)\\bкое,\\s*что\\b", "кое-что");

        // 5. Introductory words & phrases
        String[] introWords = new String[] {
                "конечно", "кстати", "например", "к счастью", "к сожалению", "в общем",
                "короче", "между прочим", "разумеется", "безусловно", "естественно",
                "во-первых", "во-вторых", "в-третьих", "впрочем", "пожалуй", "вероятно",
                "следовательно", "таким образом"
        };
        for (String iw : introWords) {
            // At start of sentence
            res = res.replaceAll("(?i)(^|[.!?]\\s+)(" + Pattern.quote(iw) + ")\\b(?!\\s*[,:])", "$1$2,");
            // In middle of sentence
            res = res.replaceAll("(?i)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(iw) + ")\\b(?!\\s*[,:])", "$1, $2,");
        }

        // 6. Typographic spacing and punctuation cleanup
        res = res.replaceAll("[ \\t]{2,}", " ");
        res = res.replaceAll("\\s+([,.:;!?])", "$1");
        res = res.replaceAll(",\\s*,", ",");
        res = res.replaceAll(",\\s*([.!?])", "$1");
        res = res.replaceAll("([,;])([а-яА-ЯёЁa-zA-Z])", "$1 $2");

        // 7. Capitalize first letter of each sentence
        StringBuilder capitalized = new StringBuilder();
        boolean capitalizeNext = true;
        for (int i = 0; i < res.length(); i++) {
            char c = res.charAt(i);
            if (capitalizeNext && Character.isLetter(c)) {
                capitalized.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                capitalized.append(c);
                if (c == '.' || c == '!' || c == '?') {
                    capitalizeNext = true;
                }
            }
        }
        res = capitalized.toString();

        // 8. Add question mark to interrogative sentences if missing
        if (Pattern.compile("^(почему|зачем|как|где|куда|откуда|когда|кто|что|сколько|разве|неужели)\\b(?!-)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(res).find()) {
            if (!Pattern.compile("[.!?]$").matcher(res).find()
                    && !Pattern.compile("(?i)\\b(потому что|из-за того что)\\b").matcher(res).find()) {
                res += "?";
            }
        }

        // Reattach leading and trailing whitespace
        String prefix = text.substring(0, startSpaces);
        String suffix = text.substring(text.length() - endSpaces);
        return prefix + res + suffix;
    }

    /**
     * Computes word-level Git diff tokens between original and corrected text.
     */
    public static List<DiffToken> computeDiffTokens(String original, String corrected) {
        List<DiffToken> tokens = new ArrayList<>();
        if (original == null && corrected == null) return tokens;
        if (original == null) {
            tokens.add(new DiffToken(corrected, DiffType.INSERTED));
            return tokens;
        }
        if (corrected == null) {
            tokens.add(new DiffToken(original, DiffType.DELETED));
            return tokens;
        }
        if (original.equals(corrected)) {
            tokens.add(new DiffToken(original, DiffType.UNCHANGED));
            return tokens;
        }

        // Split into word & punctuation chunks while preserving whitespace
        List<String> origWords = splitIntoTokens(original);
        List<String> corrWords = splitIntoTokens(corrected);

        int[][] lcs = computeLcsMatrix(origWords, corrWords);
        buildDiffFromLcs(origWords, corrWords, lcs, origWords.size(), corrWords.size(), tokens);

        return tokens;
    }

    private static List<String> splitIntoTokens(String s) {
        List<String> list = new ArrayList<>();
        int len = s.length();
        int i = 0;
        while (i < len) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                int start = i;
                while (i < len && Character.isWhitespace(s.charAt(i))) i++;
                list.add(s.substring(start, i));
            } else if (Character.isLetterOrDigit(c)) {
                int start = i;
                while (i < len && Character.isLetterOrDigit(s.charAt(i))) i++;
                list.add(s.substring(start, i));
            } else {
                list.add(String.valueOf(c));
                i++;
            }
        }
        return list;
    }

    private static int[][] computeLcsMatrix(List<String> a, List<String> b) {
        int m = a.size();
        int n = b.size();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (a.get(i).equals(b.get(j))) {
                    dp[i + 1][j + 1] = dp[i][j] + 1;
                } else {
                    dp[i + 1][j + 1] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }
        return dp;
    }

    private static void buildDiffFromLcs(List<String> a, List<String> b, int[][] dp,
                                         int i, int j, List<DiffToken> out) {
        if (i > 0 && j > 0 && a.get(i - 1).equals(b.get(j - 1))) {
            buildDiffFromLcs(a, b, dp, i - 1, j - 1, out);
            out.add(new DiffToken(a.get(i - 1), DiffType.UNCHANGED));
        } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
            buildDiffFromLcs(a, b, dp, i, j - 1, out);
            out.add(new DiffToken(b.get(j - 1), DiffType.INSERTED));
        } else if (i > 0 && (j == 0 || dp[i][j - 1] < dp[i - 1][j])) {
            buildDiffFromLcs(a, b, dp, i - 1, j, out);
            out.add(new DiffToken(a.get(i - 1), DiffType.DELETED));
        }
    }

    /**
     * Applies the corrected text to the active InputConnection.
     */
    public static boolean applyCorrection(InputMethodService ims, ProofreadResult result) {
        if (ims == null || result == null || result.correctedText == null) {
            return false;
        }
        final InputMethodService finalIms = ims;

        InputConnection ic = ims.getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        try {
            ic.beginBatchEdit();
            if (result.wasSelected) {
                ic.commitText(result.correctedText, 1);
            } else {
                ic.deleteSurroundingText(result.beforeLength, result.afterLength);
                ic.commitText(result.correctedText, 1);
            }
            ic.endBatchEdit();
            logi(TAG, "Successfully applied proofread correction: " + result.correctedText);
            return true;
        } catch (Throwable t) {
            logw(TAG, "Failed to apply correction to InputConnection", t);
            return false;
        }
    }
}
