/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarIndicatorOverlay;

import java.lang.ref.WeakReference;

/**
 * Trigger button displayed on the keyboard header/strip to run text proofreading and correction.
 */
public final class GboardProofreadButton {
    private static final String TAG = "PixelBoardProofread";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static WeakReference<View> sCurrentButtonRef = null;

    private GboardProofreadButton() {
    }

    /**
     * Attaches the trigger button to the keyboard container if not already attached.
     */
    public static void attach(View keyboardView) {
        if (keyboardView == null) return;
        Context context = keyboardView.getContext();
        if (!GboardProofreadSettings.isEnabled(context)) {
            detach();
            return;
        }

        sMainHandler.post(() -> {
            try {
                View root = keyboardView.getRootView();
                if (!(root instanceof ViewGroup)) return;
                ViewGroup container = (ViewGroup) root;

                View existing = sCurrentButtonRef != null ? sCurrentButtonRef.get() : null;
                if (existing != null && existing.getParent() == container) {
                    return; // Already attached
                }

                if (existing != null && existing.getParent() instanceof ViewGroup) {
                    ((ViewGroup) existing.getParent()).removeView(existing);
                }

                LinearLayout btn = createButton(context, keyboardView);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.TOP | Gravity.END;
                params.topMargin = dp(context, 4);
                params.rightMargin = dp(context, 10);

                container.addView(btn, params);
                sCurrentButtonRef = new WeakReference<>(btn);

            } catch (Throwable t) {
                Log.w(TAG, "Failed to attach proofread button", t);
            }
        });
    }

    public static void detach() {
        sMainHandler.post(() -> {
            View existing = sCurrentButtonRef != null ? sCurrentButtonRef.get() : null;
            if (existing != null && existing.getParent() instanceof ViewGroup) {
                ((ViewGroup) existing.getParent()).removeView(existing);
            }
            if (sCurrentButtonRef != null) {
                sCurrentButtonRef.clear();
            }
        });
    }

    private static LinearLayout createButton(Context context, View keyboardView) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setClickable(true);
        layout.setFocusable(true);

        int padH = dp(context, 9);
        int padV = dp(context, 5);
        layout.setPadding(padH, padV, padH, padV);

        int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
        boolean isDark = isColorDark(surfaceColor);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(context, 16));
        bg.setColor(surfaceColor);
        bg.setStroke(dp(context, 1), isDark ? Color.parseColor("#33FFFFFF") : Color.parseColor("#1F000000"));
        layout.setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layout.setElevation(dp(context, 4));
        }

        TextView iconText = new TextView(context);
        iconText.setText("✨");
        iconText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        iconText.setGravity(Gravity.CENTER);
        layout.addView(iconText);

        TextView labelText = new TextView(context);
        labelText.setText("Проверить");
        labelText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        labelText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        labelText.setTextColor(isDark ? Color.WHITE : Color.parseColor("#1D1B20"));
        labelText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = dp(context, 4);
        labelText.setLayoutParams(labelParams);
        layout.addView(labelText);

        layout.setOnClickListener(v -> {
            try {
                layout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable ignored) {
            }
            GboardProofreadService.checkAndShowDiff(keyboardView);
        });

        return layout;
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
