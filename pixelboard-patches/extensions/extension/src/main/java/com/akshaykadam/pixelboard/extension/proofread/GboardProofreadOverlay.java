/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarIndicatorOverlay;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Material 3 Floating Diff Card displaying text corrections with Git-style red/green highlighting.
 */
public final class GboardProofreadOverlay {
    private static final String TAG = "PixelBoardProofread";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private static WeakReference<View> sCurrentCardRef = null;
    private static Runnable sAutoDismissRunnable = null;

    private GboardProofreadOverlay() {
    }

    /**
     * Displays a transient loading indicator card.
     */
    public static void showLoading(View keyboardView) {
        if (keyboardView == null) return;
        sMainHandler.post(() -> {
            dismissCurrentCard();
            try {
                View root = keyboardView.getRootView();
                if (!(root instanceof ViewGroup)) return;
                ViewGroup container = (ViewGroup) root;
                Context context = keyboardView.getContext();

                LinearLayout card = createBaseCard(context, container);

                TextView loadingText = new TextView(context);
                loadingText.setText("✨  Проверяем текст...");
                loadingText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                loadingText.setTypeface(Typeface.DEFAULT_BOLD);
                loadingText.setGravity(Gravity.CENTER);

                int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
                boolean isDark = isColorDark(surfaceColor);
                loadingText.setTextColor(isDark ? Color.WHITE : Color.parseColor("#1D1B20"));
                loadingText.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));

                card.addView(loadingText);
                container.addView(card, createCardLayoutParams(context, container));
                sCurrentCardRef = new WeakReference<>(card);
                animateEntrance(card);

            } catch (Throwable t) {
                Log.w(TAG, "Failed to show loading card", t);
            }
        });
    }

    /**
     * Displays a transient message (e.g. "Нет текста для проверки" or "Ошибок не найдено").
     */
    public static void showEmpty(View keyboardView, String message) {
        if (keyboardView == null) return;
        sMainHandler.post(() -> {
            dismissCurrentCard();
            try {
                View root = keyboardView.getRootView();
                if (!(root instanceof ViewGroup)) return;
                ViewGroup container = (ViewGroup) root;
                Context context = keyboardView.getContext();

                LinearLayout card = createBaseCard(context, container);

                TextView text = new TextView(context);
                text.setText(message != null ? message : "Ошибок не найдено");
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                text.setTypeface(Typeface.DEFAULT_BOLD);
                text.setGravity(Gravity.CENTER);

                int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
                boolean isDark = isColorDark(surfaceColor);
                text.setTextColor(isDark ? Color.WHITE : Color.parseColor("#1D1B20"));
                text.setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16));

                card.addView(text);
                container.addView(card, createCardLayoutParams(context, container));
                sCurrentCardRef = new WeakReference<>(card);
                animateEntrance(card);

                sAutoDismissRunnable = () -> dismiss(card);
                sMainHandler.postDelayed(sAutoDismissRunnable, 1800);

            } catch (Throwable t) {
                Log.w(TAG, "Failed to show empty message card", t);
            }
        });
    }

    /**
     * Displays the full Git-style Diff Card with Apply and Cancel buttons.
     */
    public static void showResult(View keyboardView, InputMethodService ims, GboardProofreadService.ProofreadResult result) {
        if (keyboardView == null || result == null) return;

        sMainHandler.post(() -> {
            dismissCurrentCard();
            if (!result.hasChanges) {
                if (result.errorMessage != null) {
                    showEmpty(keyboardView, "⚠️  " + result.errorMessage);
                } else {
                    showEmpty(keyboardView, "✓  Ошибок не найдено! Текст грамотный");
                }
                return;
            }

            try {
                View root = keyboardView.getRootView();
                if (!(root instanceof ViewGroup)) return;
                ViewGroup container = (ViewGroup) root;
                Context context = keyboardView.getContext();

                int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
                boolean isDark = isColorDark(surfaceColor);
                int textColor = isDark ? Color.WHITE : Color.parseColor("#1D1B20");
                int secondaryTextColor = isDark ? Color.parseColor("#B0B3B8") : Color.parseColor("#5F6368");

                // Colors for Git Diff
                int redBg = isDark ? Color.parseColor("#45FF5252") : Color.parseColor("#25FF1744");
                int redFg = isDark ? Color.parseColor("#FF8A80") : Color.parseColor("#C62828");
                int greenBg = isDark ? Color.parseColor("#454CAF50") : Color.parseColor("#252E7D32");
                int greenFg = isDark ? Color.parseColor("#81C995") : Color.parseColor("#1B5E20");

                LinearLayout card = createBaseCard(context, container);

                // 1. Header Row
                LinearLayout headerRow = new LinearLayout(context);
                headerRow.setOrientation(LinearLayout.HORIZONTAL);
                headerRow.setGravity(Gravity.CENTER_VERTICAL);
                headerRow.setPadding(dp(context, 16), dp(context, 12), dp(context, 12), dp(context, 8));

                TextView title = new TextView(context);
                title.setText("✨  Проверка текста (Git-дифф)");
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                title.setTypeface(Typeface.DEFAULT_BOLD);
                title.setTextColor(textColor);
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                headerRow.addView(title, titleParams);

                TextView closeBtn = new TextView(context);
                closeBtn.setText("✕");
                closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
                closeBtn.setTextColor(secondaryTextColor);
                closeBtn.setGravity(Gravity.CENTER);
                int closePad = dp(context, 8);
                closeBtn.setPadding(closePad, closePad, closePad, closePad);
                closeBtn.setOnClickListener(v -> dismiss(card));
                headerRow.addView(closeBtn);

                card.addView(headerRow);

                // 2. Diff Text Body (ScrollView with SpannableStringBuilder)
                ScrollView scrollView = new ScrollView(context);
                LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                scrollParams.leftMargin = dp(context, 16);
                scrollParams.rightMargin = dp(context, 16);
                scrollParams.bottomMargin = dp(context, 10);
                scrollParams.weight = 1.0f;

                TextView diffView = new TextView(context);
                diffView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                diffView.setLineSpacing(dp(context, 3), 1.25f);
                diffView.setTextColor(textColor);

                SpannableStringBuilder ssb = formatGitDiff(result.diffTokens, redBg, redFg, greenBg, greenFg);
                diffView.setText(ssb);

                scrollView.addView(diffView);
                card.addView(scrollView, scrollParams);

                // 3. Action Buttons Row (Отмена and Применить)
                LinearLayout actionsRow = new LinearLayout(context);
                actionsRow.setOrientation(LinearLayout.HORIZONTAL);
                actionsRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                actionsRow.setPadding(dp(context, 16), 0, dp(context, 16), dp(context, 12));

                // Cancel Button
                TextView cancelBtn = new TextView(context);
                cancelBtn.setText("Отмена");
                cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                cancelBtn.setTypeface(Typeface.DEFAULT_BOLD);
                cancelBtn.setTextColor(secondaryTextColor);
                cancelBtn.setGravity(Gravity.CENTER);
                cancelBtn.setPadding(dp(context, 16), dp(context, 8), dp(context, 16), dp(context, 8));
                cancelBtn.setOnClickListener(v -> dismiss(card));
                actionsRow.addView(cancelBtn);

                // Apply Button (Primary Green / Accent)
                TextView applyBtn = new TextView(context);
                applyBtn.setText("✓  Применить");
                applyBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                applyBtn.setTypeface(Typeface.DEFAULT_BOLD);
                applyBtn.setTextColor(Color.WHITE);
                applyBtn.setGravity(Gravity.CENTER);
                applyBtn.setPadding(dp(context, 18), dp(context, 8), dp(context, 18), dp(context, 8));

                GradientDrawable applyBg = new GradientDrawable();
                applyBg.setShape(GradientDrawable.RECTANGLE);
                applyBg.setCornerRadius(dp(context, 18));
                applyBg.setColor(Color.parseColor("#2E7D32")); // Material Deep Green
                applyBtn.setBackground(applyBg);

                LinearLayout.LayoutParams applyParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                applyParams.leftMargin = dp(context, 10);
                applyBtn.setLayoutParams(applyParams);

                applyBtn.setOnClickListener(v -> {
                    // 1. Apply to input field
                    GboardProofreadService.applyCorrection(ims, result);
                    // 2. Play haptic feedback
                    performHapticConfirm(keyboardView);
                    // 3. Dismiss card
                    dismiss(card);
                });

                actionsRow.addView(applyBtn);
                card.addView(actionsRow);

                // 4. Attach card to container
                container.addView(card, createCardLayoutParams(context, container));
                sCurrentCardRef = new WeakReference<>(card);
                animateEntrance(card);

            } catch (Throwable t) {
                Log.w(TAG, "Failed to show proofread diff card", t);
            }
        });
    }

    /**
     * Formats DiffTokens into a Git-style colorized Spannable string.
     */
    public static SpannableStringBuilder formatGitDiff(List<GboardProofreadService.DiffToken> tokens,
                                                       int redBg, int redFg, int greenBg, int greenFg) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (tokens == null) return ssb;

        for (GboardProofreadService.DiffToken token : tokens) {
            if (token == null || token.text == null || token.text.isEmpty()) {
                continue;
            }

            int start = ssb.length();
            ssb.append(token.text);
            int end = ssb.length();

            if (token.type == GboardProofreadService.DiffType.DELETED) {
                // Red background + strikethrough + red text
                ssb.setSpan(new BackgroundColorSpan(redBg), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(redFg), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (token.type == GboardProofreadService.DiffType.INSERTED) {
                // Green background + bold + green text
                ssb.setSpan(new BackgroundColorSpan(greenBg), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ForegroundColorSpan(greenFg), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }

    private static LinearLayout createBaseCard(Context context, ViewGroup container) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setClickable(true);
        card.setFocusable(true);

        int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
        boolean isDark = isColorDark(surfaceColor);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dp(context, 18));
        bg.setColor(surfaceColor);
        bg.setStroke(dp(context, 1), isDark ? Color.parseColor("#30FFFFFF") : Color.parseColor("#22000000"));
        card.setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            card.setElevation(dp(context, 12));
        }

        return card;
    }

    private static FrameLayout.LayoutParams createCardLayoutParams(Context context, ViewGroup container) {
        int cWidth = container.getWidth() > 0 ? container.getWidth() : container.getResources().getDisplayMetrics().widthPixels;
        int cardWidth = Math.max(dp(context, 280), cWidth - dp(context, 24));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cardWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = dp(context, 48); // Sit nicely directly above bottom keyboard row
        return params;
    }

    private static void animateEntrance(View card) {
        card.setScaleX(0.92f);
        card.setScaleY(0.92f);
        card.setAlpha(0f);
        card.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(180)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void dismiss(View card) {
        if (card == null) return;
        try {
            card.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .alpha(0f)
                    .setDuration(160)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        try {
                            if (card.getParent() instanceof ViewGroup) {
                                ((ViewGroup) card.getParent()).removeView(card);
                            }
                            if (sCurrentCardRef != null && sCurrentCardRef.get() == card) {
                                sCurrentCardRef.clear();
                            }
                        } catch (Throwable ignored) {
                        }
                    })
                    .start();
        } catch (Throwable ignored) {
        }
    }

    public static void dismissCurrentCard() {
        if (sAutoDismissRunnable != null) {
            sMainHandler.removeCallbacks(sAutoDismissRunnable);
            sAutoDismissRunnable = null;
        }
        View card = (sCurrentCardRef != null) ? sCurrentCardRef.get() : null;
        if (card != null) {
            dismiss(card);
        }
    }

    private static void performHapticConfirm(View view) {
        if (view == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        } catch (Throwable t) {
            try {
                Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    v.vibrate(30);
                }
            } catch (Throwable ignored) {
            }
        }
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
