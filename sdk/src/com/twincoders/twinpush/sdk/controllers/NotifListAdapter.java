package com.twincoders.twinpush.sdk.controllers;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public abstract class NotifListAdapter extends BaseAdapter implements NotificationListItemView.Listener {

	public interface Listener {
		void onNotificationSelected(PushNotification notification);
	}

	List<PushNotification> notifications;
	Listener listener;
	Context context;

	public NotifListAdapter(Context context) {
		super();
		this.context = context;
	}
	
	@Override
	public int getCount() {
		int count = 0;
		if (notifications != null) {
			count = notifications.size();
		}
		return count;
	}

	@Override
	public PushNotification getItem(int position) {
		PushNotification n = null;
		if (position < notifications.size()) {
			n = notifications.get(position); 
		}
		return n;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NotificationListItemView view = null;
		NotificationListItemView reuseView = convertView instanceof NotificationListItemView ? (NotificationListItemView) convertView : null;

		// Reuse or create the view
		if (convertView != null && reuseView != null) {
			view = reuseView;
		} else {
			view = getViewInstance(context);
		}

		if (view != null) {
			// Set the view contents
			final PushNotification notification = getItem(position);
			view.setNotification(notification);
			view.setListener(this);
		}
		return (View) view;
	}
	
	public abstract NotificationListItemView getViewInstance(Context context);
	
	/* Getters & Setters */
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public void setNotifications(List<PushNotification> notifications) {
		this.notifications = notifications;
	}
	
	public List<PushNotification> getNotifications() {
		return notifications;
	}
	
	@Override
	public void onNotificationSelected(PushNotification notification) {
		if (listener != null) {
			TwinPushSDK.getInstance(context).onNotificationOpen(notification);
			listener.onNotificationSelected(notification);
		}
	}

}
