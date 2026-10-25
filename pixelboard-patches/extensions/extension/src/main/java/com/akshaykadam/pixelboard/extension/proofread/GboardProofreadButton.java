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

import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillService;
import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillSettings;
import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarIndicatorOverlay;

import java.lang.ref.WeakReference;

/**
 * Pinned toolbar icon buttons in Gboard's top header strip:
 * 1. Autofill key button (🔑 Bitwarden) - native vector icon button
 * 2. Text proofread button (✨ Проверка) - native vector icon button
 *
 * Designed to look and feel identical to Gboard's native 4-squares and microphone buttons.
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
     * Vector Key Icon View (drawn via Canvas for crisp, theme-tinted rendering).
     */
    public static final class KeyIconView extends View {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public KeyIconView(Context context) {
            super(context);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
        }

        public void setColor(int color) {
            mPaint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            float cx = w * 0.5f;
            float cy = h * 0.5f;
            float scale = Math.min(w, h) / 36f;
            mPaint.setStrokeWidth(2.2f * scale);

            // Key head (circle) on left
            float r = 5.2f * scale;
            float headX = cx - 5.5f * scale;
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(headX, cy, r, mPaint);

            // Key shaft extending right
            float shaftStartX = headX + r;
            float shaftEndX = cx + 9.5f * scale;
            canvas.drawLine(shaftStartX, cy, shaftEndX, cy, mPaint);

            // Key teeth pointing down
            float tooth1X = cx + 4.5f * scale;
            float tooth2X = cx + 8.5f * scale;
            float toothLen = 4.0f * scale;
            canvas.drawLine(tooth1X, cy, tooth1X, cy + toothLen, mPaint);
            canvas.drawLine(tooth2X, cy, tooth2X, cy + toothLen, mPaint);
        }
    }

    /**
     * Vector Sparkles / Magic Wand Icon View (drawn via Canvas for crisp, theme-tinted rendering).
     */
    public static final class SparkleIconView extends View {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path mMainSparkle = new Path();
        private final Path mSmallSparkle = new Path();

        public SparkleIconView(Context context) {
            super(context);
            mPaint.setStyle(Paint.Style.FILL);
        }

        public void setColor(int color) {
            mPaint.setColor(color);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            float cx = w * 0.44f;
            float cy = h * 0.54f;
            float s = Math.min(w, h) * 0.30f; // main sparkle radius

            // 4-pointed curved star (main sparkle)
            mMainSparkle.reset();
            mMainSparkle.moveTo(cx, cy - s);
            mMainSparkle.quadTo(cx, cy, cx + s, cy);
            mMainSparkle.quadTo(cx, cy, cx, cy + s);
            mMainSparkle.quadTo(cx, cy, cx - s, cy);
            mMainSparkle.quadTo(cx, cy, cx, cy - s);
            mMainSparkle.close();
            canvas.drawPath(mMainSparkle, mPaint);

            // Secondary small sparkle on top-right
            float sx = cx + s * 0.78f;
            float sy = cy - s * 0.70f;
            float sm = s * 0.40f;
            mSmallSparkle.reset();
            mSmallSparkle.moveTo(sx, sy - sm);
            mSmallSparkle.quadTo(sx, sy, sx + sm, sy);
            mSmallSparkle.quadTo(sx, sy, sx, sy + sm);
            mSmallSparkle.quadTo(sx, sy, sx - sm, sy);
            mSmallSparkle.quadTo(sx, sy, sx, sy - sm);
            mSmallSparkle.close();
            canvas.drawPath(mSmallSparkle, mPaint);
        }
    }

    /**
     * Hook called from SoftKeyboardView.onAttachedToWindow() and onLayout()
     * to guarantee that the buttons are pinned immediately as soon as the keyboard appears.
     */
    public static void onSoftKeyboardViewAttached(View keyboardView) {
        attach(keyboardView);
    }

    /**
     * Attaches the Quick Actions Bar into the keyboard header row.
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
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                params.rightMargin = dp(context, 4);

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
     * Finds the Gboard header view holder or the header SoftKeyboardView.
     */
    private static ViewGroup findBestKeyboardHost(View view) {
        if (view == null) return null;
        try {
            Context context = view.getContext();
            String pkg = context.getPackageName();

            // 1. Traverse up to find keyboard_header_view_holder
            View curr = view;
            while (curr != null) {
                if (curr instanceof ViewGroup) {
                    ViewGroup vg = (ViewGroup) curr;
                    int headerId = vg.getResources().getIdentifier("keyboard_header_view_holder", "id", pkg);
                    if (headerId != 0) {
                        View header = vg.findViewById(headerId);
                        if (header instanceof ViewGroup) {
                            ViewGroup headerGroup = (ViewGroup) header;
                            for (int i = 0; i < headerGroup.getChildCount(); i++) {
                                View child = headerGroup.getChildAt(i);
                                if (child instanceof ViewGroup && child.getClass().getSimpleName().contains("SoftKeyboard")) {
                                    return (ViewGroup) child;
                                }
                            }
                            return headerGroup;
                        }
                    }
                    int holderId = vg.getResources().getIdentifier("keyboard_holder", "id", pkg);
                    if (holderId != 0) {
                        View holder = vg.findViewById(holderId);
                        if (holder instanceof ViewGroup) {
                            return (ViewGroup) holder;
                        }
                    }
                }
                if (curr.getParent() instanceof View) {
                    curr = (View) curr.getParent();
                } else {
                    break;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Fallback: If view itself is header SoftKeyboardView
        if (view instanceof ViewGroup && view.getHeight() > 0 && view.getHeight() < dp(view.getContext(), 65)) {
            return (ViewGroup) view;
        }

        // 3. Fallback to view itself if it is a ViewGroup
        if (view instanceof ViewGroup) {
            return (ViewGroup) view;
        }

        View root = view.getRootView();
        return (root instanceof ViewGroup) ? (ViewGroup) root : null;
    }

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

        // Native Gboard icon colors: light gray on dark theme, dark gray on light theme
        int iconColor = isDark ? Color.parseColor("#E2E2E9") : Color.parseColor("#44474E");

        // Subtle circular touch target background
        int touchBgColor = isDark ? Color.parseColor("#1FFFFFFF") : Color.parseColor("#14000000");

        // 1. Key Button: [ 🔑 ] native toolbar icon button
        if (autofillEnabled) {
            View autofillBtn = createToolbarIconButton(context, keyboardView, new KeyIconView(context), iconColor, touchBgColor, true);
            bar.addView(autofillBtn);
            sAutofillButtonRef = new WeakReference<>(autofillBtn);
        }

        // 2. Proofread Button: [ ✨ ] native toolbar icon button
        if (proofreadEnabled) {
            boolean alwaysVisible = GboardProofreadSettings.isAlwaysVisible(context);
            View proofreadBtn = createToolbarIconButton(context, keyboardView, new SparkleIconView(context), iconColor, touchBgColor, false);
            if (!alwaysVisible) {
                proofreadBtn.setVisibility(View.GONE);
            }
            if (autofillEnabled) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) proofreadBtn.getLayoutParams();
                if (lp != null) {
                    lp.leftMargin = dp(context, 2);
                }
            }
            bar.addView(proofreadBtn);
            sProofreadButtonRef = new WeakReference<>(proofreadBtn);
        }

        return bar;
    }

    /**
     * Creates a native Gboard-style circular icon button (matching 4-squares and microphone buttons).
     */
    private static View createToolbarIconButton(
            Context context, View keyboardView, View iconView, int iconColor, int touchBgColor, boolean isAutofill) {
        FrameLayout buttonFrame = new FrameLayout(context);
        buttonFrame.setClickable(true);
        buttonFrame.setFocusable(true);

        int buttonSize = dp(context, 38);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(buttonSize, buttonSize);
        frameParams.gravity = Gravity.CENTER_VERTICAL;
        buttonFrame.setLayoutParams(frameParams);

        // Circular background with subtle tint and borderless ripple
        GradientDrawable normalBg = new GradientDrawable();
        normalBg.setShape(GradientDrawable.OVAL);
        normalBg.setColor(touchBgColor);
        buttonFrame.setBackground(normalBg);

        // Set selectable borderless ripple if available
        try {
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                buttonFrame.setForeground(context.getDrawable(outValue.resourceId));
            }
        } catch (Throwable ignored) {
        }

        // Add the vector icon inside the button
        int iconSize = dp(context, 22);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
        iconParams.gravity = Gravity.CENTER;
        iconView.setLayoutParams(iconParams);

        if (iconView instanceof KeyIconView) {
            ((KeyIconView) iconView).setColor(iconColor);
        } else if (iconView instanceof SparkleIconView) {
            ((SparkleIconView) iconView).setColor(iconColor);
        }
        buttonFrame.addView(iconView);

        buttonFrame.setOnClickListener(v -> {
            try {
                buttonFrame.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable ignored) {
            }
            if (isAutofill) {
                GboardAutofillService.triggerAutofill(keyboardView);
            } else {
                GboardProofreadService.checkAndShowDiff(keyboardView);
            }
        });

        return buttonFrame;
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
