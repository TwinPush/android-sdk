package com.yellowpineapple.offers101.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.communications.RequestClient;

import java.io.IOException;

import lombok.Getter;

public abstract class ParentActivity extends Activity {

    protected class LocationException extends Exception {

        protected LocationException(String message) {
            super(message);
        }

    }
    protected interface LocationListener {
        void onLocationSuccess(Location location);
        void onLocationError(Exception exception);
    }

    @Getter RequestClient requestClient = null;
    @Getter GoogleApiClient googleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        buildGoogleApiClient();
        requestClient = RequestClient.getSharedInstance(this, RequestClient.Environment.PRODUCTION);
        super.onCreate(savedInstanceState);
    }

    /* Google API Service */

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    protected void getLastKnownLocation(final LocationListener listener) {
        if (googleApiClient.isConnected()) {
            loadLocation(googleApiClient, listener);
        } else {
            googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    loadLocation(googleApiClient, listener);
                }

                @Override
                public void onConnectionSuspended(int i) {
                    listener.onLocationError(new LocationException("Connection suspended"));
                }
            });
            googleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    listener.onLocationError(new LocationException("Connection failed"));
                }
            });
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
    }

    private void loadLocation(GoogleApiClient googleApiClient, LocationListener listener) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            listener.onLocationSuccess(lastLocation);
        } else {
            listener.onLocationError(new LocationException("Location is empty"));
        }
    }


    /* Dialogs */

    ProgressDialog mDialog = null;
    AlertDialog alert = null;

    public void displayLoadingDialog() {
        displayLoadingDialog(getString(R.string.loading_default));
    }

    public void displayLoadingDialog(int textId) {
        displayLoadingDialog(getString(textId));
    }

    public void displayLoadingDialog(String text) {
        closeLoadingDialog();
        mDialog = ProgressDialog.show(this, "", text, true);
    }

    public void closeLoadingDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        mDialog = null;
    }

    public void closeDialog() {
        closeLoadingDialog();
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
        }
        alert = null;
    }

    public void displayAlertDialog(int titleTextId, int messageTextId, int buttonTextId) {
        displayAlertDialog(titleTextId, messageTextId, buttonTextId, null);
    }

    public void displayAlertDialog(int titleTextId, int messageTextId, int buttonTextId, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageTextId)
                .setTitle(titleTextId)
                .setCancelable(true)
                .setPositiveButton(buttonTextId, listener);
        displayDialog(builder);
    }

    public void displayErrorDialog(Throwable throwable) {
        if (throwable instanceof IOException) {
            this.displayErrorDialog(getString(R.string.connection_error_message));
        } else {
            this.displayErrorDialog(throwable.getLocalizedMessage());
        }
    }

    public void displayErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(R.string.error_message_title)
                .setCancelable(true)
                .setPositiveButton(R.string.error_message_button, null);
        displayDialog(builder);
    }

    public void displayConfirmDialog(int titleTextId, int messageTextId, int positiveButtonTextId, int negativeButtonTextId, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageTextId).setTitle(titleTextId).setCancelable(true).setPositiveButton(positiveButtonTextId, positiveListener).setNegativeButton(negativeButtonTextId, null);
        displayDialog(builder);
    }

    public void displayConfirmDialog(int titleTextId, String messageText, int positiveButtonTextId, int negativeButtonTextId, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageText).setTitle(titleTextId).setCancelable(true).setPositiveButton(positiveButtonTextId, positiveListener).setNegativeButton(negativeButtonTextId, null);
        displayDialog(builder);
    }

    public void displayConfirmDialog(int titleTextId, int messageTextId, int positiveButtonTextId, int negativeButtonTextId, View customView, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageTextId).setTitle(titleTextId).setCancelable(true).setPositiveButton(positiveButtonTextId, positiveListener).setNegativeButton(negativeButtonTextId, null).setView(customView);
        displayDialog(builder);
    }

    protected void displayDialog(final AlertDialog.Builder builder) {
        if (!isFinishing()) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // Close previous dialog (if exists)
                    closeDialog();
                    alert = builder.create();
                    alert.show();
                }

            });
        }
    }

    protected void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

}
