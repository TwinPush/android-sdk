package com.twincoders.twinpush.demo;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.twincoders.twinpush.sdk.controllers.NotificationListItemView;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class NotificationItemView extends RelativeLayout implements NotificationListItemView {
	
	/* Views */
	TextView titleTxt;
	TextView messageTxt;
	TextView dateTxt;
	
	/* Properties */
	PushNotification notification;

	public NotificationItemView(Context context) {
		super(context);
		/* Views */
		LayoutInflater.from(context).inflate(R.layout.notif_list_item_view, this, true);
		titleTxt = (TextView) findViewById(R.id.titleTxt);
		messageTxt = (TextView) findViewById(R.id.messageTxt);
		dateTxt = (TextView) findViewById(R.id.dateTxt);
	}
	
	@Override
	public void setNotification(PushNotification notification) {
		this.notification = notification;
		String title = notification.getTitle();
		String message = notification.getMessage();
		boolean containsTitle = title.trim().length() > 0;
		messageTxt.setVisibility(containsTitle ? View.VISIBLE : View.GONE);
		if (containsTitle) {
			titleTxt.setText(title);
			messageTxt.setText(message);
		} else {
			titleTxt.setText(message);
		}
		String date = SimpleDateFormat.getDateInstance().format(notification.getDate());
		String hour = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(notification.getDate());
		dateTxt.setText(String.format("%s\n%s", date, hour));
	}

	@Override
	public void setListener(final Listener listener) {
		this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listener.onNotificationSelected(notification);
			}
		});
	}

}
