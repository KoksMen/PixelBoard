/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.spacebar;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Floating Material 3 pill overlay showing the active language and directional indicators
 * during horizontal spacebar swipe gestures (similar to Yandex Keyboard).
 *
 * Automatically adopts system dynamic colors (Monet / Material You) and renders vector chevrons.
 */
public final class GboardSpacebarIndicatorOverlay {
    private static final String TAG = "PixelBoardSpacebar";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private static WeakReference<View> sCurrentPillRef = null;
    private static Runnable sDismissRunnable = null;

    private GboardSpacebarIndicatorOverlay() {
    }

    /**
     * Custom vector view for drawing crisp directional chevron arrows (‹ and ›)
     * without any font fallback or charset encoding dependency.
     */
    public static final class ChevronView extends View {
        private final boolean mIsRight;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path mPath = new Path();

        public ChevronView(Context context, boolean isRight) {
            super(context);
            mIsRight = isRight;
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeWidth(dp(context, 2.4f));
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

            mPath.reset();
            float padX = w * 0.22f;
            float padY = h * 0.20f;
            float midY = h * 0.5f;

            if (mIsRight) {
                // Pointing right ›
                mPath.moveTo(padX, padY);
                mPath.lineTo(w - padX, midY);
                mPath.lineTo(padX, h - padY);
            } else {
                // Pointing left ‹
                mPath.moveTo(w - padX, padY);
                mPath.lineTo(padX, midY);
                mPath.lineTo(w - padX, h - padY);
            }
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * Displays or updates the floating language switch indicator above the spacebar.
     *
     * @param keyboardView the SoftKeyboardView instance
     * @param forward true if swiping left-to-right (forward); false if swiping right-to-left
     * @param languageName the name of the newly selected language (e.g. "Русский", "English")
     */
    public static void show(View keyboardView, boolean forward, String languageName) {
        if (keyboardView == null) {
            return;
        }
        sMainHandler.post(() -> showInternal(keyboardView, forward, languageName));
    }

    private static void showInternal(View keyboardView, boolean forward, String languageName) {
        try {
            View root = keyboardView.getRootView();
            if (!(root instanceof ViewGroup)) {
                return;
            }
            ViewGroup container = (ViewGroup) root;
            Context context = keyboardView.getContext();

            // Cancel any pending dismissal
            if (sDismissRunnable != null) {
                sMainHandler.removeCallbacks(sDismissRunnable);
                sDismissRunnable = null;
            }

            // Resolve system dynamic theme colors
            int surfaceColor = resolveSurfaceColor(context);
            boolean isDarkSurface = isColorDark(surfaceColor);
            int textColor = isDarkSurface ? Color.WHITE : Color.parseColor("#1D1B20");
            int activeColor = resolveAccentColor(context, isDarkSurface);
            int inactiveColor = isDarkSurface ? Color.parseColor("#80FFFFFF") : Color.parseColor("#801D1B20");
            int strokeColor = isDarkSurface ? Color.parseColor("#28FFFFFF") : Color.parseColor("#1F000000");

            View existingPill = (sCurrentPillRef != null) ? sCurrentPillRef.get() : null;
            LinearLayout pillLayout = null;
            ChevronView leftArrow = null;
            TextView langText = null;
            ChevronView rightArrow = null;
            LinearLayout contentRow = null;

            if (existingPill instanceof LinearLayout && existingPill.getParent() == container) {
                LinearLayout candidate = (LinearLayout) existingPill;
                View child0 = candidate.getChildCount() > 0 ? candidate.getChildAt(0) : null;
                if (child0 instanceof LinearLayout && ((LinearLayout) child0).getChildCount() >= 3) {
                    View c0 = ((LinearLayout) child0).getChildAt(0);
                    View c1 = ((LinearLayout) child0).getChildAt(1);
                    View c2 = ((LinearLayout) child0).getChildAt(2);
                    if (c0 instanceof ChevronView && c1 instanceof TextView && c2 instanceof ChevronView) {
                        pillLayout = candidate;
                        contentRow = (LinearLayout) child0;
                        pillLayout.animate().cancel();
                        contentRow.animate().cancel();
                        leftArrow = (ChevronView) c0;
                        langText = (TextView) c1;
                        rightArrow = (ChevronView) c2;
                    }
                }
                if (pillLayout == null) {
                    container.removeView(candidate);
                    existingPill = null;
                }
            }

            if (pillLayout == null) {
                if (existingPill != null && existingPill.getParent() instanceof ViewGroup) {
                    ((ViewGroup) existingPill.getParent()).removeView(existingPill);
                }

                // 1. Create Outer Pill Layout (Material 3 capsule with system dynamic color)
                pillLayout = new LinearLayout(context);
                pillLayout.setOrientation(LinearLayout.HORIZONTAL);
                pillLayout.setGravity(Gravity.CENTER);
                pillLayout.setClickable(false);
                pillLayout.setFocusable(false);

                int padH = dp(context, 16);
                int padV = dp(context, 7);
                pillLayout.setPadding(padH, padV, padH, padV);

                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dp(context, 20));
                bg.setColor(surfaceColor);
                bg.setStroke(dp(context, 1), strokeColor);
                pillLayout.setBackground(bg);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    pillLayout.setElevation(dp(context, 8));
                }

                // 2. Create Inner Row for Text Slide Animation
                contentRow = new LinearLayout(context);
                contentRow.setOrientation(LinearLayout.HORIZONTAL);
                contentRow.setGravity(Gravity.CENTER);
                contentRow.setClickable(false);
                contentRow.setFocusable(false);

                leftArrow = new ChevronView(context, false);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(dp(context, 10), dp(context, 14));
                leftParams.gravity = Gravity.CENTER_VERTICAL;
                leftArrow.setLayoutParams(leftParams);

                langText = new TextView(context);
                langText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                langText.setTypeface(Typeface.DEFAULT_BOLD);
                langText.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams langParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                int textMarginH = dp(context, 12);
                langParams.leftMargin = textMarginH;
                langParams.rightMargin = textMarginH;
                langParams.gravity = Gravity.CENTER_VERTICAL;
                langText.setLayoutParams(langParams);

                rightArrow = new ChevronView(context, true);
                LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(dp(context, 10), dp(context, 14));
                rightParams.gravity = Gravity.CENTER_VERTICAL;
                rightArrow.setLayoutParams(rightParams);

                contentRow.addView(leftArrow);
                contentRow.addView(langText);
                contentRow.addView(rightArrow);

                pillLayout.addView(contentRow);

                FrameLayout.LayoutParams pillParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                pillParams.gravity = Gravity.TOP | Gravity.START;
                container.addView(pillLayout, pillParams);
                sCurrentPillRef = new WeakReference<>(pillLayout);
            } else {
                // Update existing background color & border for current theme
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(dp(context, 20));
                bg.setColor(surfaceColor);
                bg.setStroke(dp(context, 1), strokeColor);
                pillLayout.setBackground(bg);
            }

            // Update text content and colors
            String displayName = (languageName != null && !languageName.isEmpty()) ? languageName : "Язык";
            langText.setText(displayName);
            langText.setTextColor(textColor);

            // Highlight arrow corresponding to swipe direction
            if (forward) {
                rightArrow.setColor(activeColor);
                leftArrow.setColor(inactiveColor);
            } else {
                leftArrow.setColor(activeColor);
                rightArrow.setColor(inactiveColor);
            }

            // Position pill directly above spacebar
            positionPillAboveSpacebar(container, keyboardView, pillLayout);

            // Pill Entrance Animation (Scale + Alpha)
            pillLayout.setScaleX(0.85f);
            pillLayout.setScaleY(0.85f);
            pillLayout.setAlpha(0f);
            pillLayout.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(160)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Inner Text Row Slide Animation (Directional)
            float slideDistance = dp(context, 26);
            contentRow.setTranslationX(forward ? -slideDistance : slideDistance);
            contentRow.setAlpha(0.2f);
            contentRow.animate()
                    .translationX(0f)
                    .alpha(1.0f)
                    .setDuration(220)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            // Schedule dismissal after 800ms
            final View finalPill = pillLayout;
            sDismissRunnable = () -> dismiss(finalPill);
            sMainHandler.postDelayed(sDismissRunnable, 800);

        } catch (Throwable t) {
            Log.w(TAG, "Failed to show spacebar indicator overlay", t);
        }
    }

