package com.twincoders.twinpush.demo;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.activities.RichNotificationActivity;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.controllers.NotifListAdapter;
import com.twincoders.twinpush.sdk.controllers.NotificationListItemView;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.notifications.TwinPushIntentService;


public class NotificationInboxActivity extends ParentActivity implements GetNotificationsRequest.Listener, NotifListAdapter.Listener {
	
	/* Views */
	ListView listView;
	TextView emptyTxt;
	
	/* Properties */
	NotifListAdapter adapter;
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/* Views */
		setContentView(R.layout.notif_inbox_activity);
		listView = (ListView) findViewById(R.id.listView);
		emptyTxt= (TextView) findViewById(R.id.emptyTxt);
		/* Setup list adapter */
		adapter = new NotifListAdapter(this) {

			@Override
			public NotificationListItemView getViewInstance(Context context) {
				return new NotificationItemView(context);
			}
			
		};
		adapter.setListener(this);
		listView.setAdapter(adapter);
		
		/* Start loading */
		loadNotifications(0);
	}
	
	void loadNotifications(int page) {
		emptyTxt.setVisibility(View.GONE);
		displayLoadingDialog(R.string.notif_inbox_loading);
		TwinPushSDK.getInstance(this).getNotifications(page, 20, this);
	}

	@Override
	public void onError(Exception exception) {
		// TODO Display error when empty results stop returning error
		//displayErrorDialog(exception.getLocalizedMessage());
		closeLoadingDialog();
		emptyTxt.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSuccess(final List<PushNotification> notifications, int totalPages) {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				closeLoadingDialog();
				emptyTxt.setVisibility(notifications != null && !notifications.isEmpty() ? View.GONE : View.VISIBLE);
				adapter.setNotifications(notifications);
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onNotificationSelected(PushNotification notification) {
		if (notification.isRichNotification()) {
			Intent richIntent = new Intent(this, RichNotificationActivity.class);
			richIntent.putExtra(TwinPushIntentService.EXTRA_NOTIFICATION, notification);
			startActivity(richIntent);
		}
	}

}
