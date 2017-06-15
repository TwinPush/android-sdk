# TwinPush SDK Library

[![Download](https://api.bintray.com/packages/twinpush/sdk/android-sdk/images/download.svg)](https://bintray.com/twinpush/sdk/android-sdk/_latestVersion)
[![License](https://go-shields.herokuapp.com/license-MIT-blue.png)](https://raw.githubusercontent.com/TwinPush/android-sdk/master/LICENSE)


Native Android SDK library for [TwinPush](http://twinpush.com) platform.

## Setup Firebase Cloud Messaging

TwinPush uses Firebase Cloud Messaging (FCM) to deliver Push Notifications to Android devices.

To use this service, it is necessary to access to the [Firebase Console](https://console.firebase.google.com) and perform the following steps:

 1. Create a Project or import it from previous Google Cloud Messaging (GCM)
 2. Write down the **Server Key** from the Settings -> Cloud Messaging section of project
 3. Create an Android Application
 4. Setup Firebase in your Android project and include the required `google-services.json` file
 
You can also perform these steps through an assistant in the Tools -> Firebase menu of Android Studio:

![](http://i.imgur.com/cD7Z8iT.png)

## Register your application in TwinPush

The next step is to setup the TwinPush application. This can be done through the [TwinPush console](https://app.twinpush.com):

1. Access to TwinPush website and login with your account
2. From the control panel of your application, select Application Settings
3. Locate the _Android Push notifications_ section
4. Select _Firebase_ as platform
5. Enter the Server API Key obtained during Firebase Cloud Messaging registration
6. Enter the Android Application package

![API Key input](http://i.imgur.com/0WoTbbv.png)

## Building the application

### Gradle Dependency
Google recommends using [Android Studio](https://developer.android.com/sdk/index.html) with Gradle for Android Projects.

Include this dependency in your `build.gradle` file to reference this library in your project

```groovy
dependencies {
    compile 'com.twinpush.android:sdk:2.3.0'
}
```

### Configuring Android manifest

Inside the _application_ node include the following services:

```xml
<service
    android:name="com.twincoders.twinpush.sdk.services.TwinPushInstanceIdService">
    <intent-filter>
        <action android:name="com.google.firebase.INSTANCE_ID_EVENT" />
    </intent-filter>
</service>

<service
    android:name="com.twincoders.twinpush.sdk.services.NotificationIntentService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### Starting TwinPush SDK

To Setup TwinPush SDK you will need the following information:

* **TwinPush App ID**: Application ID obtained from Settings section of TwinPush platform
* **TwinPush API Key**: TwinPush Application API Key displayed in Settings section
* **Subdomain**: Server subdomain where the application is deployed. Can be obtained in the Settings section of the TwinPush platform.
  
![Notification example](http://i.imgur.com/y2wSepym.jpg)

To initialize the SDK you will ussually override the `onCreate` method of main activity and call `setup` method from the TwinPush SDK, that accepts a `TwinPushOptions` object as  parameter that will hold the required information.

```java
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Setup TwinPush SDK
    TwinPushOptions options = new TwinPushOptions();                // Initialize options
    options.twinPushAppId =     "7687xxxxxxxxxxxx";                 // - APP ID
    options.twinPushApiKey =    "c5caxxxxxxxxxxxxxxxxxxxxxxxx1592"; // - API Key
    options.subdomain =         "mycompany";                        // - Application subdomain
    TwinPushSDK.getInstance(this).setup(options);                   // Call setup
    /* Your code goes here... */
}
```

The `setup` method will return **false** if any of the required parameter is missing.

As seen in the previous example, to access to the shared instance of TwinPush SDK, it is possible to invoque `TwinPushSDK.getInstance` class method that takes the context as parameter.

#### Notification small icon (updated in version 2.3)

It is also required to setup the image resource that will be displayed in the action bar when a Push notification is received:

![Notification small icon](http://i.imgur.com/7QMWksIm.jpg)

To specify the desired icon for notifications, include a drawable reference to `ic_tp_notification` in one of your resource values files (e.g. `res/values/drawables.xml`).

```xml
<drawable name="ic_tp_notification">@drawable/my_notification_icon</drawable>
```

You can follow the [official documentation](https://developer.android.com/studio/write/image-asset-studio.html?hl=es-419#creating-notification) for creating notification icons to ensure that your icon follows the [Android Status Bar Icons Guidelines](https://developer.android.com/guide/practices/ui_guidelines/icon_design_status_bar.html).

## Basic TwinPush integration

### Registering device

Once setup the TwinPush SDK, the device must register to receive notifications. This is made through the `register` method of the `TwinPushSDK` object.

In the following sample code you can see different ways to register the device.

```java
// Obtain TwinPushSDK instance
TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
// Register without alias
twinPush.register();
// Register with alias
twinPush.register("email@company.com");
// Register with alias and listener
twinPush.register("email@company.com", new TwinPushSDK.OnRegistrationListener() {
    @Override
    public void onRegistrationSuccess(String currentAlias) {
        // Registration Successful!
    }
    
    @Override
    public void onRegistrationError(Exception exception) {
        // Error during registration
    }
});
```

### Receiving notifications

When your application receives a Push notification, it will be shown in the status bar. If the user interacts with the notification, it will send an Intent to the main activity of your application with the information received.

This intent contains the following data:

* Action: `NotificationIntentService.ON_NOTIFICATION_RECEIVED_ACTION`
* Extras:
  * `NotificationIntentService.EXTRA_NOTIFICATION`: Serialized object of class PushNotification that contains the information of the received notification.

To obtain the information from this Intent, depending on the execution mode and the current status of the activity, you should take care of the following methods:

* `onCreate`: When the activity is not running, `onCreate` method will be called. To access to the Intent, use the activity `getIntent()` method.
* `onNewIntent`: This method will be called when the application is already running. The new intent will be set as parameter.

#### Example

As an example, the following code shows an activity with a WebView when it receives a rich notification (containing HTML).

First, we include calls to `checkPushNotification` in the entry points previously mentioned.

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Your initialization code goes here
    // (...)
    // Check push notification
    checkPushNotification(getIntent());
}

@Override
protected void onNewIntent(Intent intent) {
    checkPushNotification(intent);
    super.onNewIntent(intent);
}
```

Later, the `checkPushNotification` method is implemented, which examines the Intent received to see if it is a push notification and display `RichNotificationActivity` activity (or custom) in the case of a rich notification.

```java
// Checks if the intent contains a Push notification and displays rich content when appropriated
void checkPushNotification(Intent intent) {
    if (intent != null && intent.getAction() != null && intent.getAction().equals(NotificationIntentService.ON_NOTIFICATION_OPENED_ACTION)) {
        PushNotification notification = (PushNotification) intent.getSerializableExtra(    NotificationIntentService.EXTRA_NOTIFICATION);
        TwinPushSDK.getInstance(this).onNotificationOpen(notification);
    
        if (notification != null && notification.isRichNotification()) {
            Intent richIntent = new Intent(this, RichNotificationActivity.class);
            richIntent.putExtra(    NotificationIntentService.EXTRA_NOTIFICATION, notification);
            startActivity(richIntent);
        }
    }
}
```
Remember to declare in the Manifest file the activity that you will use to display rich content notifications.

In case of the default Rich Activity:

```xml
<activity
    android:name="com.twincoders.twinpush.sdk.activities.RichNotificationActivity"
    android:theme="@style/AppTheme">
</activity>
```

### Sending activity report

Using TwinPush is possible to determine the periods of user activity with the application: how long a device uses the application, last usage time or the number of times it is opened. This feature also allows to identify inactive devices to prevent taking them into consideration for the device limit per license.

To include this, you just have to add a call to `activityStart` and `activityStop` methods of TwinPush SDK in onStart and onStop methods of application activities.

To avoid duplicating code, it is recommended to export common functionality to an abstract parent activity that will be extended by the rest of application activities.

```java
@Override
protected void onStart() {
    TwinPushSDK.getInstance(this).activityStart(this);
    super.onStart();
};

@Override
protected void onStop() {
    TwinPushSDK.getInstance(this).activityStop(this);
    super.onStop();
}
```

### Displaying User Notifications Inbox

Through the _User Inbox_ that TwinPush offers, it is possible for an user of your application to access to its received notifications from different devices.

This requires performing a method call `getUserInbox` of TwinPushSDK:

```java
TwinPushSDK.getInstance(this).getUserInbox(currentPage, maxPages,new GetInboxRequest.Listener() {
    @Override
    public void onError(Exception exception) {
        // Error occurred on request
    }

    @Override
    public void onSuccess(List<InboxNotification> notifications, int totalPages) {
        // Request successful
    }
});
```
To remove a notification from the User Inbox, only is required to call the `deleteNotification` method from TwinPushSDK:

```java
InboxNotification notification = mAdapter.getNotifications().get(position);
TwinPushSDK.getInstance(this).deleteNotification(notification, new TwinRequest.DefaultListener() {
    @Override
    public void onSuccess() {
        // Request successful
    }

    @Override
    public void onError(Exception exception) {
        // An error occurred
    }
});
```

The Demo Application contains an [Inbox Activity](https://github.com/TwinPush/android-sdk/blob/master/demo/src/main/java/com/twincoders/twinpush/sdk/demo/InboxActivity.java) that implements a fully functional example of the User Inbox using a [ReciclerView](http://developer.android.com/intl/es/reference/android/support/v7/widget/RecyclerView.html) adapter.

### Sending user information

Through TwinPush SDK it is possible to send information of the application users.

To do this, you have to make a call to `setProperty` method of `TwinPushSDK`.

```java
TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
twinPush.setProperty("age", getAge());
twinPush.setProperty("gender", getGender());
```

This method takes to parameters:

* Name to be assigned to this property, which will be used to identify it in the statistics
* Value to be assigned to the device. If sending null, it will delete previously submitted information for this attribute.

The system automatically recognizes the type of data to be sent.

You can also delete all information sent by a device performing a call to `clearProperties`:

```java
TwinPushSDK.getInstance(this).clearProperties();
```

### Sending location

There are two ways to notify the user location to TwinPush:

* **Automatically**: you only have to define the type of monitoring, and the SDK automatically sends the changes of the user's position, even when the application is closed.
* **Explicitly**: the user location is sent through a manual call every time you want to update it.

To access the location using either of the two methods, it is necessary to include the following in the manifest node of the AndroidManifest.xml file of the application:

```xml
<!-- Permission to access to GPS Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

**Note:** For Android 6+ _ACCESS\_FINE\_LOCATION_ is considered a [_dangerous_](http://developer.android.com/intl/es/guide/topics/security/permissions.html) permission and it will also require a [Runtime Permission Request](http://developer.android.com/intl/es/training/permissions/requesting.html). If the permission is not granted by the user, the location will not be updated.

#### Automatic sending of location

TwinPush automatically sends the position using a service that is running in the background and is notified of changes in the user's location.

This service does not perform any consulting to location services, but feeds on the changes reported by other sources (also known as passive provider), so battery consumption is not affected.

For the configuration of the services, you must include the following lines in the _AndroidManifest.xml_ file:

Inside the manifest node, the following pemission:

```xml
<!-- Permission to start service on Boot completed -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Inside application node:

```xml
<!-- Passive Location change receiver -->
<receiver android:name="com.twincoders.twinpush.sdk.services.LocationChangeReceiver"/>
<!-- Restart location tracking service when the device is rebooted -->
<receiver android:name="com.twincoders.twinpush.sdk.services.BootReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
    </intent-filter>
</receiver>
```

Once set up the service, you just have to include a call to the SDK `startMonitoringLocationChanges` method to start tracking:

```java
TwinPushSDK.getInstance(this).startMonitoringLocationChanges();
```

Through this call, you begin passive monitoring the user's location, even when the app is closed or is in the background.

To stop monitoring the location, just do a call to `stopMonitoringLocationChanges` method.

```java
TwinPushSDK.getInstance(this).stopMonitoringLocationChanges();
```

#### Explicitly sending the location

To explicitly update the user's location you can make a call to any of the following methods:

* `setLocation(double latitude, double longitude)`: it sends the user coordinates
* `updateLocation(LocationPrecision precision)`: obtains and sends the current location of the user based on the stated accuracy. This level of accuracy will determine the origin, time to obtain and the location accuracy collected, which will result in battery consumption.

Examples of both use cases:

```java
TwinPushSDK twinPush = TwinPushSDK.getInstance(this);
// Send coordinates
twinPush.setLocation(40.383, -3.717);
// Update location
twinPush.updateLocation(LocationPrecision.HIGH);
```

## Customize behavior

### Custom notification layouts

As described in the [official documentation](http://developer.android.com/design/patterns/notifications.html), Android offers a variety of ways to display notifications to the user.

![](http://developer.android.com/design/media/notifications_pattern_expandable.png)
> _Example of default Android expanded and contracted layouts (source: [Android Developers](http://developer.android.com/))_

By default, TwinPush will display the notification message in both contracted and expanded layouts, and will show the application icon for the notifications. By overriding the default TwinPush behavior, you can stack notifications, change the icon displayed on each and broadly, improve and customize the way in which messages are displayed to the user.

It is possible to modify the way in which notifications are shown through TwinPush by following the steps below:

* Create a class that extends [NotificationIntentService](https://github.com/TwinPush/android-sdk/blob/master/sdk/src/com/twincoders/twinpush/sdk/services/NotificationIntentService.java) and override the `displayNotification` method to display the notification in the desired way:

```java
public class MyIntentService extends NotificationIntentService {
    @Override
    protected void displayNotification(Context context, PushNotification notification) {
        // Use your custom layout to display notification
    }
}
```

* Replace the `NotificationIntentService` declaration with your own implementation in the Manifest file:

```xml
<service
    android:name="my_app_package.services.MyIntentService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```