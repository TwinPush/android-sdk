package com.twincoders.twinpush.sdk.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.notifications.TwinPushIntentService;

public class RichNotificationActivity extends Activity {
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Setup view
		WebView webView = new WebView(this);
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient());
		webView.clearCache(true);
		webView.clearHistory();
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		// Get notification
		Intent intent = getIntent();
		PushNotification notification = (PushNotification) intent.getSerializableExtra(TwinPushIntentService.EXTRA_NOTIFICATION);
		webView.loadUrl(notification.getRichURL());
		setContentView(webView);
	}
}
