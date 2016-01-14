package com.yellowpineapple.wakup.sdk.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yellowpineapple.wakup.sdk.utils.IntentBuilder;

/**
 * Created by agutierrez on 13/02/15.
 */
public class WebViewActivity extends ParentActivity {

    public final static String TITLE_EXTRA = "title";
    public final static String URL_EXTRA = "url";
    String title = null;
    String url = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectExtras();

        // Setup view
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebViewClient() {

            boolean firstLoad = true;

            @Override
            public void onPageFinished(WebView view, String url) {
                setLoading(false);
                firstLoad = false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!firstLoad) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

        });
        // Get notification
        setLoading(true);
        if (url != null) webView.loadUrl(url);
        if (title != null) setTitle(title);
        setContentView(webView);
    }

    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            if (extras.containsKey(TITLE_EXTRA)) {
                title = extras.getString(TITLE_EXTRA);
            }
            if (extras.containsKey(URL_EXTRA)) {
                url = extras.getString(URL_EXTRA);
            }
        }
    }

    // Builder

    public static Builder intent(Context context) {
        return new Builder(context);
    }

    public static class Builder extends IntentBuilder<WebViewActivity> {

        public Builder(Context context) {
            super(WebViewActivity.class, context);
        }

        public Builder title(String title) {
            getIntent().putExtra(TITLE_EXTRA, title);
            return this;
        }

        public Builder url(String url) {
            getIntent().putExtra(URL_EXTRA, url);
            return this;
        }
    }

}
