package com.twincoders.twinpush.sdk.demo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Strings;

import fr.ganfra.materialspinner.MaterialSpinner;

public class MainActivity extends AppCompatActivity {

    // GCM Google Project number
    public static final String SENDER_ID = "843900965729";
    public static final String API_KEY = "48d455a6967761fdb53c04050f78a21d";
    public static final String APP_ID = "4b7fffda0325a7b1";

    Button mRegisterButton;
    TwinPushSDK twinPushSDK;

    View mRegisterProgress;
    View mRegisterForm;

    MaterialSpinner statusSpinner;
    TextView ageTxt;
    TextView usernameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRegisterProgress = findViewById(R.id.register_progress);
        mRegisterForm = findViewById(R.id.register_form);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.status_arrays));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner = (MaterialSpinner) findViewById(R.id.status);
        statusSpinner.setAdapter(adapter);
        ageTxt = (TextView) findViewById(R.id.age);
        usernameTxt = (TextView) findViewById(R.id.username);

       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message;
                String deviceId = twinPushSDK.getDeviceId();
                if (Strings.notEmpty(deviceId)) {
                    message = String.format("Device registered with ID: %s", deviceId);
                } else {
                    message = "Device not registered to TwinPush";
                }
                Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Setup TwinPush SDK
        twinPushSDK = TwinPushSDK.getInstance(this);
        twinPushSDK.setServerHost("http://pre.twinpush.com");
        twinPushSDK.setNotificationSmallIcon(R.mipmap.ic_launcher);
        twinPushSDK.setup(APP_ID, API_KEY, SENDER_ID);
    }

    void register() {
        showProgress(true);
        twinPushSDK.register(getUsername(), new TwinPushSDK.OnRegistrationListener() {
            @Override
            public void onRegistrationError(Exception exception) {
                showProgress(false);
                Snackbar.make(mRegisterButton, exception.getLocalizedMessage(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }

            @Override
            public void onRegistrationSuccess(String deviceAlias) {
                showProgress(false);
                Snackbar.make(mRegisterButton, "Success!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                // Set custom properties
                twinPushSDK.clearProperties();
                twinPushSDK.setProperty("age", getAge());
                twinPushSDK.setProperty("status", getStatus());
            }
        });
    }

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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mRegisterProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mRegisterProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
