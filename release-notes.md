# TwinPush Android SDK Release Notes

## 2.3.0 - 2017-05-22
* Preferences stored by application are now encrypted to avoid its access from rooted devices
* Notification icon is now setup through resources reference to ensure it is refreshed after application update without the need to open application 

## 2.2.4 - 2017-03-31
* Updated Android Support V4 dependency to latest version: 25.3.1
* Updated Gradle build tools version to 3.3
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to latest available version: 10.2.1
* Increased minSdkVersion from 9 to 14 as a requirement for Google Play Services v10
* Included convenience methods to obtain Push token once registered and report open notification by ID

## 2.2.2 - 2016-11-14
Version updates to support Android SDK v25:

* Updated Android build tools and target SDK to latest available version: 25
* Updated Android Support V4 dependency to latest version: 25.0.0
* Updated Gradle build tools version to 2.2.2
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to latest available version: 9.8.0


## 2.2.1 - 2016-09-15
Version updates to support Android SDK v24:

* Updated Android build tools and target SDK to latest available version: 24
* Updated Android Support V4 dependency to latest version: 24.2.0
* Updated Gradle version to 2.1.3

## 2.1.0 - 2016-08-12
* Included Notification Inbox for User. Every user will be able to maintain its own inbox in multiple devices.
* Updated Android Support V4 dependency to latest version: 23.3.0

## 2.0.4 - 2016-02-29
* Included sanity check to avoid errors updating location when ACESS\_FINE\_LOCATION permission is not granted (for Android 6+)

## 2.0.3 - 2016-01-27
* Improved background location updates: Now LocationService is not necessary
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to latest available version: 8.4.0

## 2.0.2 - 2015-12-16
* Removed unnecessary AppCompat dependency
* Updated minimum SDK Version to 9 (Android 2.3) due to Google Play Services requirement
* Updated [Google Play Services](https://developers.google.com/android/guides/setup) dependency to last available version: 8.3.0

## 2.0.1 - 2015-12-03

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