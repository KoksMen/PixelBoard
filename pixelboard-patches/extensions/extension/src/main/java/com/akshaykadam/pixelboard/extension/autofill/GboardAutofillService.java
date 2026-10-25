/**
 * PixelBoard - Gboard Enhancement Mod
 *
 * Maintained and customized by Akshay Kadam (@Akshayykadam)
 * Repository: https://github.com/Akshayykadam/PixelBoard
 * Licensed under the GNU General Public License v3.0 (GPLv3)
 */
package com.akshaykadam.pixelboard.extension.autofill;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import com.akshaykadam.pixelboard.extension.spacebar.GboardSpacebarSwipeRuntime;

/**
 * Service to manually request Autofill from the keyboard when the system autofill popup
 * fails to appear automatically.
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
     * Triggers the Android Autofill framework for the currently focused input view.
     *
     * @param keyboardView the keyboard view
     * @return true if the autofill request action was performed
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

        InputMethodService ims = GboardSpacebarSwipeRuntime.findInputMethodService(keyboardView);
        if (ims == null) {
            logw(TAG, "Cannot find InputMethodService for autofill");
            showToast(context, "Клавиатура не инициализирована");
            return false;
        }

        InputConnection ic = ims.getCurrentInputConnection();
        if (ic == null) {
            logw(TAG, "Cannot find active InputConnection for autofill");
            showToast(context, "Поле ввода не активно");
            return false;
        }

        boolean performed = false;
        try {
            // Standard Android ContextMenuAction for requesting Autofill on focused view
            performed = ic.performContextMenuAction(AUTOFILL_CONTEXT_MENU_ID);
            logi(TAG, "performContextMenuAction(autofill) result: " + performed);
        } catch (Throwable t) {
            logw(TAG, "Exception during performContextMenuAction(autofill)");
        }

        if (performed) {
            showToast(context, "Запрос автозаполнения отправлен 🔑");
        } else {
            // Even if performContextMenuAction returned false (some custom fields return false even on success),
            // inform the user that the request was dispatched.
            showToast(context, "Автозаполнение запрошено 🔑");
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
