package com.yellowpineapple.wakup.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.communications.RequestClient;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.utils.PersistenceHandler;
import com.yellowpineapple.wakup.utils.ShareManager;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

import java.io.IOException;

import lombok.Getter;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity
public abstract class ParentActivity extends FragmentActivity {

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

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Getter private PersistenceHandler persistence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        buildGoogleApiClient();
        requestClient = RequestClient.getSharedInstance(this, RequestClient.Environment.PRODUCTION);
        persistence = PersistenceHandler.getSharedInstance(this);
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Aller_Lt.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Fix portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @OptionsItem(android.R.id.home)
    void onHomePressed() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        slideOutTransition();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Override setTitle method to avoid text ellispis even thouth there is room for all text
     */
    @Override
    public void setTitle(final CharSequence title) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Activity activity = ParentActivity.this;
                ActionBar ab = activity.getActionBar();
                if (ab != null) {
                    ab.setDisplayShowTitleEnabled(false);
                    ParentActivity.super.setTitle(title);
                    ab.setDisplayShowTitleEnabled(true);
                } else {
                    ParentActivity.super.setTitle(title);
                }
            }
        });
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
                    listener.onLocationError(new LocationException("Could not obtain location: Connection suspended"));
                }
            });
            googleApiClient.registerConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    if (mResolvingError) {
                        // If connection to Google Services failed, try to obtain location directly from provider
                        Toast.makeText(getApplicationContext(), "Could not obtain location: Connection to Google API Services failed", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (connectionResult.hasResolution()) {
                        try {
                            mResolvingError = true;
                            connectionResult.startResolutionForResult(ParentActivity.this, REQUEST_RESOLVE_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            // There was an error with the resolution intent. Try again.
                            googleApiClient.connect();
                        }
                    } else {
                        // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                        showErrorDialog(connectionResult.getErrorCode());
                        mResolvingError = true;
                    }
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
            // Notify location to TwinPush
            TwinPushSDK.getInstance(this).setLocation(lastLocation);
        } else {
            listener.onLocationError(new LocationException("Location is empty"));
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        if (!isFinishing()) {
            mResolvingError = false;
            googleApiClient.connect();
        }
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((ParentActivity)getActivity()).onDialogDismissed();
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

    @Override
    protected void onDestroy() {
        if (alert != null) {
            alert.cancel();
        }
        if (mDialog != null) {
            mDialog.cancel();
        }
        super.onDestroy();
    }

    // Actions

    protected void showOfferDetail(Offer offer, Location currentLocation) {
        OfferDetailActivity_.intent(this).offer(offer).location(currentLocation).start();
        slideInTransition();
    }

    protected void displayInMap(Offer offer, Location currentLocation) {
        OfferMapActivity_.intent(this).offer(offer).location(currentLocation).start();
        slideInTransition();
    }

    protected boolean isSavedOffer(Offer offer) {
        PersistenceHandler persistence = getPersistence();
        return persistence.isSavedOffer(offer);
    }

    protected void saveOffer(Offer offer) {
        PersistenceHandler persistence = getPersistence();
        persistence.saveOffer(offer);
    }

    protected void removeSavedOffer(Offer offer) {
        PersistenceHandler persistence = getPersistence();
        persistence.removeSavedOffer(offer);
    }

    protected void openOfferLink(Offer offer) {
        if (offer.isOnline()) {
            //TODO open online link
        }
    }

    /* Activity transitions */

    protected void slideInTransition() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
    }

    protected void slideOutTransition() {
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right);
    }

    /* Offer sharing */

    void shareOffer(final Offer offer) {
        setProgressBarIndeterminateVisibility(true);
        ImageLoader.getInstance().loadImage(offer.getImage().getUrl(), new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true).build(),
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        setProgressBarIndeterminateVisibility(false);
                        displayErrorDialog(getString(R.string.share_offer_error));
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        setProgressBarIndeterminateVisibility(false);
                        String shareTitle = getString(R.string.share_offer_title);
                        String text = String.format(getString(R.string.share_offer_subject), offer.getCompany().getName(), offer.getShortDescription());
                        String fileName = String.format("101_offer_%d.png", offer.getId());
                        ShareManager.shareImage(ParentActivity.this, loadedImage, fileName, shareTitle, text);
                    }
                });
    }

    public void setLoading(boolean loading) {
        setProgressBarIndeterminateVisibility(loading);
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
