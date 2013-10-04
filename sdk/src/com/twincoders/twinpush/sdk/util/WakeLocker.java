package com.twincoders.twinpush.sdk.util;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WakeLocker {
	
	static long WAKE_LOCK_DELAY = 5000;
	
	@SuppressWarnings("deprecation")
	public static void acquire(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        final WakeLock wakeLock = pm.newWakeLock(
        		PowerManager.FULL_WAKE_LOCK |
        		PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "WakeLock");
        wakeLock.acquire(5000);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (wakeLock.isHeld()) {
					wakeLock.release();
				}
			}
        	
        }, WAKE_LOCK_DELAY);
    }
}
