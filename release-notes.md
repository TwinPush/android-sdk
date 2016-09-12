# TwinPush Android SDK Release Notes

## 2.2.0
Version updates to support Android SDK v24:

* Updated Android build tools and target SDK to latest available version: 24
* Updated Android Support V4 dependency to latest version: 24.2.0
* Updated Gradle version to 2.1.3

## 2.1.0
* Included Notification Inbox for User. Every user will be able to maintain its own inbox in multiple devices.
* Updated Android Support V4 dependency to latest version: 23.3.0

## 2.0.4
* Included sanity check to avoid errors updating location when ACESS\_FINE\_LOCATION permission is not granted (for Android 6+)

## 2.0.3
* Improved background location updates: Now LocationService is not necessary
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to latest available version: 8.4.0

## 2.0.2
* Removed unnecessary AppCompat dependency
* Updated minimum SDK Version to 9 (Android 2.3) due to Google Play Services requirement
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to last available version: 8.3.0

## 2.0.1

* Updated GCM Library now using [GoogleCloudMessaging](https://developers.google.com/android/reference/com/google/android/gms/gcm/GoogleCloudMessaging) instead of GCMBroadcastReceiver
* Migrated project from Eclipse to Android Studio
* Library is now available in Maven and as a Gradle dependency
* Subdomain is now a mandatory parameter to setup library
* Updated library setup method that now will take a `TwinPushOptions` transport entity as parameter
* `TwinPushIntentService` has been renamed to `NotificationIntentService` and moved to `services` package.
* Improved registration request to only be launched when any of the registration attributes has changed since last request
* Registration requests now will automatically gather and send to server the following information:
  * App version
  * SDK version
  * OS Version
  * Device info (manufacturer and model)
  * Language