    private static void positionPillAboveSpacebar(ViewGroup container, View keyboardView, View pill) {
        try {
            int cWidth = container.getWidth() > 0 ? container.getWidth() : container.getResources().getDisplayMetrics().widthPixels;
            int cHeight = container.getHeight() > 0 ? container.getHeight() : container.getResources().getDisplayMetrics().heightPixels;

            pill.measure(
                    View.MeasureSpec.makeMeasureSpec(cWidth, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(cHeight, View.MeasureSpec.AT_MOST)
            );
            int pillW = pill.getMeasuredWidth();
            int pillH = pill.getMeasuredHeight();
            if (pillW <= 0) pillW = dp(container.getContext(), 120);
            if (pillH <= 0) pillH = dp(container.getContext(), 36);

            View spacebar = GboardSpacebarSwipeRuntime.findSpacebarView(keyboardView);
            int pillX;
            int pillY;

            int[] rootLoc = new int[2];
            container.getLocationInWindow(rootLoc);

            if (spacebar != null && spacebar.getVisibility() == View.VISIBLE) {
                int[] spaceLoc = new int[2];
                spacebar.getLocationInWindow(spaceLoc);

                int spaceCenterX = (spaceLoc[0] - rootLoc[0]) + spacebar.getWidth() / 2;
                pillX = spaceCenterX - pillW / 2;
                pillY = (spaceLoc[1] - rootLoc[1]) - pillH - dp(container.getContext(), 10);
            } else {
                pillX = (cWidth - pillW) / 2;
                pillY = (int) (cHeight * 0.65f - pillH);
            }

            // Keep within container bounds
            pillX = Math.max(dp(container.getContext(), 8), Math.min(cWidth - pillW - dp(container.getContext(), 8), pillX));
            pillY = Math.max(0, pillY);

            pill.setX(pillX);
            pill.setY(pillY);
        } catch (Throwable ignored) {
        }
    }

    private static void dismiss(View pill) {
        if (pill == null) return;
        try {
            pill.animate()
                    .alpha(0f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        try {
                            if (pill.getParent() instanceof ViewGroup) {
                                ((ViewGroup) pill.getParent()).removeView(pill);
                            }
                            if (sCurrentPillRef != null && sCurrentPillRef.get() == pill) {
                                sCurrentPillRef.clear();
                            }
                        } catch (Throwable ignored) {
                        }
                    })
                    .start();
        } catch (Throwable ignored) {
        }
    }

