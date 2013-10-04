package com.twincoders.twinpush.sdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.notifications.TwinPushIntentService;

public class RichNotificationActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup view
		WebView webView = new WebView(this);
		webView.setWebViewClient(new WebViewClient() {
		    public boolean shouldOverrideUrlLoading(WebView view, String url){
		        view.loadUrl(url);
		        return false; // then it is not handled by default action
		   }
		});
		// Get notification
		Intent intent = getIntent();
		PushNotification notification = (PushNotification) intent.getSerializableExtra(TwinPushIntentService.EXTRA_NOTIFICATION);
		webView.loadUrl(notification.getRichURL());
		setContentView(webView);
	}
}
