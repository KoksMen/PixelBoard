/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.spacebar;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Runtime controller for switching keyboard languages via horizontal spacebar gestures.
 *
 * Intercepts touch gestures on the keyboard container (SoftKeyboardView) while preserving
 * standard Gboard typing, tap, and long-press functionality:
 * - Swipe right (left-to-right): Switch forward to next language/subtype.
 * - Swipe left (right-to-left): Switch backward to previous language/subtype.
 * - Tap: types a space character as normal.
 * - Long-press: triggers language selector or custom actions as normal.
 */
public final class GboardSpacebarSwipeRuntime {
    public static final String TAG = "PixelBoardSpacebar";
    public static final int SPACEBAR_RES_ID = 0x7f0b05b3; // key_pos_space

    private static final float SWIPE_THRESHOLD_DP = 35.0f;
    private static final float SWIPE_DIRECTION_RATIO = 1.25f;
    private static final long MAX_SWIPE_DURATION_MS = 280L;

    private static WeakReference<InputMethodService> sCurrentIms = null;

    private static float startX;
    private static float startY;
    private static long startTime;
    private static boolean isTracking = false;
    private static boolean swipeTriggered = false;
    private static boolean isSendingCancel = false;

    // For testing and mock verification
    public static boolean forceEnabledForTesting = false;
    public static boolean lastSwipeWasForward = false;
    public static int swipeCountForTesting = 0;

    private GboardSpacebarSwipeRuntime() {
    }

