package com.akshaykadam.pixelboard.extension.spacebar;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class GboardSpacebarSettingsTest {

    @Test
    public void defaultSpacebarSwipeIsTrue() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = GboardSpacebarSettings.preferences(context);
        preferences.edit().clear().commit();
        GboardSpacebarSettings.invalidateCache();

        Assert.assertTrue(GboardSpacebarSettings.readEnabled(preferences));
        Assert.assertTrue(GboardSpacebarSettings.isEnabled(context));
    }

    @Test
    public void readAndWriteTogglesPreferenceAndInvalidatesCache() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = GboardSpacebarSettings.preferences(context);

        Assert.assertTrue(GboardSpacebarSettings.writeEnabled(preferences, true));
        Assert.assertTrue(GboardSpacebarSettings.readEnabled(preferences));
        Assert.assertTrue(GboardSpacebarSettings.isEnabled(context));

        Assert.assertTrue(GboardSpacebarSettings.writeEnabled(preferences, false));
        Assert.assertFalse(GboardSpacebarSettings.readEnabled(preferences));
        Assert.assertFalse(GboardSpacebarSettings.isEnabled(context));
    }

    @Test
    public void ensureDefaultsSetsDefaultEnabledIfMissing() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = GboardSpacebarSettings.preferences(context);
        preferences.edit().clear().commit();
        GboardSpacebarSettings.invalidateCache();

        GboardSpacebarSettings.ensureDefaults(context, preferences);
        Assert.assertTrue(preferences.contains(GboardSpacebarSettings.PREF_KEY_ENABLED));
        Assert.assertTrue(preferences.getBoolean(GboardSpacebarSettings.PREF_KEY_ENABLED, false));
    }
}
