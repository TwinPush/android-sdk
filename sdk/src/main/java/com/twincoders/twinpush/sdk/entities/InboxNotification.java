package com.twincoders.twinpush.sdk.entities;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.Date;

/**
 * Notification wrapper for a message received in the user inbox
 */
public class InboxNotification {

    String id;
    Date createdAt;
    Date openAt;
    PushNotification notification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getOpenAt() {
        return openAt;
    }

    public void setOpenAt(Date openAt) {
        this.openAt = openAt;
    }

    public PushNotification getNotification() {
        return notification;
    }

    public void setNotification(PushNotification notification) {
        this.notification = notification;
    }
}
