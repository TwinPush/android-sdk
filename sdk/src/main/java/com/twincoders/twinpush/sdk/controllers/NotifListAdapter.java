package com.twincoders.twinpush.sdk.controllers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.entities.InboxNotification;

import java.util.List;

public abstract class NotifListAdapter extends BaseAdapter implements NotificationListItemView.Listener {

	public interface Listener {
		void onNotificationSelected(InboxNotification notification);
		void onNotificationLongClicked(InboxNotification notification);
	}

	List<InboxNotification> notifications;
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
	public InboxNotification getItem(int position) {
		InboxNotification n = null;
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
			final InboxNotification notification = getItem(position);
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
	
	public void setNotifications(List<InboxNotification> notifications) {
		this.notifications = notifications;
	}
	
	public List<InboxNotification> getNotifications() {
		return notifications;
	}
	
	@Override
	public void onNotificationSelected(InboxNotification notification) {
		if (listener != null) {
			TwinPushSDK.getInstance(context).onNotificationOpen(notification.getNotification());
			listener.onNotificationSelected(notification);
		}
	}
	
	@Override
	public void onNotificationLongClicked(InboxNotification notification) {
		if (listener != null) listener.onNotificationLongClicked(notification);
	}

}
