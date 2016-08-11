package com.twincoders.twinpush.sdk.demo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.twincoders.twinpush.sdk.controllers.NotificationListItemView;
import com.twincoders.twinpush.sdk.demo.views.InboxItemView;
import com.twincoders.twinpush.sdk.entities.InboxNotification;

import java.util.List;

/**
 * Created by agutierrez on 5/11/15.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    private Context context;
    private List<InboxNotification> notifications;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public NotificationListItemView mNotificationView;
        public ViewHolder(NotificationListItemView v) {
            super((View) v);
            mNotificationView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public InboxAdapter(Context context, List<InboxNotification> notifications) {
        this.notifications = notifications;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        InboxItemView v = new InboxItemView(context);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mNotificationView.setNotification(notifications.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    // Getter and setter for data set
    public void setNotifications(List<InboxNotification> notifications) {
        this.notifications = notifications;
    }

    public List<InboxNotification> getNotifications() {
        return notifications;
    }
}