/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
            Log.w(tag, msg);
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

    public static final class TextScope {
        public final String beforeScope;
        public final String afterScope;

        public TextScope(String beforeScope, String afterScope) {
            this.beforeScope = beforeScope != null ? beforeScope : "";
            this.afterScope = afterScope != null ? afterScope : "";
        }

        public String getFullText() {
            return beforeScope + afterScope;
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
        // Smoothly pass through to native Gboard Writing Tools (JarvisKeyboard),
        // which is now fully intercepted by Yandex Speller API + Russian grammar heuristics
        // and displays the true Git Diff card styling directly in the native interface!
        return false;
    }

    /**
     * Resolves Android Context from JarvisExtension (hip) instance.
     */
    private static Context getContextFromHip(Object hipInstance) {
        if (hipInstance == null) return null;
        try {
            Method m = hipInstance.getClass().getMethod("ac");
            Object ctx = m.invoke(hipInstance);
            if (ctx instanceof Context) {
                return (Context) ctx;
            }
        } catch (Throwable ignored) {}
        try {
            Method m = hipInstance.getClass().getMethod("getContext");
            Object ctx = m.invoke(hipInstance);
            if (ctx instanceof Context) {
                return (Context) ctx;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /**
     * Extracts text to proofread from either the trigger info (qrw) or JarvisExtension (hip).
     */
    private static String extractTextToProofread(Object hipInstance, Object qrwInstance) {
        String text = null;
        if (qrwInstance != null) {
            try {
                Method fMethod = qrwInstance.getClass().getMethod("F", qrwInstance.getClass());
                Object s = fMethod.invoke(null, qrwInstance);
                if (s != null) {
                    text = s.toString();
                }
            } catch (Throwable ignored) {}
            if (text == null || text.trim().isEmpty()) {
                try {
                    Field bField = qrwInstance.getClass().getDeclaredField("b");
                    bField.setAccessible(true);
                    Object oxh = bField.get(qrwInstance);
                    if (oxh != null) {
                        text = oxh.toString();
                    }
                } catch (Throwable ignored) {}
            }
        }
        if ((text == null || text.trim().isEmpty()) && hipInstance != null) {
            try {
                Method vMethod = hipInstance.getClass().getMethod("v", boolean.class);
                Object oxh = vMethod.invoke(hipInstance, true);
                if (oxh != null) {
                    text = oxh.toString();
                }
            } catch (Throwable ignored) {}
        }
        if (text == null || text.trim().isEmpty()) {
            InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
            if (ims != null && ims.getCurrentInputConnection() != null) {
                InputConnection ic = ims.getCurrentInputConnection();
                CharSequence before = ic.getTextBeforeCursor(1000, 0);
                CharSequence after = ic.getTextAfterCursor(1000, 0);
                TextScope scope = findActiveSentenceScope(
                        before == null ? "" : before.toString(),
                        after == null ? "" : after.toString());
                text = scope.getFullText();
            }
        }
        return text;
    }

    /**
     * Intercepts Gboard native Writing Tools proofread requests (hip.y).
     * Replaces Google GenAI gRPC call with Yandex Speller API + Russian grammar heuristics.
     */
    public static Object onGetGenAiProofreadResponse(Object hipInstance, Object qrwInstance) {
        try {
            Context context = getContextFromHip(hipInstance);
            if (context == null) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null) context = ims.getApplicationContext();
            }
            if (context != null) {
                String backend = com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.readBackendType(
                        com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.preferences(context));
                if (com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_AICORE.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_ASTREA.equals(backend)) {
                    return null; // Fallback to native Gboard AICore / Astrea inference
                }
            }

            String textToProofread = extractTextToProofread(hipInstance, qrwInstance);
            if (textToProofread == null || textToProofread.trim().isEmpty()) {
                return null;
            }

            ClassLoader cl = hipInstance != null ? hipInstance.getClass().getClassLoader() : GboardProofreadService.class.getClassLoader();
            Class<?> xjaClass = Class.forName("xja", true, cl);
            final Object future = xjaClass.getDeclaredConstructor().newInstance();
            final Method edMethod = xjaClass.getMethod("ed", Object.class);

            final Class<?> omqClass = Class.forName("omq", true, cl);
            final Method zMethod = omqClass.getMethod("z");

            final String text = textToProofread;
            sExecutor.execute(() -> {
                try {
                    ProofreadResult result = checkTextInternal(text, false, 0, 0);
                    String corrected = result.hasChanges ? result.correctedText : text;

                    Object builder = zMethod.invoke(null);
                    Method iMethod = builder.getClass().getMethod("i", String.class);
                    Method aMethod = builder.getClass().getMethod("a");

                    iMethod.invoke(builder, corrected);
                    Object suggestion = aMethod.invoke(builder);

                    List<Object> list = new ArrayList<>();
                    list.add(suggestion);

                    edMethod.invoke(future, list);
                } catch (Throwable t) {
                    logw(TAG, "Worker failed in onGetGenAiProofreadResponse", t);
                    try {
                        edMethod.invoke(future, Collections.emptyList());
                    } catch (Throwable ignored) {}
                }
            });

            return future;
        } catch (Throwable t) {
            logw(TAG, "Error in onGetGenAiProofreadResponse", t);
            return null;
        }
    }

    /**
     * Intercepts Gboard native Writing Tools diff formatting (hip.t).
     * Renders red strikethrough for deleted typos and green bold for added corrections/punctuation.
     */
    public static Spannable formatProofreadDiffSpannable(CharSequence originalSeq, CharSequence correctedSeq, int color) {
        try {
            if (originalSeq == null || correctedSeq == null) {
                return null;
            }
            String original = originalSeq.toString();
            String corrected = correctedSeq.toString();
            if (original.equals(corrected)) {
                return new SpannableString(corrected);
            }
            List<DiffToken> tokens = computeDiffTokens(original, corrected);
            return GboardProofreadOverlay.formatGitDiff(tokens,
                    Color.parseColor("#35FF5252"), Color.parseColor("#FF5252"),
                    Color.parseColor("#354CAF50"), Color.parseColor("#4CAF50"));
        } catch (Throwable t) {
            logw(TAG, "Error in formatProofreadDiffSpannable", t);
            return null;
        }
    }

    /**
     * Intercepts Gboard native Writing Tools rewrite/style requests (hip.z).
     * Generates Russian stylistic variations without touching Google cloud.
     */
    public static Object onGetGenAiRewriteResponse(Object hipInstance, Object ompInstance, Object qrwInstance) {
        try {
            Context context = getContextFromHip(hipInstance);
            if (context == null) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null) context = ims.getApplicationContext();
            }
            if (context != null) {
                String backend = com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.readBackendType(
                        com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.preferences(context));
                if (com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_AICORE.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_ASTREA.equals(backend)) {
                    return null;
                }
            }

            String textToRewrite = extractTextToProofread(hipInstance, qrwInstance);
            if (textToRewrite == null || textToRewrite.trim().isEmpty()) {
                return null;
            }

            ClassLoader cl = hipInstance != null ? hipInstance.getClass().getClassLoader() : GboardProofreadService.class.getClassLoader();
            Class<?> xjaClass = Class.forName("xja", true, cl);
            final Object future = xjaClass.getDeclaredConstructor().newInstance();
            final Method edMethod = xjaClass.getMethod("ed", Object.class);

            final Class<?> omqClass = Class.forName("omq", true, cl);
            final Method zMethod = omqClass.getMethod("z");

            final String text = textToRewrite;
            sExecutor.execute(() -> {
                try {
                    ProofreadResult baseResult = checkTextInternal(text, false, 0, 0);
                    String baseClean = baseResult.hasChanges ? baseResult.correctedText : text;

                    List<String> variants = generateRussianRewriteVariants(baseClean);
                    List<Object> list = new ArrayList<>();
                    for (String v : variants) {
                        if (v == null || v.trim().isEmpty()) continue;
                        Object builder = zMethod.invoke(null);
                        builder.getClass().getMethod("i", String.class).invoke(builder, v);
                        list.add(builder.getClass().getMethod("a").invoke(builder));
                    }

                    if (list.isEmpty()) {
                        Object builder = zMethod.invoke(null);
                        builder.getClass().getMethod("i", String.class).invoke(builder, baseClean);
                        list.add(builder.getClass().getMethod("a").invoke(builder));
                    }

                    edMethod.invoke(future, list);
                } catch (Throwable t) {
                    logw(TAG, "Error in onGetGenAiRewriteResponse worker", t);
                    try {
                        edMethod.invoke(future, Collections.emptyList());
                    } catch (Throwable ignored) {}
                }
            });

            return future;
        } catch (Throwable t) {
            logw(TAG, "Error in onGetGenAiRewriteResponse", t);
            return null;
        }
    }

    /**
     * Generates multiple high quality Russian stylistic variations for the rewrite tab.
     */
    public static List<String> generateRussianRewriteVariants(String text) {
        List<String> variants = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return variants;
        }
        String clean = text.trim();

        // 1. Formal / Polite variant
        String formal = clean;
        if (!formal.endsWith(".") && !formal.endsWith("!") && !formal.endsWith("?")) {
            formal += ".";
        }
        variants.add(formal);

        // 2. Concise variant (removes common Russian filler phrases)
        String concise = clean.replaceAll("(?iU)\\b(в общем|так сказать|короче|собственно говоря|как бы)\\b,?\\s*", "").trim();
        if (!concise.equals(clean) && !concise.isEmpty()) {
            if (Character.isLowerCase(concise.charAt(0))) {
                concise = Character.toUpperCase(concise.charAt(0)) + concise.substring(1);
            }
            if (!concise.endsWith(".") && !concise.endsWith("!") && !concise.endsWith("?")) {
                concise += ".";
            }
            variants.add(concise);
        }

        // 3. Conversational / Expressive variant
        String friendly = clean;
        if (friendly.endsWith(".")) {
            friendly = friendly.substring(0, friendly.length() - 1);
        }
        if (!friendly.endsWith("!") && !friendly.endsWith("?")) {
            friendly += "!";
        }
        if (!friendly.equals(formal) && !variants.contains(friendly)) {
            variants.add(friendly);
        }

        return variants;
    }

    public static TextScope findActiveSentenceScope(String before, String after) {
        if (before == null) before = "";
        if (after == null) after = "";

        // 1. Hard line boundary: Never cross \n or \r!
        int lastNewline = Math.max(before.lastIndexOf('\n'), before.lastIndexOf('\r'));
        String beforeInLine = lastNewline >= 0 ? before.substring(lastNewline + 1) : before;

        int nextNewline = after.indexOf('\n');
        int nextCr = after.indexOf('\r');
        if (nextNewline == -1 || (nextCr != -1 && nextCr < nextNewline)) {
            nextNewline = nextCr;
        }
        String afterInLine = nextNewline >= 0 ? after.substring(0, nextNewline) : after;

        // 2. Sentence boundary in beforeInLine:
        String scopedBefore = beforeInLine;
        int lastSentEnd = -1;
        for (int i = 0; i < beforeInLine.length() - 1; i++) {
            char c = beforeInLine.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                if (Character.isWhitespace(beforeInLine.charAt(i + 1))) {
                    lastSentEnd = i + 1;
                    while (lastSentEnd < beforeInLine.length() && Character.isWhitespace(beforeInLine.charAt(lastSentEnd))) {
                        lastSentEnd++;
                    }
                }
            }
        }

        if (lastSentEnd >= 0) {
            if (lastSentEnd >= beforeInLine.length()) {
                int prevSentEnd = -1;
                for (int i = 0; i < beforeInLine.length() - 2; i++) {
                    char c = beforeInLine.charAt(i);
                    if (c == '.' || c == '!' || c == '?') {
                        if (Character.isWhitespace(beforeInLine.charAt(i + 1))) {
                            prevSentEnd = i + 1;
                            while (prevSentEnd < beforeInLine.length() && Character.isWhitespace(beforeInLine.charAt(prevSentEnd))) {
                                prevSentEnd++;
                            }
                        }
                    }
                }
                if (prevSentEnd >= 0 && prevSentEnd < lastSentEnd) {
                    scopedBefore = beforeInLine.substring(prevSentEnd);
                } else {
                    scopedBefore = beforeInLine;
                }
            } else {
                scopedBefore = beforeInLine.substring(lastSentEnd);
            }
        }

        // 3. Sentence boundary in afterInLine:
        String scopedAfter = afterInLine;
        for (int i = 0; i < afterInLine.length(); i++) {
            char c = afterInLine.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                scopedAfter = afterInLine.substring(0, i + 1);
                break;
            }
        }

        // Clamp to max 250 characters before cursor if no punctuation was found
        if (scopedBefore.length() > 250) {
            int spaceIdx = scopedBefore.indexOf(' ', scopedBefore.length() - 250);
            if (spaceIdx >= 0) {
                scopedBefore = scopedBefore.substring(spaceIdx + 1);
            } else {
                scopedBefore = scopedBefore.substring(scopedBefore.length() - 250);
            }
        }

        return new TextScope(scopedBefore, scopedAfter);
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

        // Finish any pending composing state in Gboard before reading text
        try {
            ic.finishComposingText();
        } catch (Throwable ignored) {
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
            CharSequence before = ic.getTextBeforeCursor(1500, 0);
            CharSequence after = ic.getTextAfterCursor(600, 0);
            String beforeStr = before != null ? before.toString() : "";
            String afterStr = after != null ? after.toString() : "";
            TextScope scope = findActiveSentenceScope(beforeStr, afterStr);
            textToCheck = scope.getFullText();
            beforeLen = scope.beforeScope.length();
            afterLen = scope.afterScope.length();
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

        // 1. Hyphenated prepositions and unambiguous stable adverbs
        res = res.replaceAll("(?iU)\\b(из)\\s+(за)\\b", "$1-$2");
        res = res.replaceAll("(?iU)\\b(из)\\s+(под)\\b", "$1-$2");
        res = res.replaceAll("(?iU)\\b(вс[её])\\s+(таки)\\b", "всё-таки");
        res = res.replaceAll("(?iU)\\b(так)\\s+(таки)\\b", "так-таки");
        res = res.replaceAll("(?iU)\\b(точь)\\s+в\\s+(точь)\\b", "точь-в-точь");
        res = res.replaceAll("(?iU)\\b(чуть)\\s+(чуть)\\b", "чуть-чуть");
        res = res.replaceAll("(?iU)\\b(едва)\\s+(едва)\\b", "едва-едва");
        res = res.replaceAll("(?iU)\\b(кое)\\s+(как|кто|что|где|куда)\\b", "кое-$2");
        res = res.replaceAll("(?iU)\\b(во|в)\\s+(первых|вторых|третьих)\\b", "$1-$2");
        res = res.replaceAll("(?iU)\\b(по)\\s+(русски|английски|немецки)\\b", "$1-$2");

        // Particles -то, -нибудь, -либо
        res = res.replaceAll("(?iU)\\b(что|кто|где|куда|когда|почему|зачем|как|какой|какая|какое|какие|каком|каких|какую|каким|чей|чья|чь[её]|чьи)\\s+(то)\\b", "$1-$2");
        res = res.replaceAll("(?iU)\\b(что|кто|где|куда|когда|как|какой|какая|какое|какие|каком|каких|какую|чей)\\s+(нибудь)\\b", "$1-$2");
        res = res.replaceAll("(?iU)\\b(что|кто|где|куда|когда|как|какой)\\s+(либо)\\b", "$1-$2");

        // 2. Compound conjunctions (must be executed before single conjunctions)
        String[] compoundConjunctions = new String[] {
                "потому что", "так как", "так что", "для того чтобы", "из-за того что",
                "несмотря на то что", "в то время как", "с тех пор как", "до тех пор пока",
                "как будто", "как только", "то есть", "а также", "вместо того чтобы",
                "перед тем как", "после того как", "в связи с тем что", "благодаря тому что"
        };
        for (String conj : compoundConjunctions) {
            res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(conj) + ")\\b", "$1, $2");
        }

        // 3. Opposing & coordinating conjunctions (indisputable comma rules)
        res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(но|однако|зато)\\b", "$1, $2");
        res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(а)\\b", "$1, $2");

        // 4. Subordinate conjunctions & relative words
        String[] subordinateConjunctions = new String[] {
                "чтобы", "чтоб", "хотя", "где", "куда", "откуда", "когда",
                "который", "которая", "которое", "которые", "которого", "которой", "которому",
                "которым", "которую", "которых", "которыми"
        };
        for (String conj : subordinateConjunctions) {
            res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(conj) + ")\\b", "$1, $2");
        }

        // Subordinate "что"
        res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(что)\\b", "$1, $2");
        // Revert false comma splits around specific idioms:
        res = res.replaceAll("(?iU),\\s*потому,\\s*что\\b", ", потому что");
        res = res.replaceAll("(?iU)\\bпотому,\\s*что\\b", "потому что");
        res = res.replaceAll("(?iU),\\s*только,\\s*что\\b", ", только что");
        res = res.replaceAll("(?iU)\\bтолько,\\s*что\\b", "только что");
        res = res.replaceAll("(?iU),\\s*разве,\\s*что\\b", ", разве что");
        res = res.replaceAll("(?iU)\\bразве,\\s*что\\b", "разве что");
        res = res.replaceAll("(?iU),\\s*кое,\\s*что\\b", "кое-что");
        res = res.replaceAll("(?iU)\\bкое,\\s*что\\b", "кое-что");

        // 5. Introductory words - only unambiguous modal words
        String[] introWords = new String[] {
                "конечно", "кстати", "например", "к счастью", "к сожалению",
                "в общем",
                "между прочим", "разумеется", "безусловно",
                "во-первых", "во-вторых", "в-третьих", "следовательно", "таким образом"
        };
        for (String iw : introWords) {
            // At start of sentence, followed by words
            res = res.replaceAll("(?iU)(^|[.!?]\\s+)(" + Pattern.quote(iw) + ")\\b(?=\\s+[а-яА-ЯёЁa-zA-Z0-9])", "$1$2, ");
            // In middle of sentence, followed by words
            res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(iw) + ")\\b(?=\\s+[а-яА-ЯёЁa-zA-Z0-9])", "$1, $2, ");
            // At end of sentence or before punctuation: comma only before the introductory word, never after!
            res = res.replaceAll("(?iU)([а-яА-ЯёЁ0-9])\\s+(" + Pattern.quote(iw) + ")\\b(?=\\s*[.!?]|$)", "$1, $2");
        }

        // Idioms that must NOT have commas
        res = res.replaceAll("(?iU),\\s*не\\s+за,\\s*что\\b", " не за что");
        res = res.replaceAll("(?iU)\\bне\\s+за,\\s*что\\b", "не за что");

        // 6. Typographic spacing and punctuation cleanup
        res = res.replaceAll("[ \\t]{2,}", " ");
        res = res.replaceAll("\\s+([,.:;!?])", "$1");
        res = res.replaceAll(",\\s*,", ",");
        res = res.replaceAll(",\\s*([.!?])", "$1");
        res = res.replaceAll("([,;])([а-яА-ЯёЁa-zA-Z])", "$1 $2");
        res = res.replaceAll(",\\s*$", "");
        res = res.replaceAll(",\\s*\\n", "\\n");

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
                if (c == '.' || c == '!' || c == '?' || c == '\n') {
                    capitalizeNext = true;
                }
            }
        }
        res = capitalized.toString();

        // 8. Question mark for interrogative sentences ending without punctuation (excluding indefinite -то/-нибудь)
        if (res.matches("(?iU)^(почему|зачем|как|когда|где|куда|откуда|кто|что|сколько|какой|какая|какое|какие)\\b(?!-(то|нибудь|либо)).*[^.!?]$")) {
            res = res + "?";
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

        InputConnection ic = ims.getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        try {
            ic.beginBatchEdit();
            try {
                ic.finishComposingText();
            } catch (Throwable ignored) {
            }

            String orig = result.originalText;
            String corr = result.correctedText;

            // 1. Check active selection: if current selection matches orig
            CharSequence currentSel = ic.getSelectedText(0);
            if (currentSel != null && currentSel.length() > 0) {
                String selStr = currentSel.toString();
                if (selStr.equals(orig) || selStr.trim().equals(orig.trim())) {
                    ic.commitText(corr, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via active selection: " + corr);
                    return true;
                }
            }

            // 2. Read context around cursor
            CharSequence beforeCs = ic.getTextBeforeCursor(1500, 0);
            CharSequence afterCs = ic.getTextAfterCursor(600, 0);
            String before = beforeCs != null ? beforeCs.toString() : "";
            String after = afterCs != null ? afterCs.toString() : "";

            // Case 1: Cursor is right after originalText (standard typing at end of input)
            if (before.endsWith(orig)) {
                ic.deleteSurroundingText(orig.length(), 0);
                ic.commitText(corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied correction via before.endsWith: " + corr);
                return true;
            }

            // Case 2: Cursor is right before originalText
            if (after.startsWith(orig)) {
                ic.deleteSurroundingText(0, orig.length());
                ic.commitText(corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied correction via after.startsWith: " + corr);
                return true;
            }

            // Case 3: Cursor is inside originalText
            boolean matchedInside = false;
            for (int k = 1; k < orig.length(); k++) {
                String prefix = orig.substring(0, k);
                String suffix = orig.substring(k);
                if (before.endsWith(prefix) && after.startsWith(suffix)) {
                    ic.deleteSurroundingText(prefix.length(), suffix.length());
                    ic.commitText(corr, 1);
                    matchedInside = true;
                    logw(TAG, "Applied correction via split match: " + corr);
                    break;
                }
            }
            if (matchedInside) {
                ic.endBatchEdit();
                return true;
            }

            // Case 4: Trimmed match (handling trailing/leading spaces desync)
            String origTrim = orig.trim();
            String corrTrim = corr.trim();
            if (!origTrim.isEmpty()) {
                int trailingSpaces = 0;
                while (trailingSpaces < before.length() && Character.isWhitespace(before.charAt(before.length() - 1 - trailingSpaces))) {
                    trailingSpaces++;
                }
                String beforeStripped = before.substring(0, before.length() - trailingSpaces);
                if (beforeStripped.endsWith(origTrim)) {
                    int toDel = origTrim.length() + trailingSpaces;
                    ic.deleteSurroundingText(toDel, 0);
                    String toCommit = trailingSpaces > 0 ? (corrTrim + " ") : corr;
                    ic.commitText(toCommit, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via trimmed before.endsWith: " + toCommit);
                    return true;
                }

                int leadingSpaces = 0;
                while (leadingSpaces < after.length() && Character.isWhitespace(after.charAt(leadingSpaces))) {
                    leadingSpaces++;
                }
                String afterStripped = after.substring(leadingSpaces);
                if (afterStripped.startsWith(origTrim)) {
                    int toDel = origTrim.length() + leadingSpaces;
                    ic.deleteSurroundingText(0, toDel);
                    ic.commitText(corr, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via trimmed after.startsWith: " + corr);
                    return true;
                }

                for (int k = 1; k < origTrim.length(); k++) {
                    String prefix = origTrim.substring(0, k);
                    String suffix = origTrim.substring(k);
                    if (before.endsWith(prefix) && after.startsWith(suffix)) {
                        ic.deleteSurroundingText(prefix.length(), suffix.length());
                        ic.commitText(corrTrim, 1);
                        matchedInside = true;
                        logw(TAG, "Applied correction via trimmed split match: " + corrTrim);
                        break;
                    }
                }
                if (matchedInside) {
                    ic.endBatchEdit();
                    return true;
                }
            }

            // Case 5: Near-cursor search in before (if cursor shifted slightly e.g. after punctuation auto-space)
            int idxInBefore = before.lastIndexOf(orig);
            if (idxInBefore == -1 && !origTrim.isEmpty()) {
                idxInBefore = before.lastIndexOf(origTrim);
            }
            if (idxInBefore >= 0) {
                int matchedLen = before.startsWith(orig, idxInBefore) ? orig.length() : origTrim.length();
                int charsAfter = before.length() - (idxInBefore + matchedLen);
                if (charsAfter <= 15) {
                    String extra = before.substring(idxInBefore + matchedLen);
                    ic.deleteSurroundingText(charsAfter + matchedLen, 0);
                    ic.commitText(corr + extra, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via near-cursor index search in before");
                    return true;
                }
            }

            // Case 6: Near-cursor search in after
            int idxInAfter = after.indexOf(orig);
            if (idxInAfter == -1 && !origTrim.isEmpty()) {
                idxInAfter = after.indexOf(origTrim);
            }
            if (idxInAfter >= 0 && idxInAfter <= 15) {
                int matchedLen = after.startsWith(orig, idxInAfter) ? orig.length() : origTrim.length();
                String prefixExtra = after.substring(0, idxInAfter);
                ic.deleteSurroundingText(0, idxInAfter + matchedLen);
                ic.commitText(prefixExtra + corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied correction via near-cursor index search in after");
                return true;
            }

            // Case 7: Case-insensitive match at end of before
            if (before.length() >= orig.length() && before.substring(before.length() - orig.length()).equalsIgnoreCase(orig)) {
                ic.deleteSurroundingText(orig.length(), 0);
                ic.commitText(corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied correction via case-insensitive before.endsWith: " + corr);
                return true;
            }

            // Case 8: Verified stored scope match (only if the text before cursor actually matches stored beforeScope)
            if (result.beforeLength > 0 && before.length() >= result.beforeLength) {
                String beforeSub = before.substring(before.length() - result.beforeLength);
                String expectedBefore = result.originalText.substring(0, result.beforeLength);
                if (beforeSub.equals(expectedBefore) || beforeSub.equalsIgnoreCase(expectedBefore)) {
                    int toDelAfter = 0;
                    if (result.afterLength > 0 && after.length() >= result.afterLength) {
                        String afterSub = after.substring(0, result.afterLength);
                        String expectedAfter = result.originalText.substring(result.beforeLength);
                        if (afterSub.equals(expectedAfter) || afterSub.equalsIgnoreCase(expectedAfter)) {
                            toDelAfter = result.afterLength;
                        }
                    }
                    ic.deleteSurroundingText(result.beforeLength, toDelAfter);
                    ic.commitText(corr, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via verified stored scope: " + corr);
                    return true;
                }
            }

            // Safe Abort: Do NOT blindly delete characters if no verified match was found!
            logw(TAG, "Aborted replacement: target text does not match cursor context (orig='" + orig + "'); preventing destructive deletion");
            ic.endBatchEdit();
            return false;
        } catch (Throwable t) {
            logw(TAG, "Failed to apply correction to InputConnection", t);
            return false;
        }
    }
}
