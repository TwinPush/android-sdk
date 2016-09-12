package com.twincoders.twinpush.sdk.controllers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.List;

public abstract class LegacyNotificationAdapter extends BaseAdapter implements LegacyNotificationItemView.Listener {

    public interface Listener {
        void onNotificationSelected(PushNotification notification);
        void onNotificationLongClicked(PushNotification notification);
    }

    List<PushNotification> notifications;
    Listener listener;
    Context context;

    public LegacyNotificationAdapter(Context context) {
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
        LegacyNotificationItemView view = null;
        LegacyNotificationItemView reuseView = convertView instanceof LegacyNotificationItemView ? (LegacyNotificationItemView) convertView : null;

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

    public abstract LegacyNotificationItemView getViewInstance(Context context);

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

    @Override
    public void onNotificationLongClicked(PushNotification notification) {
        if (listener != null) listener.onNotificationLongClicked(notification);
    }

}