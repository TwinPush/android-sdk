package com.yellowpineapple.wakup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.activities.RichNotificationActivity;
import com.twincoders.twinpush.sdk.entities.TwinPushOptions;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.services.NotificationIntentService;
import com.yellowpineapple.wakup.sdk.activities.OffersActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* TwinPush setup */
        TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
        // Setup TwinPush SDK
        TwinPushOptions options = new TwinPushOptions();                // Initialize options
        options.twinPushAppId =     "afb821e1c8c715c7";                 // - APP ID
        options.twinPushApiKey =    "965aac21649e505ab3d1bc9e9402b8ff"; // - API Key
        options.gcmProjectNumber =  "614578197410";                     // - GCM Project Number
        options.subdomain =         TwinPushOptions.DEFAULT_SUBDOMAIN;  // - Application subdomain
        //options.notificationIcon =  R.drawable.ic_action_logo;          // - Notification icon
        twinPush.setup(options);                                        // Call setup
        twinPush.register();

        // Check push notification
        checkPushNotification(getIntent());

        OffersActivity.intent(this).start();
    }

    // Push notifications

    @Override
    protected void onNewIntent(Intent intent) {
        checkPushNotification(intent);
        super.onNewIntent(intent);
    }

    // Checks if the intent contains a Push notification and displays rich content when appropriated
    void checkPushNotification(Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals(NotificationIntentService.ON_NOTIFICATION_OPENED_ACTION)) {
            PushNotification notification = (PushNotification) intent.getSerializableExtra(NotificationIntentService.EXTRA_NOTIFICATION);
            TwinPushSDK.getInstance(this).onNotificationOpen(notification);

            if (notification != null && notification.isRichNotification()) {
                Intent richIntent = new Intent(this, RichNotificationActivity.class);
                richIntent.putExtra(NotificationIntentService.EXTRA_NOTIFICATION, notification);
                startActivity(richIntent);
            }
        }
    }

    /* TwinPush Callbacks */
    @Override
    protected void onStart() {
        TwinPushSDK.getInstance(this).activityStart(this);
        super.onStart();
    };

    @Override
    protected void onStop() {
        TwinPushSDK.getInstance(this).activityStop(this);
        super.onStop();
    }
}
