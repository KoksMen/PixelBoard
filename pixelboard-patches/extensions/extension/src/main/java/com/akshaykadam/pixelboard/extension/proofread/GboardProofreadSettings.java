/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.proofread;

import android.content.Context;
import android.content.SharedPreferences;

import com.akshaykadam.pixelboard.extension.settings.GboardPatchesSettings;

/**
 * Settings helper for the text proofreading & correction feature (similar to Yandex Keyboard).
 */
public final class GboardProofreadSettings {
    public static final String PREF_KEY_ENABLED = "pref_key_proofread_enabled";
    public static final boolean DEFAULT_ENABLED = true;

    public static final String PREF_KEY_ALWAYS_VISIBLE = "pref_key_proofread_always_visible";
    public static final boolean DEFAULT_ALWAYS_VISIBLE = true;

    public static final String PREF_KEY_TOOLBAR_POSITION_LEFT = "pref_key_toolbar_position_left";
    public static final boolean DEFAULT_TOOLBAR_POSITION_LEFT = false;

    public static final String PREF_KEY_TOOLBAR_ORDER_SPARKLE_FIRST = "pref_key_toolbar_order_sparkle_first";
    public static final boolean DEFAULT_TOOLBAR_ORDER_SPARKLE_FIRST = false;

    public static final String PREF_KEY_HIDE_STICKERS = "pref_key_hide_stickers";
    public static final boolean DEFAULT_HIDE_STICKERS = true;

    public static final String PREF_KEY_HIDE_GIF = "pref_key_hide_gif";
    public static final boolean DEFAULT_HIDE_GIF = true;

    public static final String PREF_KEY_HIDE_THEMES = "pref_key_hide_themes";
    public static final boolean DEFAULT_HIDE_THEMES = true;

    public static final String PREF_KEY_HIDE_TRANSLATE = "pref_key_hide_translate";
    public static final boolean DEFAULT_HIDE_TRANSLATE = false;

    public static final String PREF_KEY_HIDE_CLIPBOARD = "pref_key_hide_clipboard";
    public static final boolean DEFAULT_HIDE_CLIPBOARD = false;

    public static final String PREF_KEY_AUTOFILL_POSITION_RATIO = "pref_key_autofill_position_ratio";
    public static final float DEFAULT_AUTOFILL_POSITION_RATIO = -1.0f;

    private static volatile Boolean cachedEnabled;
    private static volatile Boolean cachedAlwaysVisible;
    private static volatile Boolean cachedPositionLeft;
    private static volatile Boolean cachedOrderSparkleFirst;

    private GboardProofreadSettings() {
    }

    public static SharedPreferences preferences(Context context) {
        return GboardPatchesSettings.preferences(context);
    }

    public static boolean readEnabled(SharedPreferences preferences) {
        if (preferences == null) {
            return DEFAULT_ENABLED;
        }
        return preferences.getBoolean(PREF_KEY_ENABLED, DEFAULT_ENABLED);
    }

    public static boolean writeEnabled(SharedPreferences preferences, boolean enabled) {
        if (preferences == null) {
            return false;
        }
        preferences.edit().putBoolean(PREF_KEY_ENABLED, enabled).apply();
        cachedEnabled = Boolean.valueOf(enabled);
        return true;
    }

    public static boolean readAlwaysVisible(SharedPreferences preferences) {
        if (preferences == null) {
            return DEFAULT_ALWAYS_VISIBLE;
        }
        return preferences.getBoolean(PREF_KEY_ALWAYS_VISIBLE, DEFAULT_ALWAYS_VISIBLE);
    }

    public static boolean writeAlwaysVisible(SharedPreferences preferences, boolean alwaysVisible) {
        if (preferences == null) {
            return false;
        }
        preferences.edit().putBoolean(PREF_KEY_ALWAYS_VISIBLE, alwaysVisible).apply();
        cachedAlwaysVisible = Boolean.valueOf(alwaysVisible);
        return true;
    }

    public static boolean isAlwaysVisible(Context context) {
        Boolean cached = cachedAlwaysVisible;
        if (cached != null) {
            return cached.booleanValue();
        }
        if (context == null) {
            return DEFAULT_ALWAYS_VISIBLE;
        }
        try {
            SharedPreferences prefs = preferences(context);
            boolean visible = readAlwaysVisible(prefs);
            cachedAlwaysVisible = Boolean.valueOf(visible);
            return visible;
        } catch (Throwable ignored) {
            return DEFAULT_ALWAYS_VISIBLE;
        }
    }

