package com.twincoders.twinpush.sdk.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.twincoders.twinpush.sdk.logging.Ln;

/**
 * A utility class to manage notification permission requests for Android 13 (SDK 33) and above.
 * <p>
 * This class simplifies the process of requesting the `POST_NOTIFICATIONS` permission required
 * to display notifications starting from Android SDK 33 (Tiramisu). For older versions of Android,
 * this permission is not required, and the class automatically returns a positive result.
 * </p>
 */
public class PushPermissionRequest {

    /**
     * A callback interface to handle the result of the permission request.
     */
    public interface Callback {
        /**
         * Called when the permission request is completed.
         *
         * @param granted true if the permission was granted, false otherwise.
         */
        void onResult(boolean granted);
    }

    /**
     * Returns the required permission for posting notifications on Android SDK 33 and above.
     *
     * @return The `POST_NOTIFICATIONS` permission string.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String getPushPermission() {
        return Manifest.permission.POST_NOTIFICATIONS;
    }

    private Callback callback = null;
    private final ComponentActivity activity;
    private final ActivityResultLauncher<String> requestPermissionLauncher;

    /**
     * Registers the permission request launcher for a given activity.
     * <p>
     * This method must be called in the `onCreate` method of an activity that extends `ComponentActivity`.
     * </p>
     *
     * @param activity The activity where the permission request will be launched.
     * @return A new instance of `PushPermissionRequest`.
     */
    public static PushPermissionRequest registerForResult(ComponentActivity activity) {
        return new PushPermissionRequest(activity);
    }

    /**
     * Private constructor to initialize the permission request helper.
     *
     * @param activity The activity where the permission request is registered.
     */
    private PushPermissionRequest(ComponentActivity activity) {
        this.activity = activity;
        this.requestPermissionLauncher =
                activity.registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
    }

    /**
     * Handles the result of the permission request.
     *
     * @param granted true if the permission was granted, false otherwise.
     */
    private void onPermissionResult(boolean granted) {
        if (callback != null) {
            callback.onResult(granted);
        }
    }

    /**
     * Launches the notification permission request.
     * <p>
     * If the permission is already granted, the callback is invoked with a `true` result.
     * If the permission has been denied, the callback is invoked with a `false` result.
     * Otherwise, it triggers the system permission request dialog.
     * </p>
     *
     * @param callback The callback to handle the result of the permission request.
     */
    public void launch(Callback callback) {
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Ln.d("Android SDK version >= 33, required to request POST_NOTIFICATIONS permission");
            if (isPermissionGranted(activity)) {
                Ln.d("Permission for POST_NOTIFICATIONS granted!");
                callback.onResult(true);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, getPushPermission())) {
                Ln.d("Permission for POST_NOTIFICATIONS has already been denied");
                callback.onResult(false);
            } else {
                Ln.d("Requesting POST_NOTIFICATIONS permission");
                requestPermissionLauncher.launch(getPushPermission());
            }
        } else {
            Ln.d("Android SDK version < 33, not required to request POST_NOTIFICATIONS permission");
            callback.onResult(true);
        }
    }

    /**
     * Checks if the `POST_NOTIFICATIONS` permission has been granted.
     *
     * @param context The context to check the permission status.
     * @return true if the permission is granted, false otherwise.
     */
    public static boolean isPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context, getPushPermission()) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}