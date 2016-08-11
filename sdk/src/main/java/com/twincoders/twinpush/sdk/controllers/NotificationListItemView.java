package com.twincoders.twinpush.sdk.controllers;

import com.twincoders.twinpush.sdk.entities.InboxNotification;

public interface NotificationListItemView {
	
	public interface Listener {
		void onNotificationSelected(InboxNotification notification);
		void onNotificationLongClicked(InboxNotification notification);
	}
	
	void setListener(Listener listener);
	void setNotification(InboxNotification notification);

}