    public static boolean isEnabled(Context context) {
        Boolean cached = cachedEnabled;
        if (cached != null) {
            return cached.booleanValue();
        }
        if (context == null) {
            return DEFAULT_ENABLED;
        }
        try {
            SharedPreferences prefs = preferences(context);
            boolean enabled = readEnabled(prefs);
            cachedEnabled = Boolean.valueOf(enabled);
            return enabled;
        } catch (Throwable ignored) {
            return DEFAULT_ENABLED;
        }
    }

    public static boolean readPositionLeft(SharedPreferences preferences) {
        if (preferences == null) {
            return DEFAULT_TOOLBAR_POSITION_LEFT;
        }
        return preferences.getBoolean(PREF_KEY_TOOLBAR_POSITION_LEFT, DEFAULT_TOOLBAR_POSITION_LEFT);
    }

    public static boolean writePositionLeft(SharedPreferences preferences, boolean positionLeft) {
        if (preferences == null) {
            return false;
        }
        preferences.edit().putBoolean(PREF_KEY_TOOLBAR_POSITION_LEFT, positionLeft).apply();
        cachedPositionLeft = Boolean.valueOf(positionLeft);
        return true;
    }

    public static boolean isPositionLeft(Context context) {
        Boolean cached = cachedPositionLeft;
        if (cached != null) {
            return cached.booleanValue();
        }
        if (context == null) {
            return DEFAULT_TOOLBAR_POSITION_LEFT;
        }
        try {
            SharedPreferences prefs = preferences(context);
            boolean pos = readPositionLeft(prefs);
            cachedPositionLeft = Boolean.valueOf(pos);
            return pos;
        } catch (Throwable ignored) {
            return DEFAULT_TOOLBAR_POSITION_LEFT;
        }
    }

    public static boolean readOrderSparkleFirst(SharedPreferences preferences) {
        if (preferences == null) {
            return DEFAULT_TOOLBAR_ORDER_SPARKLE_FIRST;
        }
        return preferences.getBoolean(PREF_KEY_TOOLBAR_ORDER_SPARKLE_FIRST, DEFAULT_TOOLBAR_ORDER_SPARKLE_FIRST);
    }

    public static boolean writeOrderSparkleFirst(SharedPreferences preferences, boolean sparkleFirst) {
        if (preferences == null) {
            return false;
        }
        preferences.edit().putBoolean(PREF_KEY_TOOLBAR_ORDER_SPARKLE_FIRST, sparkleFirst).apply();
        cachedOrderSparkleFirst = Boolean.valueOf(sparkleFirst);
        return true;
    }

    public static boolean isOrderSparkleFirst(Context context) {
        Boolean cached = cachedOrderSparkleFirst;
        if (cached != null) {
            return cached.booleanValue();
        }
        if (context == null) {
            return DEFAULT_TOOLBAR_ORDER_SPARKLE_FIRST;
        }
        try {
            SharedPreferences prefs = preferences(context);
            boolean order = readOrderSparkleFirst(prefs);
            cachedOrderSparkleFirst = Boolean.valueOf(order);
            return order;
        } catch (Throwable ignored) {
            return DEFAULT_TOOLBAR_ORDER_SPARKLE_FIRST;
        }
    }

    public static boolean readHideStickers(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_HIDE_STICKERS;
        return preferences.getBoolean(PREF_KEY_HIDE_STICKERS, DEFAULT_HIDE_STICKERS);
    }

    public static boolean writeHideStickers(SharedPreferences preferences, boolean hide) {
        if (preferences == null) return false;
        preferences.edit().putBoolean(PREF_KEY_HIDE_STICKERS, hide).apply();
        return true;
    }