    /**
     * Registers the active LatinIME InputMethodService instance.
     */
    public static void registerInputMethodService(InputMethodService ims) {
        try {
            if (ims != null) {
                sCurrentIms = new WeakReference<>(ims);
                Log.i(TAG, "Registered InputMethodService: " + ims.getClass().getName());
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Checks whether the given view is the spacebar soft key.
     */
    public static boolean isSpacebarView(View view) {
        if (view == null) {
            return false;
        }
        int id = view.getId();
        if (id == SPACEBAR_RES_ID) {
            return true;
        }
        if (id != View.NO_ID && id != 0) {
            try {
                Context context = view.getContext();
                if (context != null) {
                    String entryName = context.getResources().getResourceEntryName(id);
                    if ("key_pos_space".equals(entryName)
                            || "key_pos_spacebar".equals(entryName)
                            || entryName.contains("space")) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        Object tag = view.getTag();
        if (tag instanceof CharSequence) {
            String tagStr = tag.toString().toLowerCase();
            if (tagStr.contains("space")) {
                return true;
            }
        }
        CharSequence desc = view.getContentDescription();
        if (desc != null) {
            String descStr = desc.toString().toLowerCase();
            if (descStr.contains("space") || descStr.contains("пробел")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the spacebar view within the keyboard view hierarchy.
     */
    public static View findSpacebarView(View root) {
        if (root == null) {
            return null;
        }
        try {
            View direct = root.findViewById(SPACEBAR_RES_ID);
            if (direct != null && direct.getVisibility() == View.VISIBLE) {
                return direct;
            }
        } catch (Throwable ignored) {
        }
        if (root instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) root;
            int count = vg.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = vg.getChildAt(i);
                if (isSpacebarView(child) && child.getVisibility() == View.VISIBLE) {
                    return child;
                }
                if (child instanceof ViewGroup) {
                    View nested = findSpacebarView(child);
                    if (nested != null) {
                        return nested;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks whether the touch event falls within the spacebar view bounds.
     */
    private static boolean isEventInsideSpacebar(View keyboardView, View spacebar, MotionEvent event) {
        if (spacebar == null || spacebar.getVisibility() != View.VISIBLE) {
            return false;
        }

        // 1. Relative bounds check within keyboardView
        try {
            Rect r = new Rect();
            spacebar.getDrawingRect(r);
            if (keyboardView instanceof ViewGroup) {
                ((ViewGroup) keyboardView).offsetDescendantRectToMyCoords(spacebar, r);
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (r.contains(x, y)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Global screen coordinates check
        try {
            int[] loc = new int[2];
            spacebar.getLocationOnScreen(loc);
            float rx = event.getRawX();
            float ry = event.getRawY();
            if (rx >= loc[0] && rx <= (loc[0] + spacebar.getWidth())
                    && ry >= loc[1] && ry <= (loc[1] + spacebar.getHeight())) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    /**
     * Main touch dispatcher hook for SoftKeyboardView.
     *
     * @param keyboardView the SoftKeyboardView instance
     * @param event the MotionEvent
     * @return true if the event was consumed by the swipe gesture; false to allow standard Gboard processing
     */
    public static boolean handleSoftKeyboardViewTouchEvent(View keyboardView, MotionEvent event) {
        try {
            if (keyboardView == null || event == null || isSendingCancel) {
                return false;
            }
            Context context = keyboardView.getContext();
            if (!forceEnabledForTesting && !GboardSpacebarSettings.isEnabled(context)) {
                return false;
            }

            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    // Attach proofread trigger button if enabled
                    com.akshaykadam.pixelboard.extension.proofread.GboardProofreadButton.attach(keyboardView);

                    View spacebar = findSpacebarView(keyboardView);
                    if (spacebar == null) {
                        // This view does not contain a spacebar (e.g. candidates/suggestions bar)
                        isTracking = false;
                        swipeTriggered = false;
                        return false;
                    }

                    boolean onSpace = isEventInsideSpacebar(keyboardView, spacebar, event);
                    if (onSpace) {
                        startX = event.getX();
                        startY = event.getY();
                        startTime = SystemClock.uptimeMillis();
                        isTracking = true;
                        swipeTriggered = false;
                        Log.i(TAG, "Spacebar touch DOWN tracked at (" + startX + ", " + startY + ")");
                    } else {
                        isTracking = false;
                        swipeTriggered = false;
                    }
                    // Return false: let Gboard receive ACTION_DOWN for key highlight & tap preparation
                    return false;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (!isTracking) {
                        return false;
                    }
                    if (swipeTriggered) {
                        // Swallow all further move events of this gesture
                        return true;
                    }

                    long elapsed = SystemClock.uptimeMillis() - startTime;
                    if (elapsed > MAX_SWIPE_DURATION_MS) {
                        // Spacebar held longer than 280ms: release tracking to Gboard for native cursor scrubbing!
                        isTracking = false;
                        return false;
                    }

                    float dx = event.getX() - startX;
                    float dy = event.getY() - startY;
                    float absDx = Math.abs(dx);
                    float absDy = Math.abs(dy);
                    float thresholdPx = dpToPx(context, SWIPE_THRESHOLD_DP);

                    // 1. Check for vertical swipe UP: triggers proofreading & correction!
                    if (dy <= -thresholdPx && absDy > SWIPE_DIRECTION_RATIO * absDx) {
                        swipeTriggered = true;
                        Log.i(TAG, "SWIPE UP TRIGGERED: proofread requested dy=" + dy + " elapsed=" + elapsed);
                        performHapticFeedback(keyboardView);
                        sendCancelToKeyboardView(keyboardView, event);
                        com.akshaykadam.pixelboard.extension.proofread.GboardProofreadService.checkAndShowDiff(keyboardView);
                        return true;
                    }

                    // 2. Check for horizontal swipe: switches language!
                    if (absDx >= thresholdPx && absDx > SWIPE_DIRECTION_RATIO * absDy) {
                        swipeTriggered = true;
                        boolean forward = dx > 0;
                        lastSwipeWasForward = forward;
                        swipeCountForTesting++;
                        Log.i(TAG, "SWIPE TRIGGERED: forward=" + forward + " dx=" + dx + " dy=" + dy + " elapsed=" + elapsed);

                        // 1. Switch language forward or backward
                        switchLanguage(keyboardView, forward);

                        // 2. Perform haptic feedback
                        performHapticFeedback(keyboardView);

                        // 3. Send CANCEL to Gboard to cancel space tap and cursor scrub
                        sendCancelToKeyboardView(keyboardView, event);

                        return true;
                    }
                    return false;
                }

                case MotionEvent.ACTION_UP: {
                    boolean wasSwiped = swipeTriggered;
                    isTracking = false;
                    swipeTriggered = false;
                    if (wasSwiped) {
                        Log.i(TAG, "ACTION_UP consumed after swipe: no space committed");
                        return true;
                    }
                    // Normal tap: let Gboard receive ACTION_UP and commit the space character!
                    return false;
                }

                case MotionEvent.ACTION_CANCEL: {
                    isTracking = false;
                    swipeTriggered = false;
                    return false;
                }

                default:
                    return false;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Exception in handleSoftKeyboardViewTouchEvent", t);
            return false;
        }
    }

    /**
     * Legacy hook for SoftKeyView (kept for compatibility, returns false so Gboard is unhindered).
     */
    public static boolean handleTouchEvent(View view, MotionEvent event) {
        return false;
    }

    /**
     * Legacy hook for SoftKeyView onAttached (kept empty to not overwrite Gboard touch listener).
     */
    public static void onAttached(View view) {
    }

    public static void onSoftKeyViewAttached(View view) {
    }

    /**
     * Sends an ACTION_CANCEL event to the keyboard view to cleanly abort pending space/cursor actions.
     */
    private static void sendCancelToKeyboardView(View keyboardView, MotionEvent source) {
        if (keyboardView == null || source == null || isSendingCancel) {
            return;
        }
        try {
            isSendingCancel = true;
            MotionEvent cancel = MotionEvent.obtain(
                    source.getDownTime(),
                    source.getEventTime(),
                    MotionEvent.ACTION_CANCEL,
                    source.getX(),
                    source.getY(),
                    source.getMetaState()
            );
            keyboardView.dispatchTouchEvent(cancel);
            cancel.recycle();
        } catch (Throwable t) {
            Log.w(TAG, "Failed to send cancel event to keyboard view", t);
        } finally {
            isSendingCancel = false;
        }
    }

    /**
     * Switches keyboard language / subtype forward or backward.
     */
    public static boolean switchLanguage(View view, boolean forward) {
        Context context = view != null ? view.getContext() : null;
        InputMethodService ims = sCurrentIms != null ? sCurrentIms.get() : null;
        if (ims == null && view != null) {
            ims = findInputMethodService(view);
        }

        Log.i(TAG, "switchLanguage: forward=" + forward + ", ims=" + ims);

        Context ctx = (context != null) ? context : (ims != null ? ims : null);

        // Pre-resolve or determine target subtype from enabled subtypes of Gboard
        InputMethodSubtype targetSubtype = null;
        InputMethodInfo currentImi = null;
        InputMethodManager imm = null;
        if (ctx != null) {
            try {
                imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    String pkg = ctx.getPackageName();
                    List<InputMethodInfo> imis = imm.getEnabledInputMethodList();
                    if (imis != null) {
                        for (InputMethodInfo imi : imis) {
                            if (imi.getPackageName().equals(pkg)) {
                                currentImi = imi;
                                break;
                            }
                        }
                    }
                    if (currentImi != null) {
                        List<InputMethodSubtype> subtypes = imm.getEnabledInputMethodSubtypeList(currentImi, true);
                        if (subtypes != null && subtypes.size() > 1) {
                            InputMethodSubtype current = imm.getCurrentInputMethodSubtype();
                            int currentIndex = -1;
                            if (current != null) {
                                for (int i = 0; i < subtypes.size(); i++) {
                                    if (subtypes.get(i).hashCode() == current.hashCode()
                                            || subtypes.get(i).equals(current)) {
                                        currentIndex = i;
                                        break;
                                    }
                                }
                            }
                            int targetIndex;
                            if (forward) {
                                targetIndex = (currentIndex + 1) % subtypes.size();
                            } else {
                                targetIndex = (currentIndex <= 0) ? subtypes.size() - 1 : currentIndex - 1;
                            }
                            targetSubtype = subtypes.get(targetIndex);
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Error calculating targetSubtype", t);
            }
        }

        // Strategy 1: InputMethodService direct API (highest priority for forward)
        if (ims != null) {
            try {
                if (forward) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (ims.switchToNextInputMethod(true)) {
                            Log.i(TAG, "Strategy 1 (ims.switchToNextInputMethod(true)) succeeded");
                            showFeedback(view, ctx, forward, targetSubtype);
                            return true;
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Strategy 1 failed", t);
            }
        }

        // Strategy 2: Explicit Subtype Switch via IMS or IMM (supports forward & backward reliably)
        if (currentImi != null && targetSubtype != null) {
            try {
                if (ims != null) {
                    ims.switchInputMethod(currentImi.getId(), targetSubtype);
                    Log.i(TAG, "Strategy 2 (ims.switchInputMethod) succeeded: " + targetSubtype);
                    showFeedback(view, ctx, forward, targetSubtype);
                    return true;
                } else if (imm != null) {
                    IBinder token = (view != null) ? view.getWindowToken() : null;
                    if (token == null && ims != null && ims.getWindow() != null && ims.getWindow().getWindow() != null) {
                        token = ims.getWindow().getWindow().getAttributes().token;
                    }
                    if (token != null) {
                        imm.setInputMethodAndSubtype(token, currentImi.getId(), targetSubtype);
                        Log.i(TAG, "Strategy 2 (imm.setInputMethodAndSubtype) succeeded: " + targetSubtype);
                        showFeedback(view, ctx, forward, targetSubtype);
                        return true;
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Strategy 2 failed", t);
            }
        }

        // Strategy 3: InputMethodManager token API
        if (ctx != null && imm != null) {
            IBinder token = (view != null) ? view.getWindowToken() : null;
            if (token == null && ims != null && ims.getWindow() != null && ims.getWindow().getWindow() != null) {
                token = ims.getWindow().getWindow().getAttributes().token;
            }

            if (token != null) {
                try {
                    if (forward) {
                        if (imm.switchToNextInputMethod(token, true)) {
                            Log.i(TAG, "Strategy 3 (imm.switchToNextInputMethod) succeeded");
                            showFeedback(view, ctx, forward, targetSubtype);
                            return true;
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        try {
                            Method prevMethod = InputMethodManager.class.getMethod("switchToPreviousInputMethod", IBinder.class);
                            Object res = prevMethod.invoke(imm, token);
                            if (Boolean.TRUE.equals(res)) {
                                Log.i(TAG, "Strategy 3 (imm.switchToPreviousInputMethod) succeeded");
                                showFeedback(view, ctx, forward, targetSubtype);
                                return true;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Strategy 3 failed", t);
                }
            }
        }

        // Strategy 4: Fallback for backward on IMS
        if (!forward && ims != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (ims.switchToPreviousInputMethod()) {
                    Log.i(TAG, "Strategy 4 (ims.switchToPreviousInputMethod) succeeded");
                    showFeedback(view, ctx, forward, targetSubtype);
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        // Show indicator overlay feedback even if system call fallback occurs
        showFeedback(view, ctx, forward, targetSubtype);
        return false;
    }

    /**
     * Traverses the Context wrapper hierarchy to find the enclosing InputMethodService.
     */
    public static InputMethodService findInputMethodService(View view) {
        if (view == null) {
            return null;
        }
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof InputMethodService) {
                return (InputMethodService) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static void performHapticFeedback(View view) {
        if (view == null) return;
        try {
            if (!view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } catch (Throwable t) {
            try {
                Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    v.vibrate(25);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Displays the floating Material 3 pill overlay showing the target language and directional arrows.
     * Completely replaces standard system Toasts.
     */
    private static void showFeedback(View view, Context context, boolean forward, InputMethodSubtype subtype) {
        if (view == null) {
            return;
        }
        try {
            InputMethodService ims = sCurrentIms != null ? sCurrentIms.get() : null;
            String langName = resolveLanguageName(context, ims, subtype, forward);
            GboardSpacebarIndicatorOverlay.show(view, forward, langName);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to show feedback overlay", t);
        }
    }

    /**
     * Resolves a clean, human-readable language label (e.g. "Русский", "English").
     */
    public static String resolveLanguageName(Context context, InputMethodService ims, InputMethodSubtype subtype, boolean forward) {
        Context ctx = context != null ? context : (ims != null ? ims : null);
        if (subtype != null && ctx != null) {
            String name = resolveSubtypeName(ctx, subtype);
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        if (ctx != null) {
            try {
                InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    InputMethodSubtype current = imm.getCurrentInputMethodSubtype();
                    if (current != null) {
                        String name = resolveSubtypeName(ctx, current);
                        if (name != null && !name.isEmpty()) {
                            return name;
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return forward ? "Следующий" : "Предыдущий";
    }

    private static String resolveSubtypeName(Context context, InputMethodSubtype subtype) {
        if (subtype == null) return null;
        try {
            if (context != null) {
                CharSequence displayName = subtype.getDisplayName(
                        context, context.getPackageName(), context.getApplicationInfo());
                if (displayName != null && displayName.length() > 0) {
                    String str = displayName.toString().trim();
                    int paren = str.indexOf('(');
                    if (paren > 0) {
                        str = str.substring(0, paren).trim();
                    }
                    if (!str.isEmpty()) {
                        return str;
                    }
                }
            }
            String localeStr = subtype.getLocale();
            if (localeStr != null && !localeStr.isEmpty()) {
                java.util.Locale loc = java.util.Locale.forLanguageTag(localeStr.replace('_', '-'));
                String disp = loc.getDisplayLanguage(java.util.Locale.getDefault());
                if (disp != null && !disp.isEmpty()) {
                    return Character.toUpperCase(disp.charAt(0)) + disp.substring(1);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static float dpToPx(Context context, float dp) {
        if (context == null || context.getResources() == null) {
            return dp * 2.0f;
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static void resetForTesting() {
        startX = 0;
        startY = 0;
        startTime = 0;
        isTracking = false;
        swipeTriggered = false;
        forceEnabledForTesting = false;
        lastSwipeWasForward = false;
        swipeCountForTesting = 0;
    }
}
