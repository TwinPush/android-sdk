package com.twincoders.twinpush.sdk.notifications;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class TwinPushReceiver extends GCMBroadcastReceiver {	
	
	@Override
	protected String getGCMIntentServiceClassName(Context context) { 
		return TwinPushIntentService.class.getName(); 
	} 

}
