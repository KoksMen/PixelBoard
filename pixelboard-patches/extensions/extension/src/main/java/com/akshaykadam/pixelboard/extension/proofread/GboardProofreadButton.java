/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillService;
import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillSettings;
import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarIndicatorOverlay;

import java.lang.ref.WeakReference;

/**
 * Pinned Quick Actions Bar mounted directly on the keyboard (SoftKeyboardView)
 * providing immediate 1-tap access to:
 * 1. Text proofreading & correction (✨ Проверить)
 * 2. Password & Autofill manager (🔑 Bitwarden)
 */
public final class GboardProofreadButton {
    private static final String TAG = "PixelBoardProofread";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private static WeakReference<ViewGroup> sCurrentBarRef = null;
    private static WeakReference<View> sProofreadButtonRef = null;
    private static WeakReference<View> sAutofillButtonRef = null;
    private static WeakReference<ViewGroup> sAttachedParentRef = null;

    private GboardProofreadButton() {
    }

    /**
     * Hook called from SoftKeyboardView.onAttachedToWindow() and onLayout()
     * to guarantee that the bar is pinned immediately when the keyboard is displayed.
     */
    public static void onSoftKeyboardViewAttached(View keyboardView) {
        attach(keyboardView);
    }

    /**
     * Attaches the Quick Actions Bar directly onto the keyboard view container.
     */
    public static void attach(View keyboardView) {
        if (keyboardView == null) return;
        Context context = keyboardView.getContext();

        boolean proofreadEnabled = GboardProofreadSettings.isEnabled(context);
        boolean autofillEnabled = GboardAutofillSettings.isEnabled(context);

        if (!proofreadEnabled && !autofillEnabled) {
            detach();
            return;
        }

        sMainHandler.post(() -> {
            try {
                // Select the best keyboard host ViewGroup
                ViewGroup host = findBestKeyboardHost(keyboardView);
                if (host == null) return;

                ViewGroup existingBar = sCurrentBarRef != null ? sCurrentBarRef.get() : null;
                ViewGroup attachedParent = sAttachedParentRef != null ? sAttachedParentRef.get() : null;

                if (existingBar != null && attachedParent == host && existingBar.getParent() == host) {
                    existingBar.bringToFront();
                    return; // Already attached to this host
                }

                if (existingBar != null && existingBar.getParent() instanceof ViewGroup) {
                    ((ViewGroup) existingBar.getParent()).removeView(existingBar);
                }

                LinearLayout bar = createBar(context, keyboardView, proofreadEnabled, autofillEnabled);
                if (bar == null) return;

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.TOP | Gravity.END;
                params.topMargin = dp(context, 4);
                params.rightMargin = dp(context, 8);

                host.addView(bar, params);
                bar.bringToFront();

                sCurrentBarRef = new WeakReference<>(bar);
                sAttachedParentRef = new WeakReference<>(host);

            } catch (Throwable t) {
                Log.w(TAG, "Failed to attach quick actions bar", t);
            }
        });
    }

    /**
     * Finds the most appropriate keyboard ViewGroup (SoftKeyboardView or KeyboardHolder).
     */
    private static ViewGroup findBestKeyboardHost(View view) {
        if (view instanceof ViewGroup) {
            // Check if view is SoftKeyboardView or has children
            ViewGroup vg = (ViewGroup) view;
            if (vg.getChildCount() > 0 || vg.getClass().getSimpleName().contains("SoftKeyboard")) {
                return vg;
            }
        }
        if (view.getParent() instanceof ViewGroup) {
            return (ViewGroup) view.getParent();
        }
        View root = view.getRootView();
        return (root instanceof ViewGroup) ? (ViewGroup) root : null;
    }

    /**
     * Reveals the proofread button when spacebar interaction occurs (if fast mode is disabled).
     */
    public static void showProofreadOnTouch(View keyboardView) {
        if (keyboardView == null) return;
        Context context = keyboardView.getContext();
        if (!GboardProofreadSettings.isEnabled(context)) return;

        sMainHandler.post(() -> {
            try {
                attach(keyboardView);
                View btn = sProofreadButtonRef != null ? sProofreadButtonRef.get() : null;
                if (btn != null && btn.getVisibility() != View.VISIBLE) {
                    btn.setVisibility(View.VISIBLE);
                    btn.setAlpha(0f);
                    ObjectAnimator.ofFloat(btn, "alpha", 0f, 1f).setDuration(160).start();
                }
            } catch (Throwable ignored) {
            }
        });
    }

