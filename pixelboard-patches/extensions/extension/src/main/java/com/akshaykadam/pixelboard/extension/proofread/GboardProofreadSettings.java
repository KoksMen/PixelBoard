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

    private static volatile Boolean cachedEnabled;
    private static volatile Boolean cachedAlwaysVisible;

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

    public static void setEnabledForTesting(Boolean enabled) {
        cachedEnabled = enabled;
    }
}