    public static void hide() {
        sMainHandler.post(() -> {
            if (sDismissRunnable != null) {
                sMainHandler.removeCallbacks(sDismissRunnable);
                sDismissRunnable = null;
            }
            View pill = (sCurrentPillRef != null) ? sCurrentPillRef.get() : null;
            if (pill != null) {
                dismiss(pill);
            }
        });
    }

    public static int resolveSurfaceColor(Context context) {
        if (context == null) {
            return Color.parseColor("#E62B2D30");
        }
        boolean isDark = true;
        try {
            isDark = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        } catch (Throwable ignored) {
        }

        // 1. Android 12+ Monet Dynamic Surface Container
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                int resId = context.getResources().getIdentifier(
                        isDark ? "system_surface_container_high_dark" : "system_surface_container_high_light",
                        "color", "android");
                if (resId != 0) {
                    return applyAlpha(context.getColor(resId), 0.94f);
                }
                resId = context.getResources().getIdentifier(
                        isDark ? "system_neutral1_800" : "system_neutral1_100",
                        "color", "android");
                if (resId != 0) {
                    return applyAlpha(context.getColor(resId), 0.94f);
                }
            } catch (Throwable ignored) {
            }
        }

        // 2. Try theme attributes
        try {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.colorBackground, tv, true)) {
                if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                    return applyAlpha(tv.data, 0.94f);
                }
            }
        } catch (Throwable ignored) {
        }

        // 3. Fallback based on dark/light mode
        return isDark ? Color.parseColor("#E6282A2E") : Color.parseColor("#F0F2F4F7");
    }

    public static int resolveAccentColor(Context context, boolean isDark) {
        if (context == null) {
            return isDark ? Color.parseColor("#8AB4F8") : Color.parseColor("#1A73E8");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                int resId = context.getResources().getIdentifier(
                        isDark ? "system_accent1_300" : "system_accent1_600",
                        "color", "android");
                if (resId != 0) {
                    return context.getColor(resId);
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.colorPrimary, tv, true)) {
                if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                    return tv.data;
                }
            }
        } catch (Throwable ignored) {
        }
        return isDark ? Color.parseColor("#8AB4F8") : Color.parseColor("#1A73E8");
    }

    private static int applyAlpha(int color, float alpha) {
        int a = Math.round(255 * alpha);
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
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