    public static void detach() {
        sMainHandler.post(() -> {
            ViewGroup existing = sCurrentBarRef != null ? sCurrentBarRef.get() : null;
            if (existing != null && existing.getParent() instanceof ViewGroup) {
                ((ViewGroup) existing.getParent()).removeView(existing);
            }
            if (sCurrentBarRef != null) sCurrentBarRef.clear();
            if (sProofreadButtonRef != null) sProofreadButtonRef.clear();
            if (sAutofillButtonRef != null) sAutofillButtonRef.clear();
            if (sAttachedParentRef != null) sAttachedParentRef.clear();
        });
    }

    private static LinearLayout createBar(
            Context context, View keyboardView, boolean proofreadEnabled, boolean autofillEnabled) {
        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);

        int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
        boolean isDark = isColorDark(surfaceColor);

        // High contrast colors for pinned buttons
        int btnBgColor = isDark ? Color.parseColor("#E6282932") : Color.parseColor("#F2FFFFFF");
        int strokeColor = isDark ? Color.parseColor("#4DFFFFFF") : Color.parseColor("#33000000");

        // 1. Key Button: [ 🔑 ]
        if (autofillEnabled) {
            View autofillBtn = createAutofillButton(context, keyboardView, btnBgColor, strokeColor, isDark);
            bar.addView(autofillBtn);
            sAutofillButtonRef = new WeakReference<>(autofillBtn);
        }

        // 2. Proofread Button: [ ✨ Проверить ]
        if (proofreadEnabled) {
            boolean alwaysVisible = GboardProofreadSettings.isAlwaysVisible(context);
            View proofreadBtn = createProofreadButton(context, keyboardView, btnBgColor, strokeColor, isDark);
            if (!alwaysVisible) {
                proofreadBtn.setVisibility(View.GONE);
            }
            if (autofillEnabled) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) proofreadBtn.getLayoutParams();
                if (lp != null) {
                    lp.leftMargin = dp(context, 6);
                }
            }
            bar.addView(proofreadBtn);
            sProofreadButtonRef = new WeakReference<>(proofreadBtn);
        }

        return bar;
    }

    private static View createAutofillButton(
            Context context, View keyboardView, int bgColor, int strokeColor, boolean isDark) {
        LinearLayout btn = new LinearLayout(context);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);
        btn.setFocusable(true);

        int padH = dp(context, 10);
        int padV = dp(context, 5);
        btn.setPadding(padH, padV, padH, padV);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(context, 16));
        bg.setColor(bgColor);
        bg.setStroke(dp(context, 1.2f), strokeColor);
        btn.setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setElevation(dp(context, 5));
        }

        TextView keyIcon = new TextView(context);
        keyIcon.setText("🔑");
        keyIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        keyIcon.setGravity(Gravity.CENTER);
        btn.addView(keyIcon);

        btn.setOnClickListener(v -> {
            try {
                btn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable ignored) {
            }
            GboardAutofillService.triggerAutofill(keyboardView);
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(context, 32));
        btn.setLayoutParams(lp);

        return btn;
    }

    private static View createProofreadButton(
            Context context, View keyboardView, int bgColor, int strokeColor, boolean isDark) {
        LinearLayout btn = new LinearLayout(context);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);
        btn.setFocusable(true);

        int padH = dp(context, 10);
        int padV = dp(context, 5);
        btn.setPadding(padH, padV, padH, padV);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(context, 16));
        bg.setColor(bgColor);
        bg.setStroke(dp(context, 1.2f), strokeColor);
        btn.setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn.setElevation(dp(context, 5));
        }

        TextView iconText = new TextView(context);
        iconText.setText("✨");
        iconText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        iconText.setGravity(Gravity.CENTER);
        btn.addView(iconText);

        TextView labelText = new TextView(context);
        labelText.setText("Проверить");
        labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        labelText.setTypeface(Typeface.DEFAULT_BOLD);
        labelText.setTextColor(isDark ? Color.WHITE : Color.parseColor("#1D1B20"));
        labelText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = dp(context, 4);
        labelText.setLayoutParams(labelParams);
        btn.addView(labelText);

        btn.setOnClickListener(v -> {
            try {
                btn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable ignored) {
            }
            GboardProofreadService.checkAndShowDiff(keyboardView);
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(context, 32));
        btn.setLayoutParams(lp);

        return btn;
    }

    private static boolean isColorDark(int color) {
        double darkness = 1.0 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255.0;
        return darkness >= 0.45;
    }

    private static int dp(Context context, float dp) {
        if (context == null || context.getResources() == null) {
            return Math.round(dp * 2f);
        }
        DisplayMetrics m = context.getResources().getDisplayMetrics();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, m));
    }
}
