/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarSwipeRuntime;

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
                if (context.getPackageManager().getLaunchIntentForPackage(pkg) != null) {
                    return pkg;
                }
            } catch (Throwable ignored) {
            }
        }
        return "com.x8bit.bitwarden";
    }

    /**
     * Triggers the Android Autofill framework and launches the active password manager (Bitwarden).
     *
     * @param keyboardView the keyboard view
     * @return true if action was triggered
     */
    public static boolean triggerAutofill(View keyboardView) {
        if (keyboardView == null) {
            return false;
        }

        Context context = keyboardView.getContext();
        try {
            keyboardView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        } catch (Throwable ignored) {
        }

        // 1. Dispatch standard Android ContextMenuAction for requesting Autofill on focused view
        InputMethodService ims = GboardSpacebarSwipeRuntime.findInputMethodService(keyboardView);
        if (ims != null) {
            InputConnection ic = ims.getCurrentInputConnection();
            if (ic != null) {
                try {
                    boolean performed = ic.performContextMenuAction(AUTOFILL_CONTEXT_MENU_ID);
                    logi(TAG, "performContextMenuAction(autofill) result: " + performed);
                } catch (Throwable t) {
                    logw(TAG, "Exception during performContextMenuAction(autofill)");
                }
            }
        }

        // 2. Launch the active password manager (Bitwarden)
        String autofillPkg = getAutofillServicePackage(context);
        boolean launched = false;
        if (autofillPkg != null && !autofillPkg.isEmpty()) {
            try {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(autofillPkg);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(launchIntent);
                    launched = true;
                    String appName = autofillPkg.contains("bitwarden") ? "Bitwarden" : "Менеджер паролей";
                    showToast(context, "Открытие " + appName + " 🔑");
                    logi(TAG, "Successfully launched password manager: " + autofillPkg);
                }
            } catch (Throwable t) {
                logw(TAG, "Failed to launch password manager: " + autofillPkg);
            }
        }

        if (!launched) {
            showToast(context, "Запрос автозаполнения отправлен 🔑");
        }

        return true;
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
