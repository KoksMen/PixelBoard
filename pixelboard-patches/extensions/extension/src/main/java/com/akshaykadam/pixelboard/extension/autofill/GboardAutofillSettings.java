/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.autofill;

import android.content.Context;
import android.content.SharedPreferences;

import com.akshaykadam.pixelboard.extension.settings.GboardPatchesSettings;

/**
 * Settings helper for manual Autofill trigger button.
 */
public final class GboardAutofillSettings {
    public static final String PREF_KEY_ENABLED = "pref_key_autofill_button_enabled";
    public static final boolean DEFAULT_ENABLED = true;

    private static volatile Boolean cachedEnabled;

    private GboardAutofillSettings() {
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
