package com.twincoders.twinpush.sdk.services;

import android.content.Context;
import androidx.annotation.Nullable;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.huawei.hms.common.ApiException;
import com.twincoders.twinpush.sdk.logging.Ln;

public class HmsStatus {

    public static boolean isHuaweiServicesAvailable(Context context) {
        return (HuaweiMobileServicesUtil.isHuaweiMobileServicesAvailable(context) ==
                com.huawei.hms.api.ConnectionResult.SUCCESS) ||
                (HuaweiMobileServicesUtil.isHuaweiMobileServicesAvailable(context) ==
                        com.huawei.hms.api.ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED);
    }

    @Nullable
    public static String getHMSToken(Context context) {
        try {
            // read from agconnect-services.json
            String appId = new AGConnectOptionsBuilder().build(context).getString("client/app_id");
            String token = HmsInstanceId.getInstance(context).getToken(appId, "HCM");
            Ln.i("Obtained HMS Token: %s:", token);
            return token;
        } catch (ApiException e) {
            Ln.e(e, "Error obtaining HMS push token");
            return null;
        }
    }
}
