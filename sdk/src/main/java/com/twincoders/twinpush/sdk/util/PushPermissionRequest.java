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

public class PushPermissionRequest {

    public interface Callback {
        void onResult(boolean granted);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String getPushPermission() {
        return Manifest.permission.POST_NOTIFICATIONS;
    }

    private Callback callback = null;
    private final ComponentActivity activity;
    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public static PushPermissionRequest registerForResult(ComponentActivity activity) {
        return new PushPermissionRequest(activity);
    }

    private PushPermissionRequest(ComponentActivity activity) {
        this.activity = activity;
        this.requestPermissionLauncher =
                activity.registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
    }

    private void onPermissionResult(boolean granted) {
        if (callback != null) {
            callback.onResult(granted);
        }
    }

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

    public static boolean isPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context, getPushPermission()) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
