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

    public static void setEnabledForTesting(Boolean enabled) {
        cachedEnabled = enabled;
    }
}
