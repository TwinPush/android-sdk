package com.twincoders.twinpush.demo;

import com.twincoders.twinpush.sdk.TwinPushSDK;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;

public class ParentActivity extends Activity {

	ProgressDialog mDialog = null;
	AlertDialog alert = null;
	
	private Handler handler = new Handler();

	public void displayLoadingDialog(int textResId) {
		displayLoadingDialog(getString(textResId));
	}
	
	public void displayLoadingDialog(String text) {
		closeLoadingDialog();
		mDialog = ProgressDialog.show(this, "", text, true);
	}

	public void closeLoadingDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
		mDialog = null;
	}

	public void closeDialog() {
		closeLoadingDialog();
		if (alert != null) {
			alert.dismiss();
		}
		alert = null;
	}
	
	public void displayAlertDialog(int titleTextId, int messageTextId, int buttonTextId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageTextId)
		        .setTitle(titleTextId)
		        .setCancelable(true)
		        .setPositiveButton(buttonTextId, null);
		showDialog(builder);
	}
	
	public void displayErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
		        .setTitle(R.string.error_message_title)
		        .setCancelable(true)
		        .setPositiveButton(R.string.error_message_button, null);
		showDialog(builder);
	}
	
	public void displayConfirmDialog(int titleTextId, int messageTextId, int positiveButtonTextId, int negativeButtonTextId, DialogInterface.OnClickListener positiveListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageTextId).setTitle(titleTextId).setCancelable(true).setPositiveButton(positiveButtonTextId, positiveListener).setNegativeButton(negativeButtonTextId, null);
		showDialog(builder);
	}
	
	public void displayConfirmDialog(int titleTextId, String messageText, int positiveButtonTextId, int negativeButtonTextId, DialogInterface.OnClickListener positiveListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageText).setTitle(titleTextId).setCancelable(true).setPositiveButton(positiveButtonTextId, positiveListener).setNegativeButton(negativeButtonTextId, null);
		showDialog(builder);
	}

	private void showDialog(final AlertDialog.Builder builder) {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				// Close previous dialog (if exists)
				closeDialog();
				alert = builder.create();
				alert.show();
			}
		});
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
