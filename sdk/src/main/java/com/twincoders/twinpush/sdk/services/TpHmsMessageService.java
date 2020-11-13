package com.twincoders.twinpush.sdk.services;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

public class TpHmsMessageService extends HmsMessageService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Ln.d("TwinPush Huawei HMS on new token called");

        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());

        // Get updated InstanceID token.
        Ln.d("Huawei HMS Token created: " + s);

        // Refresh register if needed
        if (twinPush.isDeviceRegistered()) {
            twinPush.register();
        }
    }

    @Override
    public void onTokenError(Exception e) {
        Ln.e(e, "Error obtaining Huawei Push Token");
        super.onTokenError(e);
    }
}
