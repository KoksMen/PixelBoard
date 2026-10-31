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
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
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
import java.util.Collection;
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

    public static volatile String sLastOriginalText = null;
    public static volatile String sLastCorrectedText = null;

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
    private static volatile String sLastCleanCorrectedText = null;

    /**
     * Intercepts native Gboard Jarvis (Writing Tools) button clicks.
     * When Yandex Speller is active (the default), directly launches our unified
     * Git Diff proofread & rewrite card so the user gets instant, full-featured
     * proofreading without Google service failures or broken text!
     */
    public static boolean onJarvisClicked(Object caller) {
        if (sBypassJarvisIntercept) {
            return false;
        }
        try {
            View targetView = null;
            if (caller instanceof View) {
                targetView = (View) caller;
            }
            if (targetView == null) {
                targetView = GboardProofreadButton.getAttachedParent();
            }
            if (targetView == null && caller != null) {
                try {
                    Object obr = null;
                    try {
                        obr = caller.getClass().getMethod("ah").invoke(caller);
                    } catch (Throwable ignored) {}
                    if (obr == null) {
                        try {
                            obr = caller.getClass().getMethod("aa").invoke(caller);
                        } catch (Throwable ignored) {}
                    }
                    if (obr != null) {
                        Object view = obr.getClass().getMethod("l").invoke(obr);
                        if (view instanceof View) targetView = (View) view;
                        if (targetView == null) {
                            view = obr.getClass().getMethod("k").invoke(obr);
                            if (view instanceof View) targetView = (View) view;
                        }
                    }
                } catch (Throwable ignored) {}
            }

            if (targetView == null) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null && ims.getWindow() != null) {
                    View decor = ims.getWindow().getWindow() != null
                            ? ims.getWindow().getWindow().getDecorView()
                            : null;
                    if (decor != null) targetView = decor;
                }
            }

            if (targetView != null) {
                Context context = targetView.getContext();
                String backend = com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.readBackendType(
                        com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.preferences(context));

                // If user explicitly configured an on-device/Google backend, let native Jarvis open
                if (com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_AICORE.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_PRIVATE_INFERENCE_ASTREA.equals(backend)
                        || com.akshaykadam.pixelboard.extension.writingtools.GboardAiWritingToolsSettings.BACKEND_GBOARD_SERVER.equals(backend)) {
                    return false;
                }

                // Default & Yandex backend: Intercept and display our unified Yandex Git Diff card!
                final View finalView = targetView;
                getMainHandler().post(() -> checkAndShowDiff(finalView));
                return true;
            }
        } catch (Throwable t) {
            logw(TAG, "Failed in onJarvisClicked", t);
        }
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
     * Extracts text from an oxh (Gboard Document/Selection descriptor) instance.
     * Captures the complete text rather than arbitrarily slicing to a single sentence.
     */
    private static String extractTextFromOxh(Object oxh) {
        if (oxh == null) return null;
        try {
            Method oMethod = oxh.getClass().getMethod("o");
            boolean isSelected = Boolean.TRUE.equals(oMethod.invoke(oxh));
            if (isSelected) {
                Method lMethod = oxh.getClass().getMethod("l");
                Object sel = lMethod.invoke(oxh);
                if (sel != null && !sel.toString().trim().isEmpty()) {
                    return sel.toString();
                }
            }
            // If not selected, capture the full document text (oxh.b)
            Field bField = oxh.getClass().getDeclaredField("b");
            bField.setAccessible(true);
            Object bObj = bField.get(oxh);
            if (bObj != null && !bObj.toString().trim().isEmpty()) {
                return bObj.toString();
            }
        } catch (Throwable t) {
            logw(TAG, "Error extracting text from oxh", t);
        }
        return oxh.toString();
    }

    /**
     * Extracts text to proofread from either the trigger info (qrw) or JarvisExtension (hip).
     */
    private static String extractTextToProofread(Object hipInstance, Object qrwInstance) {
        String text = null;
        if (qrwInstance != null) {
            try {
                Field bField = qrwInstance.getClass().getDeclaredField("b");
                bField.setAccessible(true);
                Object oxh = bField.get(qrwInstance);
                if (oxh != null) {
                    text = extractTextFromOxh(oxh);
                }
            } catch (Throwable ignored) {}
            if (text == null || text.trim().isEmpty()) {
                try {
                    Method fMethod = qrwInstance.getClass().getMethod("F", qrwInstance.getClass());
                    Object s = fMethod.invoke(null, qrwInstance);
                    if (s != null) {
                        text = s.toString();
                    }
                } catch (Throwable ignored) {}
            }
        }
        if ((text == null || text.trim().isEmpty()) && hipInstance != null) {
            try {
                Field xField = hipInstance.getClass().getDeclaredField("x");
                xField.setAccessible(true);
                Object curX = xField.get(hipInstance);
                if (curX != null) {
                    text = extractTextFromOxh(curX);
                }
            } catch (Throwable ignored) {}
            if (text == null || text.trim().isEmpty()) {
                try {
                    Method vMethod = hipInstance.getClass().getMethod("v", boolean.class);
                    Object oxh = vMethod.invoke(hipInstance, true);
                    if (oxh != null) {
                        text = extractTextFromOxh(oxh);
                    }
                } catch (Throwable ignored) {}
            }
        }
        if (text == null || text.trim().isEmpty()) {
            InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
            if (ims != null && ims.getCurrentInputConnection() != null) {
                InputConnection ic = ims.getCurrentInputConnection();
                try {
                    ExtractedTextRequest req = new ExtractedTextRequest();
                    req.hintMaxChars = 10000;
                    req.hintMaxLines = 500;
                    ExtractedText et = ic.getExtractedText(req, 0);
                    if (et != null && et.text != null && et.text.length() > 0) {
                        text = et.text.toString();
                    }
                } catch (Throwable ignored) {}
                if (text == null || text.trim().isEmpty()) {
                    CharSequence before = ic.getTextBeforeCursor(4000, 0);
                    CharSequence after = ic.getTextAfterCursor(2000, 0);
                    text = (before != null ? before.toString() : "") + (after != null ? after.toString() : "");
                }
            }
        }
        return text;
    }

    /**
     * Configures the target replacement range in JarvisExtension (hip.x).
     * Preserves intact document coordinates to prevent cursor/span desync.
     */
    private static void setupTargetRangeInHip(Object hipInstance, String textToTarget) {
        if (hipInstance == null) return;
        try {
            Field xField = hipInstance.getClass().getDeclaredField("x");
            xField.setAccessible(true);
            Object curX = xField.get(hipInstance);
            if (curX == null) {
                Method vMethod = hipInstance.getClass().getMethod("v", boolean.class);
                curX = vMethod.invoke(hipInstance, true);
            }
            if (curX != null) {
                xField.set(hipInstance, curX);
            }
        } catch (Throwable t) {
            logw(TAG, "Failed to setup target range in hip.x", t);
        }
    }

    /**
     * Constructs a fully-validated Omq suggestion matching Gboard's exact builder constraints:
     * sets bitmask 0x7 (index, score, isPersonalized), sessionId, modelVersion, omr.a (NO_ERROR),
     * candidateOrigin, failureMode, and optional style prompt.
     */
    private static Object createOmqSuggestion(ClassLoader cl, Method zMethod, String text, int index, Object ompInstance) {
        try {
            Object builder = zMethod.invoke(null);
            Class<?> bClass = builder.getClass();

            // 1. text (b: String)
            bClass.getMethod("i", String.class).invoke(builder, text != null ? text : "");

            // 2. index (c: int) -> bit 0x1
            bClass.getMethod("d", int.class).invoke(builder, index);

            // 3. score (d: double) -> bit 0x2
            bClass.getMethod("g", double.class).invoke(builder, 1.0);

            // 4. isPersonalized (k: boolean) -> bit 0x4 (Total bitmask l = 1|2|4 = 7)
            bClass.getMethod("e", boolean.class).invoke(builder, false);

            // 5. sessionId (f: String)
            bClass.getMethod("f", String.class).invoke(builder, "PixelBoardProofread");

            // 6. modelVersion (h: String)
            bClass.getMethod("h", String.class).invoke(builder, "yandex-speller-v1");

            // 7. errorCode (g: Lomr;) -> Lomr;->a (NO_ERROR)
            Class<?> omrClass = Class.forName("omr", true, cl);
            Field omrAField = omrClass.getField("a");
            bClass.getMethod("c", omrClass).invoke(builder, omrAField.get(null));

            // 8. candidateOrigin (i: Loko;) -> Loko;->a
            Class<?> okoClass = Class.forName("oko", true, cl);
            Field okoAField = okoClass.getField("a");
            bClass.getMethod("j", okoClass).invoke(builder, okoAField.get(null));

            // 9. failureMode (j: Lomn;) -> Lomn;->a
            Class<?> omnClass = Class.forName("omn", true, cl);
            Field omnAField = omnClass.getField("a");
            bClass.getMethod("b", omnClass).invoke(builder, omnAField.get(null));

            // 10. style (e: Optional<Lomp;>)
            if (ompInstance != null) {
                try {
                    Class<?> optionalClass = Class.forName("j$.util.Optional", true, cl);
                    Method ofMethod = optionalClass.getMethod("of", Object.class);
                    Object opt = ofMethod.invoke(null, ompInstance);
                    bClass.getMethod("k", optionalClass).invoke(builder, opt);
                } catch (Throwable ignored) {
                }
            }

            return bClass.getMethod("a").invoke(builder);
        } catch (Throwable t) {
            logw(TAG, "Failed to create Omq suggestion via builder", t);
            return null;
        }
    }

    /**
     * Intercepts Gboard native Writing Tools proofread requests (hip.y).
     * Replaces Google GenAI gRPC call with Yandex Speller API + Russian grammar heuristics.
     */
    /**
     * Converts a Java Collection into Gboard's native Guava ImmutableList (wfr).
     * This avoids ClassCastException in Gboard's internal Future callbacks (hkz, gut).
     */
    private static Object toImmutableList(ClassLoader cl, Collection<?> list) {
        try {
            Class<?> wfrClass = Class.forName("wfr", true, cl);
            Method oMethod = wfrClass.getMethod("o", Collection.class);
            return oMethod.invoke(null, list != null ? list : Collections.emptyList());
        } catch (Throwable t) {
            logw(TAG, "Failed to convert collection to wfr (ImmutableList)", t);
            return list != null ? list : Collections.emptyList();
        }
    }

    /**
     * Filters out characters spanned by StrikethroughSpan from the Git Diff Spannable
     * so only the clean corrected text is committed to the editor.
     */
    public static CharSequence filterCommitText(CharSequence seq) {
        if (seq == null) {
            return null;
        }
        if (sLastCleanCorrectedText != null && !sLastCleanCorrectedText.isEmpty()) {
            CharSequence clean = sLastCleanCorrectedText;
            sLastCleanCorrectedText = null;
            return clean;
        }
        if (!(seq instanceof android.text.Spanned)) {
            if (sLastCorrectedText != null && !sLastCorrectedText.isEmpty()) {
                String str = seq.toString();
                if (sLastOriginalText != null && str.equals(sLastOriginalText + sLastCorrectedText)) {
                    return sLastCorrectedText;
                }
            }
            return seq;
        }
        android.text.Spanned spanned = (android.text.Spanned) seq;
        android.text.style.StrikethroughSpan[] strikeSpans =
                spanned.getSpans(0, spanned.length(), android.text.style.StrikethroughSpan.class);
        if (strikeSpans == null || strikeSpans.length == 0) {
            return seq;
        }
        boolean[] struck = new boolean[spanned.length()];
        for (android.text.style.StrikethroughSpan span : strikeSpans) {
            int start = Math.max(0, spanned.getSpanStart(span));
            int end = Math.min(spanned.length(), spanned.getSpanEnd(span));
            for (int i = start; i < end; i++) {
                struck[i] = true;
            }
        }
        StringBuilder clean = new StringBuilder(spanned.length());
        for (int i = 0; i < spanned.length(); i++) {
            if (!struck[i]) {
                clean.append(spanned.charAt(i));
            }
        }
        return clean.toString();
    }

    public static Object onGetGenAiProofreadResponse(Object hipInstance, Object qrwInstance) {
        try {
            Context context = getContextFromHip(hipInstance);
            if (context == null) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null) context = ims.getApplicationContext();
            }

            String textToProofread = extractTextToProofread(hipInstance, qrwInstance);
            if (textToProofread == null || textToProofread.trim().isEmpty()) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null && ims.getCurrentInputConnection() != null) {
                    CharSequence before = ims.getCurrentInputConnection().getTextBeforeCursor(1000, 0);
                    if (before != null && before.length() > 0) {
                        textToProofread = before.toString();
                    }
                }
            }

            ClassLoader cl = hipInstance != null ? hipInstance.getClass().getClassLoader() : GboardProofreadService.class.getClassLoader();
            Class<?> xjaClass = Class.forName("xja", true, cl);
            final Object future = xjaClass.getDeclaredConstructor().newInstance();
            final Method edMethod = xjaClass.getMethod("ed", Object.class);

            final Class<?> omqClass = Class.forName("omq", true, cl);
            final Method zMethod = omqClass.getMethod("z");

            if (textToProofread == null || textToProofread.trim().isEmpty()) {
                Object suggestion = createOmqSuggestion(cl, zMethod, "", 0, null);
                List<Object> list = new ArrayList<>();
                if (suggestion != null) list.add(suggestion);
                edMethod.invoke(future, toImmutableList(cl, list));
                return future;
            }

            sLastOriginalText = textToProofread;
            setupTargetRangeInHip(hipInstance, textToProofread);

            if (hipInstance != null) {
                try {
                    Field yField = hipInstance.getClass().getDeclaredField("y");
                    yField.setAccessible(true);
                    yField.set(hipInstance, future);
                } catch (Throwable t) {
                    logw(TAG, "Failed to set hip.y in proofread", t);
                }
            }

            final String text = textToProofread;
            sExecutor.execute(() -> {
                try {
                    ProofreadResult result = checkTextInternal(text, false, 0, 0);
                    String corrected = result.hasChanges ? result.correctedText : text;
                    sLastCorrectedText = corrected;
                    sLastCleanCorrectedText = corrected;

                    Object suggestion = createOmqSuggestion(cl, zMethod, corrected, 0, null);

                    List<Object> list = new ArrayList<>();
                    if (suggestion != null) list.add(suggestion);

                    Object immutableList = toImmutableList(cl, list);
                    edMethod.invoke(future, immutableList);
                } catch (Throwable t) {
                    logw(TAG, "Worker failed in onGetGenAiProofreadResponse", t);
                    try {
                        Object suggestion = createOmqSuggestion(cl, zMethod, text != null ? text : "", 0, null);
                        List<Object> list = new ArrayList<>();
                        if (suggestion != null) list.add(suggestion);
                        edMethod.invoke(future, toImmutableList(cl, list));
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
            String original = (originalSeq != null && originalSeq.length() > 0) ? originalSeq.toString() : sLastOriginalText;
            String corrected = (correctedSeq != null && correctedSeq.length() > 0) ? correctedSeq.toString() : sLastCorrectedText;

            if (corrected == null) {
                return null;
            }
            if (original == null || original.isEmpty() || original.equals(corrected)) {
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

            String textToRewrite = extractTextToProofread(hipInstance, qrwInstance);
            if (textToRewrite == null || textToRewrite.trim().isEmpty()) {
                InputMethodService ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
                if (ims != null && ims.getCurrentInputConnection() != null) {
                    CharSequence before = ims.getCurrentInputConnection().getTextBeforeCursor(1000, 0);
                    if (before != null && before.length() > 0) {
                        textToRewrite = before.toString();
                    }
                }
            }

            ClassLoader cl = hipInstance != null ? hipInstance.getClass().getClassLoader() : GboardProofreadService.class.getClassLoader();
            Class<?> xjaClass = Class.forName("xja", true, cl);
            final Object future = xjaClass.getDeclaredConstructor().newInstance();
            final Method edMethod = xjaClass.getMethod("ed", Object.class);

            final Class<?> omqClass = Class.forName("omq", true, cl);
            final Method zMethod = omqClass.getMethod("z");

            if (textToRewrite == null || textToRewrite.trim().isEmpty()) {
                Object suggestion = createOmqSuggestion(cl, zMethod, "", 0, ompInstance);
                List<Object> list = new ArrayList<>();
                if (suggestion != null) list.add(suggestion);
                edMethod.invoke(future, toImmutableList(cl, list));
                return future;
            }

            sLastOriginalText = textToRewrite;
            setupTargetRangeInHip(hipInstance, textToRewrite);

            if (hipInstance != null) {
                try {
                    Field yField = hipInstance.getClass().getDeclaredField("y");
                    yField.setAccessible(true);
                    yField.set(hipInstance, future);
                } catch (Throwable t) {
                    logw(TAG, "Failed to set hip.y in rewrite", t);
                }
            }

            final String text = textToRewrite;
            sExecutor.execute(() -> {
                try {
                    ProofreadResult baseResult = checkTextInternal(text, false, 0, 0);
                    String baseClean = baseResult.hasChanges ? baseResult.correctedText : text;

                    List<String> variants = generateRussianRewriteVariants(baseClean, ompInstance);
                    List<Object> list = new ArrayList<>();
                    int idx = 0;
                    for (String v : variants) {
                        if (v == null || v.trim().isEmpty()) continue;
                        Object suggestion = createOmqSuggestion(cl, zMethod, v, idx++, ompInstance);
                        if (suggestion != null) {
                            list.add(suggestion);
                        }
                    }

                    if (list.isEmpty()) {
                        Object suggestion = createOmqSuggestion(cl, zMethod, baseClean, 0, ompInstance);
                        if (suggestion != null) list.add(suggestion);
                    }

                    Object immutableList = toImmutableList(cl, list);
                    edMethod.invoke(future, immutableList);
                } catch (Throwable t) {
                    logw(TAG, "Error in onGetGenAiRewriteResponse worker", t);
                    try {
                        Object suggestion = createOmqSuggestion(cl, zMethod, text != null ? text : "", 0, ompInstance);
                        List<Object> list = new ArrayList<>();
                        if (suggestion != null) list.add(suggestion);
                        edMethod.invoke(future, toImmutableList(cl, list));
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
     * Generates a specific styled variant of Russian text (Formal, Casual, Shorten, Emojify)
     * while preserving leading and trailing whitespace.
     */
    public static String getRussianRewriteVariant(String text, String style) {
        if (text == null || text.trim().isEmpty()) return text;
        int startSpaces = 0;
        while (startSpaces < text.length() && Character.isWhitespace(text.charAt(startSpaces))) startSpaces++;
        int endSpaces = 0;
        while (endSpaces < text.length() && Character.isWhitespace(text.charAt(text.length() - 1 - endSpaces))) endSpaces++;
        String prefix = text.substring(0, startSpaces);
        String suffix = text.substring(text.length() - endSpaces);
        String clean = text.trim();

        String styled = clean;
        if ("FORMALIZE".equals(style)) {
            styled = clean.replaceAll("(?iu)\\b(привет|хай|салют)\\b", "Здравствуйте");
            styled = styled.replaceAll("(?iu)\\b(спс|пасиб|пасиба)\\b", "спасибо");
            styled = styled.replaceAll("(?iu)\\b(норм|нормас)\\b", "хорошо");
            styled = styled.replaceAll("(?iu)\\b(ок|окэй|окей)\\b", "хорошо");
            styled = styled.replaceAll("(?iu)\\b(ч[её])\\b", "что");
            if (Character.isLowerCase(styled.charAt(0))) {
                styled = Character.toUpperCase(styled.charAt(0)) + styled.substring(1);
            }
            if (!styled.endsWith(".") && !styled.endsWith("!") && !styled.endsWith("?")) {
                styled += ".";
            }
        } else if ("CASUALIZE".equals(style)) {
            styled = clean.replaceAll("(?iu)\\b(здравствуйте|добрый день|добрый вечер)\\b", "Привет");
            if (styled.endsWith(".")) {
                styled = styled.substring(0, styled.length() - 1);
            }
            if (!styled.endsWith("!") && !styled.endsWith("?")) {
                styled += "!";
            }
        } else if ("SHORTEN".equals(style)) {
            styled = clean.replaceAll("(?iu)\\b(в общем|так сказать|короче|собственно говоря|как бы|в принципе|на самом деле|типа)\\b,?\\s*", "").trim();
            if (styled.isEmpty()) styled = clean;
            styled = styled.replaceAll("[ \\t]{2,}", " ");
            styled = styled.replaceAll("[ \\t]+([,.:;!?])", "$1");
            if (Character.isLowerCase(styled.charAt(0))) {
                styled = Character.toUpperCase(styled.charAt(0)) + styled.substring(1);
            }
            if (!styled.endsWith(".") && !styled.endsWith("!") && !styled.endsWith("?")) {
                styled += ".";
            }
        } else if ("EMOJIFY".equals(style)) {
            styled = clean + " 😊👍";
        }
        return prefix + styled + suffix;
    }

    /**
     * Generates multiple high quality Russian stylistic variations for the rewrite tab,
     * tailored to the requested Gboard style option (omp: Formal, Casual, Shorten, Emojify, etc.).
     */
    public static List<String> generateRussianRewriteVariants(String text, Object ompInstance) {
        List<String> variants = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return variants;
        }
        String clean = text.trim();
        String style = ompInstance != null ? ompInstance.toString() : "";

        if (style.contains("FORMALIZE")) {
            String formal = clean;
            if (!formal.endsWith(".") && !formal.endsWith("!") && !formal.endsWith("?")) {
                formal += ".";
            }
            variants.add(formal);
            variants.add("Уважаемые коллеги, " + Character.toLowerCase(formal.charAt(0)) + formal.substring(1));
        } else if (style.contains("CASUALIZE")) {
            String friendly = clean;
            if (friendly.endsWith(".")) {
                friendly = friendly.substring(0, friendly.length() - 1);
            }
            if (!friendly.endsWith("!") && !friendly.endsWith("?")) {
                friendly += "!";
            }
            variants.add(friendly);
            variants.add("Привет! " + friendly);
        } else if (style.contains("SHORTEN")) {
            String concise = clean.replaceAll("(?iu)\\b(в общем|так сказать|короче|собственно говоря|как бы|в принципе)\\b,?\\s*", "").trim();
            if (concise.isEmpty()) concise = clean;
            if (Character.isLowerCase(concise.charAt(0))) {
                concise = Character.toUpperCase(concise.charAt(0)) + concise.substring(1);
            }
            if (!concise.endsWith(".") && !concise.endsWith("!") && !concise.endsWith("?")) {
                concise += ".";
            }
            variants.add(concise);
        } else if (style.contains("EMOJIFY")) {
            variants.add(clean + " 😊👍");
            variants.add("✨ " + clean + " ✨");
        } else if (style.contains("ELABORATE")) {
            variants.add(clean + " Подводя итог, следует особо отметить ключевое значение сказанного.");
            variants.add(clean);
        } else {
            variants.addAll(generateRussianRewriteVariants(clean));
        }
        return variants;
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
        String concise = clean.replaceAll("(?iu)\\b(в общем|так сказать|короче|собственно говоря|как бы)\\b,?\\s*", "").trim();
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
            // First attempt full document capture via ExtractedText
            String fullFieldText = null;
            try {
                ExtractedTextRequest req = new ExtractedTextRequest();
                req.hintMaxChars = 10000;
                req.hintMaxLines = 500;
                ExtractedText et = ic.getExtractedText(req, 0);
                if (et != null && et.text != null && et.text.length() > 0) {
                    fullFieldText = et.text.toString();
                }
            } catch (Throwable ignored) {}

            if (fullFieldText != null && !fullFieldText.trim().isEmpty() && fullFieldText.length() <= 8000) {
                textToCheck = fullFieldText;
                beforeLen = -1; // Marker: full document mode
                afterLen = -1;
            } else {
                CharSequence before = ic.getTextBeforeCursor(4000, 0);
                CharSequence after = ic.getTextAfterCursor(2000, 0);
                String beforeStr = before != null ? before.toString() : "";
                String afterStr = after != null ? after.toString() : "";
                textToCheck = beforeStr + afterStr;
                beforeLen = beforeStr.length();
                afterLen = afterStr.length();
            }
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
     * Clean and safe Russian grammar & typography engine.
     * Fixes stable hyphenated particles (-то, -нибудь, -либо), stable prepositions,
     * and normalizes typographic spacing around punctuation without arbitrarily
     * inserting disruptive commas or unwanted question marks.
     */
    public static String applyGrammarHeuristics(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        try {
            int startSpaces = 0;
            while (startSpaces < text.length() && Character.isWhitespace(text.charAt(startSpaces))) {
                startSpaces++;
            }
            int endSpaces = 0;
            while (endSpaces < text.length() && Character.isWhitespace(text.charAt(text.length() - 1 - endSpaces))) {
                endSpaces++;
            }

            String res = text.trim();

            // 1. Stable Russian hyphenated particles (-то, -нибудь, -либо)
            res = res.replaceAll("(?iu)\\b(что|кто|где|куда|когда|почему|зачем|как|какой|какая|какое|какие|каком|каких|какую|каким|чей|чья|чь[её]|чьи)\\s+(то)\\b", "$1-$2");
            res = res.replaceAll("(?iu)\\b(что|кто|где|куда|когда|как|какой|какая|какое|какие|каком|каких|какую|чей)\\s+(нибудь)\\b", "$1-$2");
            res = res.replaceAll("(?iu)\\b(что|кто|где|куда|когда|как|какой)\\s+(либо)\\b", "$1-$2");

            // 2. Stable hyphenated prepositions and adverbs
            res = res.replaceAll("(?iu)\\b(из)\\s+(за)\\b", "$1-$2");
            res = res.replaceAll("(?iu)\\b(из)\\s+(под)\\b", "$1-$2");
            res = res.replaceAll("(?iu)\\b(вс[её])\\s+(таки)\\b", "всё-таки");
            res = res.replaceAll("(?iu)\\b(кое)\\s+(как|кто|что|где|куда)\\b", "кое-$2");

            // 3. Typographic spacing and punctuation cleanup (no destructive commas or question marks)
            res = res.replaceAll("[ \\t]{2,}", " ");
            res = res.replaceAll("[ \\t]+([,.:;!?])", "$1");
            res = res.replaceAll("([,;])([а-яА-ЯёЁa-zA-Z0-9])", "$1 $2");
            res = res.replaceAll(",[ \\t]*$", "");
            res = res.replaceAll(",[ \\t]*\\n", "\\n");
            res = res.replaceAll("[ \\t]{2,}", " ");

            // 4. Capitalize first letter of document if the original text was at the start of a sentence
            if (!res.isEmpty() && Character.isLetter(res.charAt(0)) && Character.isUpperCase(text.trim().charAt(0))) {
                res = Character.toUpperCase(res.charAt(0)) + (res.length() > 1 ? res.substring(1) : "");
            }

            // Reattach leading and trailing whitespace exactly as in original
            String prefix = text.substring(0, startSpaces);
            String suffix = text.substring(text.length() - endSpaces);
            return prefix + res + suffix;
        } catch (Throwable t) {
            logw(TAG, "Error in applyGrammarHeuristics", t);
            return text;
        }
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
                                         int m, int n, List<DiffToken> out) {
        List<DiffToken> reversed = new ArrayList<>();
        int currI = m;
        int currJ = n;
        while (currI > 0 || currJ > 0) {
            if (currI > 0 && currJ > 0 && a.get(currI - 1).equals(b.get(currJ - 1))) {
                reversed.add(new DiffToken(a.get(currI - 1), DiffType.UNCHANGED));
                currI--;
                currJ--;
            } else if (currJ > 0 && (currI == 0 || dp[currI][currJ - 1] >= dp[currI - 1][currJ])) {
                reversed.add(new DiffToken(b.get(currJ - 1), DiffType.INSERTED));
                currJ--;
            } else if (currI > 0 && (currJ == 0 || dp[currI][currJ - 1] < dp[currI - 1][currJ])) {
                reversed.add(new DiffToken(a.get(currI - 1), DiffType.DELETED));
                currI--;
            }
        }
        Collections.reverse(reversed);
        out.addAll(reversed);
    }

    /**
     * Applies the corrected text to the active InputConnection.
     */
    public static boolean applyCorrection(InputMethodService ims, ProofreadResult result) {
        if (ims == null || result == null || result.correctedText == null || result.originalText == null) {
            return false;
        }

        InputConnection ic = ims.getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        String orig = result.originalText;
        String corr = result.correctedText;

        if (orig.equals(corr)) {
            return true;
        }

        try {
            ic.beginBatchEdit();
            try {
                ic.finishComposingText();
            } catch (Throwable ignored) {
            }

            // 0. Active selection match:
            CharSequence currentSel = ic.getSelectedText(0);
            if (currentSel != null && currentSel.length() > 0) {
                String selStr = currentSel.toString();
                if (selStr.equals(orig) || selStr.trim().equals(orig.trim()) || result.wasSelected) {
                    ic.commitText(corr, 1);
                    ic.endBatchEdit();
                    logw(TAG, "Applied correction via active selection: " + corr);
                    return true;
                }
            }

            // 1. Try to get absolute document offsets via ExtractedText
            try {
                ExtractedTextRequest req = new ExtractedTextRequest();
                req.hintMaxChars = 10000;
                req.hintMaxLines = 1000;
                ExtractedText et = ic.getExtractedText(req, 0);
                if (et != null && et.text != null) {
                    String fullDoc = et.text.toString();
                    int curPos = (et.selectionStart >= 0) ? et.startOffset + et.selectionStart : fullDoc.length();
                    curPos = Math.max(0, Math.min(curPos, fullDoc.length()));

                    // Case A: Full document replacement (when beforeLength == -1)
                    if (result.beforeLength == -1 && (fullDoc.equals(orig) || fullDoc.trim().equals(orig.trim()))) {
                        ic.setSelection(curPos, curPos);
                        int before = curPos;
                        int after = fullDoc.length() - curPos;
                        if (before > 0 || after > 0) {
                            ic.deleteSurroundingText(before, after);
                        }
                        ic.commitText(corr, 1);
                        ic.endBatchEdit();
                        logw(TAG, "Applied full document replacement via deleteSurroundingText(" + before + ", " + after + "): " + corr);
                        return true;
                    }

                    // Case B: Substring inside full document
                    int idx = fullDoc.indexOf(orig);
                    if (idx < 0) {
                        String origTrim = orig.trim();
                        idx = fullDoc.indexOf(origTrim);
                    }
                    if (idx >= 0) {
                        int targetEnd = idx + orig.length();
                        ic.setSelection(targetEnd, targetEnd);
                        ic.deleteSurroundingText(orig.length(), 0);
                        ic.commitText(corr, 1);
                        ic.endBatchEdit();
                        logw(TAG, "Applied substring replacement [" + idx + ", " + targetEnd + "] via deleteSurroundingText: " + corr);
                        return true;
                    }
                }
            } catch (Throwable t) {
                logw(TAG, "ExtractedText replacement attempt failed, falling back to relative context", t);
            }

            // 2. Relative context fallback (when beforeLength and afterLength match originalText)
            if (result.beforeLength >= 0 && result.afterLength >= 0
                    && (result.beforeLength + result.afterLength == orig.length())) {
                ic.deleteSurroundingText(result.beforeLength, result.afterLength);
                ic.commitText(corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied relative context replacement via deleteSurroundingText(" + result.beforeLength + ", " + result.afterLength + "): " + corr);
                return true;
            }

            CharSequence beforeCs = ic.getTextBeforeCursor(1500, 0);
            CharSequence afterCs = ic.getTextAfterCursor(600, 0);
            String before = beforeCs != null ? beforeCs.toString() : "";
            String after = afterCs != null ? afterCs.toString() : "";

            // Case 1: Cursor is right after originalText
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

            // Case 4: Trimmed match
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

            // Case 5: Verified stored scope match
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

            // Case 6: Case-insensitive match at end of before
            if (before.length() >= orig.length() && before.substring(before.length() - orig.length()).equalsIgnoreCase(orig)) {
                ic.deleteSurroundingText(orig.length(), 0);
                ic.commitText(corr, 1);
                ic.endBatchEdit();
                logw(TAG, "Applied correction via case-insensitive before.endsWith: " + corr);
                return true;
            }

            logw(TAG, "Aborted replacement: target text does not match cursor context (orig='" + orig + "'); preventing destructive deletion");
            ic.endBatchEdit();
            return false;
        } catch (Throwable t) {
            logw(TAG, "Failed to apply correction to InputConnection", t);
            try {
                ic.endBatchEdit();
            } catch (Throwable ignored) {}
            return false;
        }
    }
}