    public static boolean isHideStickers(Context context) {
        if (context == null) return DEFAULT_HIDE_STICKERS;
        try {
            return readHideStickers(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_HIDE_STICKERS;
        }
    }

    public static boolean readHideGif(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_HIDE_GIF;
        return preferences.getBoolean(PREF_KEY_HIDE_GIF, DEFAULT_HIDE_GIF);
    }

    public static boolean writeHideGif(SharedPreferences preferences, boolean hide) {
        if (preferences == null) return false;
        preferences.edit().putBoolean(PREF_KEY_HIDE_GIF, hide).apply();
        return true;
    }

    public static boolean isHideGif(Context context) {
        if (context == null) return DEFAULT_HIDE_GIF;
        try {
            return readHideGif(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_HIDE_GIF;
        }
    }

    public static boolean readHideThemes(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_HIDE_THEMES;
        return preferences.getBoolean(PREF_KEY_HIDE_THEMES, DEFAULT_HIDE_THEMES);
    }

    public static boolean writeHideThemes(SharedPreferences preferences, boolean hide) {
        if (preferences == null) return false;
        preferences.edit().putBoolean(PREF_KEY_HIDE_THEMES, hide).apply();
        return true;
    }

    public static boolean isHideThemes(Context context) {
        if (context == null) return DEFAULT_HIDE_THEMES;
        try {
            return readHideThemes(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_HIDE_THEMES;
        }
    }

    public static boolean readHideTranslate(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_HIDE_TRANSLATE;
        return preferences.getBoolean(PREF_KEY_HIDE_TRANSLATE, DEFAULT_HIDE_TRANSLATE);
    }

    public static boolean writeHideTranslate(SharedPreferences preferences, boolean hide) {
        if (preferences == null) return false;
        preferences.edit().putBoolean(PREF_KEY_HIDE_TRANSLATE, hide).apply();
        return true;
    }

    public static boolean isHideTranslate(Context context) {
        if (context == null) return DEFAULT_HIDE_TRANSLATE;
        try {
            return readHideTranslate(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_HIDE_TRANSLATE;
        }
    }

    public static boolean readHideClipboard(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_HIDE_CLIPBOARD;
        return preferences.getBoolean(PREF_KEY_HIDE_CLIPBOARD, DEFAULT_HIDE_CLIPBOARD);
    }

    public static boolean writeHideClipboard(SharedPreferences preferences, boolean hide) {
        if (preferences == null) return false;
        preferences.edit().putBoolean(PREF_KEY_HIDE_CLIPBOARD, hide).apply();
        return true;
    }

    public static boolean isHideClipboard(Context context) {
        if (context == null) return DEFAULT_HIDE_CLIPBOARD;
        try {
            return readHideClipboard(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_HIDE_CLIPBOARD;
        }
    }

    public static float readAutofillPositionRatio(SharedPreferences preferences) {
        if (preferences == null) return DEFAULT_AUTOFILL_POSITION_RATIO;
        return preferences.getFloat(PREF_KEY_AUTOFILL_POSITION_RATIO, DEFAULT_AUTOFILL_POSITION_RATIO);
    }

    public static boolean writeAutofillPositionRatio(SharedPreferences preferences, float ratio) {
        if (preferences == null) return false;
        preferences.edit().putFloat(PREF_KEY_AUTOFILL_POSITION_RATIO, ratio).apply();
        return true;
    }

    public static float isAutofillPositionRatio(Context context) {
        if (context == null) return DEFAULT_AUTOFILL_POSITION_RATIO;
        try {
            return readAutofillPositionRatio(preferences(context));
        } catch (Throwable ignored) {
            return DEFAULT_AUTOFILL_POSITION_RATIO;
        }
    }

        public static String filterAccessPointsOrder(Context context, String originalOrder) {
        if (originalOrder == null || originalOrder.isEmpty()) {
            return "settings;one_handed;textediting;share;translate;floating_keyboard";
        }
        boolean hideStickers = isHideStickers(context);
        boolean hideGif = isHideGif(context);
        boolean hideThemes = isHideThemes(context);
        boolean hideTranslate = isHideTranslate(context);
        boolean hideClipboard = isHideClipboard(context);

        String[] parts = originalOrder.split(";");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            if ("sticker".equals(trimmed) && hideStickers) continue;
            if ("gif_search".equals(trimmed) && hideGif) continue;
            if ("theme_setting".equals(trimmed) && hideThemes) continue;
            if ("translate".equals(trimmed) && hideTranslate) continue;
            if ("clipboard".equals(trimmed) && hideClipboard) continue;

            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(trimmed);
        }
        return sb.toString();
    }

    public static void setEnabledForTesting(Boolean enabled) {
        cachedEnabled = enabled;
    }
}
