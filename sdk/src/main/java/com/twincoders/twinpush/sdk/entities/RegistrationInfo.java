package com.twincoders.twinpush.sdk.entities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.twincoders.twinpush.sdk.BuildConfig;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class RegistrationInfo {

    /* TwinPush SDK Version */
    public String sdkVersion = null;
    /* Client Application Version */
    public String appVersion = null;
    /* Android version name ("4.2.2", "5.1") */
    public String osVersion = null;
    /* Android API Level (19 for "4.2.2", etc) */
    public Integer osVersionInt = null;
    /* Current device locale ("en_US", "es_ES")*/
    public String language = null;
    /* Device Manufacturer ("samsung", "motorola") */
    public String deviceManufacturer = null;
    /* User readable device model ("Galaxy Nexus", "Moto G3") */
    public String deviceModel = null;
    /* Technical device code */
    public String deviceCode = null;
    /* Device Unique Identifier */
    public String udid = null;

    /* User alias for registration */
    public String deviceAlias = null;
    /* GCM Token for notifications */
    public String pushToken = null;

    /* TwinPush setup parameters */
    public String appID = null;


    public static RegistrationInfo fromContext(Context context, String udid, String deviceAlias, String pushToken) {
        RegistrationInfo info = new RegistrationInfo();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            info.appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Ln.e(e, "Could not obtain application version");
        }
        info.sdkVersion = BuildConfig.VERSION_NAME;
        info.udid = udid;
        info.deviceAlias = deviceAlias;
        info.pushToken = pushToken;
        info.osVersion = Build.VERSION.RELEASE;
        info.osVersionInt = Build.VERSION.SDK_INT;
        info.deviceManufacturer = capitalize(Build.MANUFACTURER);
        info.deviceModel = capitalize(Build.MODEL);
        info.deviceCode = capitalize(Build.DEVICE);
        info.language = Locale.getDefault().toString();
        // TwinPush setup params
        TwinPushSDK twinPush = TwinPushSDK.getInstance(context);
        info.appID = twinPush.getAppId();

        info.printLog();

        return info;
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first) || s.contains("_")) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public void printLog() {
        Ln.d("============================================");
        Ln.d("===      TwinPush Registration info      ===");
        Ln.d("============================================");
        Ln.d("TwinPush App ID: %s", appID);
        Ln.d("App Version:     %s", appVersion);
        Ln.d("SDK Version:     %s", sdkVersion);
        Ln.d("Android Version: %s (API %d)", osVersion, osVersionInt);
        Ln.d("Device:          %s %s (%s)", deviceManufacturer, deviceModel, deviceCode);
        Ln.d("UDID:            %s", udid);
        Ln.d("Language:        %s", language);
        Ln.d("Alias:           %s", deviceAlias);
        Ln.d("Push Token:      %s", pushToken);
        Ln.d("============================================");
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("app_id", appID);
        map.put("app_version", appVersion);
        map.put("sdk_version", sdkVersion);
        map.put("os_version", osVersion);
        map.put("os_version_api", osVersionInt);
        map.put("device_manufacturer", deviceManufacturer);
        map.put("device_model", deviceModel);
        map.put("device_code", deviceCode);
        map.put("udid", deviceCode);
        map.put("language", language);
        map.put("device_alias", deviceAlias);
        map.put("push_token", pushToken);
        return map;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : toMap().entrySet()) {
            builder.append(entry.getKey());
            builder.append("=");
            builder.append(entry.getValue());
            builder.append(";");
        }
        return builder.toString();
    }

}
