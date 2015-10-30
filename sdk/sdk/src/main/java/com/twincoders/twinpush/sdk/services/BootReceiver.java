package com.twincoders.twinpush.sdk.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twincoders.twinpush.sdk.logging.Ln;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Ln.i("Received boot intent");
		// Launch location service
		context.startService(new Intent(context, LocationService.class));
	}

}
