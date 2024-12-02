package com.twincoders.twinpush.sdk.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.activities.RichNotificationActivity;
import com.twincoders.twinpush.sdk.entities.TwinPushOptions;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.logging.Strings;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.services.NotificationIntentService;
import com.twincoders.twinpush.sdk.util.PushPermissionRequest;

import java.util.Arrays;

import fr.ganfra.materialspinner.MaterialSpinner;

public class MainActivity extends ParentActivity {

    // Preferences
    static final String PREFS = "shared";
    static final String PREF_AGE = "age";
    static final String PREF_STATUS = "status";

    PushPermissionRequest pushPermissionRequest;

    Button mRegisterButton;
    Button mInfoButton;
    TwinPushSDK twinPush;

    View mRegisterProgress;
    View mRegisterForm;

    MaterialSpinner statusSpinner;
    TextView ageTxt;
    TextView usernameTxt;

    SharedPreferences getPreferences() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterProgress = findViewById(R.id.register_progress);
        mRegisterForm = findViewById(R.id.register_form);
        mInfoButton = findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(this::showInfo);
        mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(this::register);

        ageTxt = findViewById(R.id.age);
        usernameTxt = findViewById(R.id.username);
        statusSpinner = findViewById(R.id.status);

        // Setup "status" dropdown
        String[] statusArray = getResources().getStringArray(R.array.status_arrays);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        twinPush = TwinPushSDK.getInstance(this);
        // Setup TwinPush SDK
        TwinPushOptions options = new TwinPushOptions();                // Initialize options
        options.twinPushAppId =     "816b6f7f129b5982";                 // - APP ID
        options.twinPushApiKey =    "17c1d1fc0804fd57038e4062779f144d"; // - API Key
        options.subdomain =         "pre";                              // - Application subdomain
        twinPush.setup(options);                                        // Call setup

        // Show previous values when present
        usernameTxt.setText(twinPush.getDeviceAlias());
        if (getPreferences().contains(PREF_AGE)) {
            ageTxt.setText(String.valueOf(getPreferences().getInt(PREF_AGE, 0)));
        }
        if (getPreferences().contains(PREF_STATUS)) {
            String currentStatus = getPreferences().getString(PREF_STATUS, null);
            statusSpinner.setSelection(Arrays.asList(statusArray).indexOf(currentStatus) + 1);
        }

        pushPermissionRequest = PushPermissionRequest.registerForResult(this);

        // Check push notification
        checkPushNotification(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        checkPushNotification(intent);
        super.onNewIntent(intent);
    }

    // Layout events
    public void register(View view) {
        showProgress(true);
        final TwinPushSDK.OnRegistrationListener listener = new TwinPushSDK.OnRegistrationListener() {
            @Override
            public void onRegistrationError(Exception exception) {
                showProgress(false);
                Snackbar.make(mRegisterButton, exception.getLocalizedMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            @Override
            public void onRegistrationSuccess(String deviceAlias) {
                String pushPermission = PushPermissionRequest.isPermissionGranted(MainActivity.this) ? "granted" : "NOT granted";
                String message = String.format("Successfully registered to TwinPush! Push permissions %s", pushPermission);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                // Set custom properties to TwinPush
                twinPush.clearProperties();
                twinPush.setProperty("age", getAge());
                twinPush.setEnumProperty("Estado civil", getStatus());
                // Store properties locally
                SharedPreferences.Editor preferences = getPreferences().edit();
                if (getStatus() != null) {
                    preferences.putString(PREF_STATUS, getStatus());
                } else {
                    preferences.remove(PREF_STATUS);
                }
                if (getAge() != null) {
                    preferences.putInt(PREF_AGE, getAge());
                } else {
                    preferences.remove(PREF_AGE);
                }
                preferences.apply();

                Intent intent = new Intent(MainActivity.this, InboxActivity.class);
                startActivity(intent);

                showProgress(false);
            }
        };
        pushPermissionRequest.launch(granted -> twinPush.register(getUsername(), listener));
    }

    public void showInfo(View view) {
        String message;
        String deviceId = twinPush.getDeviceId();
        if (Strings.notEmpty(deviceId)) {
            message = String.format("Device registered with ID: %s", deviceId);
        } else {
            message = "Device not registered to TwinPush";
        }
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    // Getter for UI values

    String getUsername() {
        return usernameTxt.getText().toString();
    }

    Integer getAge() {
        String age = ageTxt.getText().toString();
        return Strings.isEmpty(age) ? null : Integer.parseInt(age);
    }

    String getStatus() {
        return (String) statusSpinner.getSelectedItem();
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    private void showProgress(final boolean show) {
        super.showProgress(show, mRegisterProgress, mRegisterForm);
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

    @Override
    protected void onStart() {
        TwinPushSDK.getInstance(this).activityStart(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        TwinPushSDK.getInstance(this).activityStop(this);
        super.onStop();
    }
}
