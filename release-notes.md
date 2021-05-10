# TwinPush Android SDK Release Notes

## 3.4.1 - 2021-05-10

* Library migrated to JitPack repository

## 3.3.1 - 2020-12-02

- Included Huawei Messaging Services integration

## 3.2.2 - 2020-07-02

* Included callback listener for generic setProperty and for clearProperties SDK methods

## 3.2.1 - 2020-03-19

* Notification push acknowledgement will only be sent when notifications are enabled on the device
* TwinPush API response errors will be wrapped in a TwinPushException object with additional info

## 3.2.0 - 2020-02-04

* Implemented optional push acknowledgement on notification received 

## 3.1.0 - 2020-02-04

* Included tag filtering for getUserInbox method

## 3.0.1 - 2019-12-23

* Fixed race condition that could lead to runtime exception when registration returns error

## 3.0.0 - 2019-10-15

* Updated compile SDK version to 29
* Increased `minSdkVersion` to 16 as required for Firebase library
* Updated firebase library version to 20 
* Migrated to Android X

## 2.8.0 - 2019-09-12

* Now it is possible to send Enum List custom properties for device using `setProperty` method

## 2.7.7 - 2019-04-29

* Device info will be cleared when `app_id` is changed on `setup` method call
* Registration listener will also be called after external registration when present

## 2.7.6 - 2018-11-29

* Included SDK method `setEnumProperty` to create custom properties with enum values
* Included SDK method `unregister` that clears the device registration info stored locally
* Updated Android Support V4 dependency to latest version: 27.1.1
* Updated gradle build tools to 3.2.1 and buildToolsVersion to 28.0.3
* Updated target SDK to v27
* Updated Android Support V4 dependency to latest version: 27.1.1
* Improved communications error handling to include detailed message on returned exceptions

## 2.7.1 - 2018-10-05

* Updated communications module library using volley


## 2.7.0 - 2018-08-22

* Updated Firebase library version to 17.3.0
* TwinPushInstanceIdService is no longer needed to be declared con manifest


## 2.6.0 - 2018-07-24

* Implemented default notifications channel to ensure Android 8 compatibility

## 2.5.1 - 2018-06-05

* Included SDK method to obtain current application badge count

## 2.5.0 - 2018-05-30

* Implemented external registration through local broadcast intent

## 2.4.3 - 2018-05-18
* Fixed issue that could cause consecutive registration requests to not be correctly launched after a first error

## 2.4.2 - 2017-10-24
* Migrated push service from GCM to **Firebase**
* Included method call for inbox summary in SDK interface
* `register` method will now execute as a background task

Version updates to support Android SDK v26:

* Updated Android build tools and target SDK to latest available version: 26
* Updated Android Support V4 dependency to latest version: 26.1.0

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