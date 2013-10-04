package com.twincoders.twinpush.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.twincoders.twinpush.demo.util.CommonUtilities;
import com.twincoders.twinpush.demo.util.ConnectionDetector;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.TwinPushSDK.OnRegistrationListener;
import com.twincoders.twinpush.sdk.activities.RichNotificationActivity;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.notifications.TwinPushIntentService;
import com.twincoders.twinpush.sdk.util.WakeLocker;

public class MainActivity extends ParentActivity {

	// TwinPush Token & API Key
	private static final String TWINPUSH_TOKEN = "-----------";
	private static final String TWINPUSH_APP_ID = "----------";

	// GCM Google Project number
  public static final String SENDER_ID = "----------";


	// Connection detector
    ConnectionDetector cd;

    /* Views */
    ImageView twinPushLogo;
	EditText aliasEdit;
	Button registerButton;
	View mainView;
	EditText ageEdit;
	RadioGroup genderGroup;

	/* Properties */
	String registrationId = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Views */
        setContentView(R.layout.main_activity);
        aliasEdit = (EditText) findViewById(R.id.aliasEdit);
        ageEdit = (EditText) findViewById(R.id.ageEdit);
        genderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        mainView = findViewById(R.id.mainView);
        twinPushLogo = (ImageView) findViewById(R.id.twinPushLogo);
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onRegisterButtonClicked();
			}
		});
        twinPushLogo.setLongClickable(true);
        twinPushLogo.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());
				Location location = twinPush.getLastKnownLocation();
				if (location != null) {
					Toast.makeText(getApplicationContext(), String.format("Current location: %f, %f", location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "No location saved", Toast.LENGTH_LONG).show();
				}
				return false;
			}
		});
        /* End of Views */

        /* TwinPush setup */
        TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
        twinPush.setup(TWINPUSH_APP_ID, TWINPUSH_TOKEN, SENDER_ID);
        twinPush.setNotificationSmallIcon(R.drawable.ic_notification);
        twinPush.register();
        twinPush.updateLocation();
        twinPush.startMonitoringLocationChanges();
        String alias = twinPush.getDeviceAlias();
        aliasEdit.setText(alias);

        cd = new ConnectionDetector(getApplicationContext());

        registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION));

        // Recover saved user info
        int age = getSharedPrefs().getInt("age", -1);
        if (age > 0) {
        	ageEdit.setText(String.valueOf(age));
        }
        int gender = getSharedPrefs().getInt("gender", -1);
        if (gender > 0) {
        	genderGroup.check(gender);
        }

        mainView.requestFocusFromTouch();

        // Get notification
        checkPushNotification(getIntent());
    }

	@Override
	protected void onNewIntent(Intent intent) {
		checkPushNotification(intent);
		super.onNewIntent(intent);
	}

	// Checks if the intent contains a Push notification and displays rich content when appropriated
	void checkPushNotification(Intent intent) {
		if (intent.getAction().equals(TwinPushIntentService.ON_NOTIFICATION_OPENED_ACTION)) {
			PushNotification notification = (PushNotification) intent.getSerializableExtra(TwinPushIntentService.EXTRA_NOTIFICATION);
			if (notification != null) {
				TwinPushSDK.getInstance(this).onNotificationOpen(notification);
				if (notification.isRichNotification()) {
					Intent richIntent = new Intent(this, RichNotificationActivity.class);
					richIntent.putExtra(TwinPushIntentService.EXTRA_NOTIFICATION, notification);
					startActivity(richIntent);
				}
			}
		}
	}

    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
        	// Waking up mobile if it is sleeping
        	WakeLocker.acquire(getApplicationContext());

        	/**
        	 * Take appropriate action on this message
        	 * depending upon your app requirement
        	 * For now i am just displaying it on the screen
        	 * */

        	// Showing received message
        	Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onDestroy() {
    	try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }

    void onRegisterButtonClicked() {
		Ln.i("Register button clicked");
		// Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
        	displayAlertDialog(R.string.gcm_internet_connection_error_title, R.string.gcm_internet_connection_error_message, R.string.error_message_button);
		} else {
			displayLoadingDialog(R.string.register_loading);
			String alias = aliasEdit.getText().toString();

			TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
			twinPush.register(alias, new OnRegistrationListener() {

				@Override
				public void onRegistrationSuccess(String deviceAlias) {
					closeLoadingDialog();
					Toast.makeText(getApplicationContext(), R.string.register_device_registered, Toast.LENGTH_LONG).show();
					Intent intent = new Intent(getApplicationContext(), NotificationInboxActivity.class);
					startActivity(intent);
				}

				@Override
				public void onRegistrationError(Exception exception) {
					closeLoadingDialog();
					displayErrorDialog(exception.getLocalizedMessage());
				}
			});
			twinPush.clearProperties();
			twinPush.setProperty("age", getAge());
			twinPush.setProperty("gender", getGender());
			// Save user info
			Editor editor = getSharedPrefs().edit().
					putInt("gender", genderGroup.getCheckedRadioButtonId());
			if (getAge() != null) {
				editor.putInt("age", getAge());
			} else {
				editor.remove("age");
			}
			editor.commit();
		}
	}

    Integer getAge() {
    	String ageString = ageEdit.getText().toString();
		Integer age = ageString.trim().length() > 0 ? Integer.parseInt(ageString) : null;
		return age;
    }

    String getGender() {
    	int index = genderGroup.indexOfChild(findViewById(genderGroup.getCheckedRadioButtonId()));
    	return index == 0 ? "Male" : "Female";
    }

    SharedPreferences getSharedPrefs() {
    	return getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
    }

}
