/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillService;
import com.akshaykadam.pixelboard.extension.autofill.GboardAutofillSettings;
import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarIndicatorOverlay;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Pinned toolbar icon buttons in Gboard's top header strip:
 * 1. Autofill key button (🔑 Bitwarden) - native vector icon button
 * 2. Text proofread button (✨ Проверка / Инструменты редактора) - native vector icon button
 *
 * Designed to look and feel identical to Gboard's native 4-squares and microphone buttons.
 */
public final class GboardProofreadButton {
    private static final String TAG = "PixelBoardProofread";
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final String CUSTOM_BUTTON_TAG = "pixelboard_custom_button";

    private static WeakReference<ViewGroup> sCurrentBarRef = null;
    private static WeakReference<View> sProofreadButtonRef = null;
    private static WeakReference<View> sAutofillButtonRef = null;
    public static WeakReference<ViewGroup> sAttachedParentRef = null;
    public static ViewGroup getAttachedParent() { return sAttachedParentRef != null ? sAttachedParentRef.get() : null; }

    private GboardProofreadButton() {
    }

    /**
     * Vector Key Icon View (drawn via Canvas for crisp, theme-tinted rendering).
     */
    public static final class KeyIconView extends View {
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path mKeyPath = new Path();

        public KeyIconView(Context context) {
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

            // Google Material 3 Filled Key Icon (bold, theme-tinted silhouette matching native Gboard toolbar)
            float cx = w * 0.5f;
            float cy = h * 0.5f;
            float s = Math.min(w, h) / 24f; // 24dp Material design grid

            mPaint.setStyle(Paint.Style.FILL);

            mKeyPath.reset();
            mKeyPath.setFillType(Path.FillType.EVEN_ODD);

            // Outer head circle (radius 5.2dp centered at cx - 4.8dp)
            float headCenterX = cx - 4.8f * s;
            mKeyPath.addCircle(headCenterX, cy, 5.2f * s, Path.Direction.CW);

            // Inner hole cutout (radius 2.2dp)
            mKeyPath.addCircle(headCenterX, cy, 2.2f * s, Path.Direction.CW);

            // Key shaft (solid bar extending to right edge)
            float shaftLeft = headCenterX + 3.2f * s;
            float shaftTop = cy - 1.4f * s;
            float shaftRight = cx + 9.2f * s;
            float shaftBottom = cy + 1.4f * s;
            mKeyPath.addRect(shaftLeft, shaftTop, shaftRight, shaftBottom, Path.Direction.CW);

            // Key tooth 1 (downward rectangular tooth)
            float t1Left = cx + 4.0f * s;
            float t1Right = cx + 6.2f * s;
            float t1Bottom = cy + 4.4f * s;
            mKeyPath.addRect(t1Left, shaftTop, t1Right, t1Bottom, Path.Direction.CW);

            // Key tooth 2 (downward rectangular tooth at end of shaft)
            float t2Left = cx + 7.2f * s;
            float t2Right = cx + 9.2f * s;
            float t2Bottom = cy + 4.4f * s;
            mKeyPath.addRect(t2Left, shaftTop, t2Right, t2Bottom, Path.Direction.CW);

            canvas.drawPath(mKeyPath, mPaint);
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
     * Note: Only the Autofill key button is placed here when enabled.
     * Proofreading is handled exclusively by Gboard's native Writing Tools button [✎✨]
     * to prevent duplicate icons in the toolbar.
     */
    public static void attach(View keyboardView) {
        if (keyboardView == null) return;
        Context context = keyboardView.getContext();

        sMainHandler.post(() -> {
            try {
                ViewGroup host = findBestKeyboardHost(keyboardView);
                if (host != null) {
                    sAttachedParentRef = new WeakReference<>(host);
                }

                boolean autofillEnabled = GboardAutofillSettings.isEnabled(context);
                if (!autofillEnabled) {
                    detachBarOnly();
                    return;
                }

                ViewGroup existingBar = sCurrentBarRef != null ? sCurrentBarRef.get() : null;
                ViewGroup attachedParent = sAttachedParentRef != null ? sAttachedParentRef.get() : null;

                if (existingBar != null && attachedParent == host && existingBar.getParent() == host) {
                    existingBar.bringToFront();
                    updateBarVisibility(existingBar, host, context);
                    return; // Already attached to this host
                }

                if (existingBar != null && existingBar.getParent() instanceof ViewGroup) {
                    ((ViewGroup) existingBar.getParent()).removeView(existingBar);
                }

                boolean positionLeft = GboardProofreadSettings.isPositionLeft(context);
                LinearLayout bar = createBar(context, keyboardView, host, false, autofillEnabled);
                if (bar == null) return;

                String pkg = context.getPackageName();
                int micId = host.getResources().getIdentifier("key_pos_header_power_key", "id", pkg);
                View micView = micId != 0 ? host.findViewById(micId) : null;

                int menuId = host.getResources().getIdentifier("key_pos_header_access_points_menu", "id", pkg);
                View menuView = menuId != 0 ? host.findViewById(menuId) : null;

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;

                int barWidth = dp(context, 40);
                float savedRatio = GboardProofreadSettings.isAutofillPositionRatio(context);

                if (savedRatio >= 0.0f && host.getWidth() > 0) {
                    params.gravity |= Gravity.START;
                    int maxLeft = host.getWidth() - barWidth;
                    int left = (int) (savedRatio * host.getWidth());
                    if (left < 0) left = 0;
                    if (left > maxLeft) left = maxLeft;
                    params.leftMargin = left;
                    params.rightMargin = 0;
                } else if (positionLeft) {
                    params.gravity |= Gravity.START;
                    int leftOffset = (menuView != null && menuView.getWidth() > 0)
                            ? (menuView.getRight() + dp(context, 4))
                            : dp(context, 48);
                    params.leftMargin = leftOffset;
                    params.rightMargin = 0;
                } else {
                    params.gravity |= Gravity.END;
                    int micSpace = dp(context, 48);
                    if (micView != null && micView.getVisibility() == View.VISIBLE) {
                        int micW = micView.getWidth();
                        int hostW = host.getWidth();
                        if (hostW > 0 && micView.getLeft() > 0) {
                            micSpace = hostW - micView.getLeft();
                        } else if (micW > 0) {
                            micSpace = micW;
                        }
                    }
                    params.rightMargin = micSpace + dp(context, 2);
                    params.leftMargin = 0;
                }

                // Adjust middle bar padding so word suggestions and stickers never collide with the button
                adjustMiddleBarPadding(host, pkg, positionLeft, barWidth, context, savedRatio >= 0.0f);

                host.addView(bar, params);
                bar.bringToFront();

                sCurrentBarRef = new WeakReference<>(bar);
                sAttachedParentRef = new WeakReference<>(host);

                // Initial visibility update
                updateBarVisibility(bar, host, context);

                // Listen to layout changes on host to hide overlay during sub-screens (Jarvis editor, emoji, etc.)
                host.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        ViewGroup b = sCurrentBarRef != null ? sCurrentBarRef.get() : null;
                        if (b != null && b.getParent() == host) {
                            updateBarVisibility(b, host, context);
                        }
                    }
                });

            } catch (Throwable t) {
                Log.w(TAG, "Failed to attach quick actions bar", t);
            }
        });
    }

    /**
     * Checks if the keyboard is currently showing a subview/editor (like "Инструменты редактора",
     * emoji, clipboard, translate, etc.) and hides the overlay bar so it never superimposes
     * over the header title or back button.
     */
    public static void updateBarVisibility(ViewGroup bar, ViewGroup host, Context context) {
        if (bar == null || host == null || context == null) return;
        try {
            String pkg = context.getPackageName();

            // 0. If top bar customization/edit page is open, hide overlay bar and reset padding
            View r = host.getRootView();
            if (r != null) {
                if (findViewByClassNameRecursive(r, "EditPage") != null
                        || findViewByClassNameRecursive(r, "EditMode") != null
                        || findViewByClassNameRecursive(r, "AccessPointsPanel") != null) {
                    if (bar.getVisibility() != View.GONE) {
                        bar.setVisibility(View.GONE);
                        adjustMiddleBarPadding(host, pkg, false, 0, context, false);
                    }
                    return;
                }
            }

            // 1. If any "back to prime" button is present and visible, a subview is open
            int[] subviewBackIds = new int[]{
                    host.getResources().getIdentifier("key_pos_jarvis_back_to_prime", "id", pkg),
                    host.getResources().getIdentifier("key_pos_back_to_prime", "id", pkg),
                    host.getResources().getIdentifier("key_pos_back_to_prime2", "id", pkg),
                    host.getResources().getIdentifier("key_pos_clipboard_back_to_prime", "id", pkg),
                    host.getResources().getIdentifier("key_pos_textediting_back_to_prime", "id", pkg),
                    host.getResources().getIdentifier("key_pos_translate_back", "id", pkg),
            };

            View root = host.getRootView();
            for (int id : subviewBackIds) {
                if (id != 0 && root != null) {
                    View backView = root.findViewById(id);
                    if (backView != null && backView.getVisibility() == View.VISIBLE) {
                        if (bar.getVisibility() != View.GONE) {
                            bar.setVisibility(View.GONE);
                            adjustMiddleBarPadding(host, pkg, false, 0, context, false);
                        }
                        return;
                    }
                }
            }

            // 2. Also check if the standard 4-squares menu button is present and visible
            int menuId = host.getResources().getIdentifier("key_pos_header_access_points_menu", "id", pkg);
            View menuView = menuId != 0 ? host.findViewById(menuId) : null;
            if (menuView == null || menuView.getVisibility() != View.VISIBLE) {
                if (bar.getVisibility() != View.GONE) {
                    bar.setVisibility(View.GONE);
                }
                return;
            }

            // 3. In prime typing keyboard mode: make bar visible
            if (bar.getVisibility() != View.VISIBLE) {
                bar.setVisibility(View.VISIBLE);
                bar.bringToFront();
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Finds the Gboard header view holder or the header SoftKeyboardView.
     */
    private static View findViewByClassNameRecursive(View v, String nameSubstring) {
        if (v == null) return null;
        if (v.getClass().getSimpleName().contains(nameSubstring) && v.getVisibility() == View.VISIBLE) {
            return v;
        }
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View res = findViewByClassNameRecursive(vg.getChildAt(i), nameSubstring);
                if (res != null) return res;
            }
        }
        return null;
    }

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
        // No-op: Gboard's native Writing Tools button [✎✨] handles proofreading
    }

    public static void detachBarOnly() {
        ViewGroup existing = sCurrentBarRef != null ? sCurrentBarRef.get() : null;
        if (existing != null && existing.getParent() instanceof ViewGroup) {
            ((ViewGroup) existing.getParent()).removeView(existing);
        }
        if (sCurrentBarRef != null) sCurrentBarRef.clear();
        if (sProofreadButtonRef != null) sProofreadButtonRef.clear();
        if (sAutofillButtonRef != null) sAutofillButtonRef.clear();
    }

    public static void detach() {
        sMainHandler.post(() -> {
            detachBarOnly();
            if (sAttachedParentRef != null) sAttachedParentRef.clear();
        });
    }

    private static void adjustMiddleBarPadding(ViewGroup host, String pkg, boolean positionLeft, int barWidth, Context context, boolean isCustomDragged) {
        if (host == null) return;
        try {
            int candId = host.getResources().getIdentifier("softkey_holder_fixed_candidates", "id", pkg);
            View cand = candId != 0 ? host.findViewById(candId) : null;
            View middleBar = cand != null && cand.getParent() instanceof View ? (View) cand.getParent() : cand;

            if (middleBar != null) {
                if (barWidth <= 0 || isCustomDragged) {
                    // Reset padding so candidate strip has full dynamic width
                    middleBar.setPadding(0, middleBar.getPaddingTop(), 0, middleBar.getPaddingBottom());
                } else {
                    int extraPad = barWidth + dp(context, 2);
                    if (positionLeft) {
                        middleBar.setPadding(extraPad, middleBar.getPaddingTop(), 0, middleBar.getPaddingBottom());
                    } else {
                        middleBar.setPadding(0, middleBar.getPaddingTop(), extraPad, middleBar.getPaddingBottom());
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static LinearLayout createBar(
            Context context, View keyboardView, ViewGroup host, boolean proofreadEnabled, boolean autofillEnabled) {
        if (!autofillEnabled) {
            return null;
        }
        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setClipChildren(false);
        bar.setClipToPadding(false);

        int surfaceColor = GboardSpacebarIndicatorOverlay.resolveSurfaceColor(context);
        boolean isDark = isColorDark(surfaceColor);

        // Native Gboard icon colors: light gray on dark theme, dark gray on light theme
        int iconColor = isDark ? Color.parseColor("#E2E2E9") : Color.parseColor("#44474E");

        View autofillBtn = createToolbarIconButton(context, keyboardView, host, bar, new KeyIconView(context), iconColor, true);
        sAutofillButtonRef = new WeakReference<>(autofillBtn);
        sProofreadButtonRef = null;

        bar.addView(autofillBtn);
        return bar;
    }

    /**
     * Creates a native Gboard-style transparent toolbar icon button that supports
     * borderless ripple, in-place actions, long-press gestures,
     * and horizontal drag-and-drop to position anywhere on the top bar.
     */
    private static View createToolbarIconButton(
            Context context, View keyboardView, ViewGroup host, LinearLayout bar, View iconView, int iconColor, boolean isAutofill) {
        FrameLayout buttonFrame = new FrameLayout(context);
        buttonFrame.setClickable(true);
        buttonFrame.setFocusable(false);
        buttonFrame.setFocusableInTouchMode(false);
        buttonFrame.setTag(CUSTOM_BUTTON_TAG);

        int buttonSize = dp(context, 40);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(buttonSize, buttonSize);
        frameParams.gravity = Gravity.CENTER_VERTICAL;
        buttonFrame.setLayoutParams(frameParams);

        // Native Gboard style: NO solid circle background! Completely transparent with borderless ripple on touch
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

        // Support dragging to move position on the toolbar
        final int touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        buttonFrame.setOnTouchListener(new View.OnTouchListener() {
            private float downRawX = 0f;
            private float initialMarginLeft = 0f;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downRawX = event.getRawX();
                        if (bar.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bar.getLayoutParams();
                            initialMarginLeft = lp.leftMargin;
                        }
                        isDragging = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downRawX;
                        if (!isDragging && Math.abs(dx) > touchSlop) {
                            isDragging = true;
                            if (v.getParent() != null) {
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        if (isDragging) {
                            if (host != null && host.getWidth() > 0 && bar.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                                int newLeft = (int) (initialMarginLeft + dx);
                                int maxLeft = host.getWidth() - bar.getWidth();
                                if (newLeft < 0) newLeft = 0;
                                if (newLeft > maxLeft) newLeft = maxLeft;
                                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bar.getLayoutParams();
                                lp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                                lp.leftMargin = newLeft;
                                lp.rightMargin = 0;
                                bar.setLayoutParams(lp);
                            }
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isDragging) {
                            if (host != null && host.getWidth() > 0 && bar.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bar.getLayoutParams();
                                float ratio = (float) lp.leftMargin / (float) host.getWidth();
                                GboardProofreadSettings.writeAutofillPositionRatio(
                                        GboardProofreadSettings.preferences(context), ratio);
                            }
                            isDragging = false;
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        buttonFrame.setOnClickListener(v -> {
            try {
                buttonFrame.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable ignored) {
            }
            if (isAutofill) {
                // Short click: trigger in-place autofill prompt without minimizing current app
                GboardAutofillService.requestAutofillOnly(keyboardView);
            } else {
                // Short click: ALWAYS run full Yandex Speller API + Russian grammar heuristics with Git Diff card!
                GboardProofreadService.checkAndShowDiff(keyboardView);
            }
        });

        buttonFrame.setOnLongClickListener(v -> {
            try {
                buttonFrame.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            } catch (Throwable ignored) {
            }
            Context ctx = v.getContext();
            if (isAutofill) {
                // Long press on Key button: open Bitwarden password manager app!
                return GboardAutofillService.launchPasswordManagerApp(ctx);
            } else {
                // Long press on Sparkle button: open PixelBoard settings!
                try {
                    Intent intent = new Intent();
                    intent.setClassName(ctx.getPackageName(), "com.akshaykadam.pixelboard.extension.settings.GboardPatchesSettingsActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    return true;
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to launch settings from button long click", t);
                    return false;
                }
            }
        });

        return buttonFrame;
    }

    /**
     * Triggers Gboard's native "Инструменты редактора ✨" / AI Writing Tools,
     * opening the full sentence proofread and grammar editor with "Использовать этот вариант".
     * Falls back to Yandex Speller if native tools are not available on the current device/field.
     */
    public static void triggerWritingTools(View keyboardView) {
        if (keyboardView == null) return;
        try {
            final View root = keyboardView.getRootView() != null ? keyboardView.getRootView() : keyboardView;

            // 1. Search for existing Writing Tools / Jarvis access point in the active toolbar
            View writingToolsView = findWritingToolsView(root);
            if (writingToolsView != null) {
                writingToolsView.performClick();
                return;
            }

            // 2. If not directly on toolbar, open the 4-squares features menu and click it
            String pkg = root.getContext().getPackageName();
            int menuId = root.getResources().getIdentifier("key_pos_header_access_points_menu", "id", pkg);
            View menuView = menuId != 0 ? root.findViewById(menuId) : null;
            if (menuView != null) {
                menuView.performClick();
                root.postDelayed(() -> {
                    try {
                        View target = findWritingToolsView(root.getRootView() != null ? root.getRootView() : root);
                        if (target != null) {
                            target.performClick();
                        } else {
                            // Retry after short delay in case menu animation is still expanding
                            root.postDelayed(() -> {
                                try {
                                    View retryTarget = findWritingToolsView(root.getRootView() != null ? root.getRootView() : root);
                                    if (retryTarget != null) {
                                        retryTarget.performClick();
                                    } else {
                                        GboardProofreadService.checkAndShowDiff(keyboardView);
                                    }
                                } catch (Throwable ignored) {
                                    GboardProofreadService.checkAndShowDiff(keyboardView);
                                }
                            }, 150);
                        }
                    } catch (Throwable ignored) {
                        GboardProofreadService.checkAndShowDiff(keyboardView);
                    }
                }, 150);
                return;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to launch native writing tools", t);
        }

        // Fallback: custom proofread
        GboardProofreadService.checkAndShowDiff(keyboardView);
    }

    /**
     * Recursively traverses the view tree to locate the native Writing Tools ("Инструменты редактора") button.
     */
    public static View findWritingToolsView(View root) {
        if (root == null) return null;
        try {
            String pkg = root.getContext().getPackageName();
            int id = root.getResources().getIdentifier("key_pos_header_proofread", "id", pkg);
            if (id != 0) {
                View found = root.findViewById(id);
                if (found != null && found.getVisibility() == View.VISIBLE) {
                    return found;
                }
            }
        } catch (Throwable ignored) {
        }
        return findWritingToolsViewRecursive(root);
    }

    private static View findWritingToolsViewRecursive(View v) {
        if (v == null) return null;

        // Ignore our own custom buttons
        if (CUSTOM_BUTTON_TAG.equals(v.getTag())) {
            return null;
        }

        if (v instanceof TextView) {
            CharSequence text = ((TextView) v).getText();
            if (text != null) {
                String s = text.toString().toLowerCase(Locale.ROOT);
                if (s.contains("инструменты редактора") || s.contains("writing tools")
                        || s.contains("proofread") || s.contains("исправить")
                        || s.contains("редактор") || s.contains("інструменти редактора")) {
                    View parent = (View) v.getParent();
                    if (parent != null && parent.isClickable()) {
                        return parent;
                    }
                    return v;
                }
            }
        }

        CharSequence desc = v.getContentDescription();
        if (desc != null) {
            String s = desc.toString().toLowerCase(Locale.ROOT);
            if (s.contains("инструменты редактора") || s.contains("writing tools")
                    || s.contains("proofread") || s.contains("исправить")
                    || s.contains("проверить") || s.contains("редактор")
                    || s.contains("інструменти редактора")) {
                return v;
            }
        }

        Object tag = v.getTag();
        if (tag instanceof String) {
            String s = ((String) tag).toLowerCase(Locale.ROOT);
            if (s.contains("jarvis") || s.contains("writing_tools")) {
                return v;
            }
        }

        try {
            int id = v.getId();
            if (id != View.NO_ID && v.getResources() != null) {
                String entryName = v.getResources().getResourceEntryName(id).toLowerCase(Locale.ROOT);
                if (entryName.contains("jarvis") || entryName.contains("header_proofread")) {
                    return v;
                }
            }
        } catch (Throwable ignored) {
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View found = findWritingToolsViewRecursive(vg.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
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
