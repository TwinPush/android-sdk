package com.twincoders.twinpush.sdk.entities;

import com.twincoders.twinpush.sdk.services.RegistrationIntentReceiver;

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
    /* Class for the registration receiver */
    public Class<? extends RegistrationIntentReceiver> registrationReceiver = null;

}
