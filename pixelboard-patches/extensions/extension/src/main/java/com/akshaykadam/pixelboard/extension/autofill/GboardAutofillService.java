/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.autofill;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarSwipeRuntime;

import java.lang.reflect.Method;

/**
 * Service to invoke the Autofill Manager (e.g. Bitwarden, KeePass, Google Password Manager)
 * directly from the keyboard.
 */
public final class GboardAutofillService {
    public static final String TAG = "PixelBoardAutofill";
    // android.R.id.autofill is 0x01020050 in Android framework
    public static final int AUTOFILL_CONTEXT_MENU_ID = 0x01020050;

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private GboardAutofillService() {
    }

    private static void logw(String tag, String msg) {
        try {
            Log.w(tag, msg);
        } catch (Throwable ignored) {
        }
    }

    private static void logi(String tag, String msg) {
        try {
            Log.i(tag, msg);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Resolves the package name of the active autofill service (e.g. Bitwarden).
     */
    public static String getAutofillServicePackage(Context context) {
        if (context == null) return "com.x8bit.bitwarden";
        try {
            String serviceComponent = Settings.Secure.getString(
                    context.getContentResolver(), "autofill_service");
            if (serviceComponent != null && !serviceComponent.isEmpty()) {
                ComponentName cn = ComponentName.unflattenFromString(serviceComponent);
                if (cn != null && cn.getPackageName() != null && !cn.getPackageName().isEmpty()) {
                    return cn.getPackageName();
                }
            }
        } catch (Throwable ignored) {
        }

        // Fallbacks for known password managers installed on device
        String[] known = new String[]{
                "com.x8bit.bitwarden",
                "com.onepassword.android",
                "keepass2android.keepass2android",
                "com.dashlane",
                "com.nordpass"
        };
        for (String pkg : known) {
            try {
                if (resolveLaunchIntent(context, pkg) != null) {
                    return pkg;
                }
            } catch (Throwable ignored) {
            }
        }
        return "com.x8bit.bitwarden";
    }

    /**
     * Resolves or constructs a launch intent for the given package name,
     * working around Android 11+ package visibility filtering (&lt;queries&gt;).
     */
    public static Intent resolveLaunchIntent(Context context, String pkg) {
        if (pkg == null || pkg.isEmpty()) return null;
        if (context != null) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (intent != null) return intent;
            } catch (Throwable ignored) {
            }
        }

        // Direct explicit intent fallbacks for known password managers
        if ("com.x8bit.bitwarden".equals(pkg)) {
            Intent directIntent = new Intent(Intent.ACTION_MAIN);
            directIntent.setComponent(new ComponentName(pkg, "com.x8bit.bitwarden.MainActivity"));
            directIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            return directIntent;
        }

        Intent genericIntent = new Intent(Intent.ACTION_MAIN);
        genericIntent.setPackage(pkg);
        genericIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return genericIntent;
    }

    /**
     * Sends an in-place autofill request for the currently focused field without minimizing or leaving the current app.
     */
    public static boolean requestAutofillOnly(View keyboardView) {
        if (keyboardView == null) return false;
        Context context = keyboardView.getContext();
        try {
            keyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        } catch (Throwable ignored) {
        }

        InputMethodService ims = GboardSpacebarSwipeRuntime.findInputMethodService(keyboardView);
        if (ims == null) {
            ims = GboardSpacebarSwipeRuntime.getCurrentInputMethodService();
        }

        boolean performed = false;
        if (ims != null) {
            InputConnection ic = ims.getCurrentInputConnection();
            if (ic != null) {
                try {
                    performed = ic.performContextMenuAction(AUTOFILL_CONTEXT_MENU_ID);
                    logi(TAG, "performContextMenuAction(autofill) result: " + performed);
                } catch (Throwable t) {
                    logw(TAG, "Exception during performContextMenuAction(autofill)");
                }
            }
        }

        // Also trigger via system AutofillManager if available on API 26+
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                android.view.autofill.AutofillManager afm =
                        context.getSystemService(android.view.autofill.AutofillManager.class);
                if (afm != null && afm.isEnabled()) {
                    View root = keyboardView.getRootView();
                    View focused = root != null ? root.findFocus() : null;
                    if (focused != null) {
                        afm.requestAutofill(focused);
                        performed = true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        showToast(context, "Запрос автозаполнения 🔑");
        return performed;
    }

    /**
     * Forcibly launches the active password manager app (e.g. Bitwarden) with BAL bypass.
     */
    public static boolean launchPasswordManagerApp(Context context) {
        if (context == null) return false;
        try {
            context.getApplicationContext();
        } catch (Throwable ignored) {
        }
        String autofillPkg = getAutofillServicePackage(context);
        if (autofillPkg == null || autofillPkg.isEmpty()) {
            autofillPkg = "com.x8bit.bitwarden";
        }
        try {
            Intent launchIntent = resolveLaunchIntent(context, autofillPkg);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                Bundle bundle = null;
                if (Build.VERSION.SDK_INT >= 34) {
                    try {
                        ActivityOptions options = ActivityOptions.makeBasic();
                        Method m = ActivityOptions.class.getMethod("setPendingIntentBackgroundActivityStartMode", int.class);
                        m.invoke(options, 1); // MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                        bundle = options.toBundle();
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    PendingIntent pi = PendingIntent.getActivity(
                            context, 1001, launchIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    if (bundle != null) {
                        pi.send(context, 0, null, null, null, null, bundle);
                    } else {
                        pi.send();
                    }
                } catch (Throwable t) {
                    if (bundle != null) {
                        context.startActivity(launchIntent, bundle);
                    } else {
                        context.startActivity(launchIntent);
                    }
                }

                String appName = autofillPkg.contains("bitwarden") ? "Bitwarden" : "Менеджер паролей";
                showToast(context, "Открытие " + appName + " 🔑");
                return true;
            }
        } catch (Throwable t) {
            logw(TAG, "Failed to launch password manager: " + autofillPkg);
        }
        return false;
    }

    /**
     * Triggers the Android Autofill framework on the current input field.
     */
    public static boolean triggerAutofill(View keyboardView) {
        return requestAutofillOnly(keyboardView);
    }

    private static void showToast(Context context, String message) {
        if (context == null || message == null) return;
        sMainHandler.post(() -> {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {
            }
        });
    }
}
