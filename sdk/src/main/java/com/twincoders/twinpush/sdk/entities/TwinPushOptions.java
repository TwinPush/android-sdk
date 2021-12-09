package com.twincoders.twinpush.sdk.entities;

import com.twincoders.twinpush.sdk.services.SilentPushReceiver;

public class TwinPushOptions {

    public final static String DEFAULT_SUBDOMAIN = "app";

    /* Application ID obtained from TwinPush WebApp */
    public String twinPushAppId = null;
    /* Application API Key obtained from TwinPush WebApp */
    public String twinPushApiKey = null;
    /* Custom subdomain for TwinPush server (e.g: domain of 'beta' will result in 'https://beta.twinpush.com' url) */
    public String subdomain = null;
    /* Custom host for the TwinPush server. Default is "https://{{subdomain}}.twinpush.com" */
    public String serverHost = null;
    /* If set to true, the SDK will not make the registration request to the TwinPush API. Instead, it will notify the intent via broadcast**/
    public RegistrationMode registrationMode = RegistrationMode.INTERNAL;
    /* If set to true, the NotificationIntentService will acknowledge to the TwinPush API when a notification is received.
    * For this function to properly work, it also has to be enabled for the current app license */
    public boolean pushAckEnabled = false;
    /* Preferred platform in case that the device can be registered as multiple*/
    public Platform preferredPlatform = Platform.ANDROID;
    /* Class to receive event when a silent notification is received */
    public Class<? extends SilentPushReceiver> silentPushReceiverClass = null;

}
