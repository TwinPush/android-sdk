package com.twincoders.twinpush.sdk.demo.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.twincoders.twinpush.sdk.controllers.NotificationListItemView;
import com.twincoders.twinpush.sdk.demo.R;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.text.SimpleDateFormat;

/**
 * Created by agutierrez on 6/11/15.
 */
public class InboxItemView extends LinearLayout implements NotificationListItemView {

    /* Views */
    TextView titleTxt;
    TextView messageTxt;
    TextView dateTxt;

    /* Properties */
    PushNotification notification;

    public InboxItemView(Context context) {
        super(context);
		/* Views */
        LayoutInflater.from(context).inflate(R.layout.inbox_view, this, true);
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
        titleTxt.setVisibility(containsTitle ? View.VISIBLE : View.GONE);
        titleTxt.setText(title);
        messageTxt.setText(message);
        String date = SimpleDateFormat.getDateInstance().format(notification.getDate());
        String hour = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(notification.getDate());
        dateTxt.setText(String.format("%s\n%s", date, hour));
    }

    @Override
    public void setListener(Listener listener) {

    }
}
