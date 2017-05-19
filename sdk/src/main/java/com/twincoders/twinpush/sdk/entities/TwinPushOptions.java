package com.twincoders.twinpush.sdk.entities;

public class TwinPushOptions {

    public final static String DEFAULT_SUBDOMAIN = "app";

    /* Application ID obtained from TwinPush WebApp */
    public String twinPushAppId = null;
    /* Application API Key obtained from TwinPush WebApp */
    public String twinPushApiKey = null;
    /* GCM Project number (aka Sender ID) to receive notifications */
    public String gcmProjectNumber = null;
    /* Custom subdomain for TwinPush server (e.g: domain of 'beta' will result in 'https://beta.twinpush.com' url) */
    public String subdomain = null;
    /* Custom host for the TwinPush server. Default is "https://{{subdomain}}.twinpush.com" */
    public String serverHost = null;

}
