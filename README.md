# README #

This repository contains the 101 Offers Android application for Smartphone and Wearable devices

### Dependencies ###

The following dependencies are used in the project:

* [AndroidStaggeredGrid](https://github.com/etsy/AndroidStaggeredGrid): Multiple height grid view
* [Async Http](http://loopj.com/android-async-http/): Library for asynchronous requests
* [Gson](http://code.google.com/p/google-gson/): Parse and serialize JSON
* [Calligraphy](https://github.com/chrisjenx/Calligraphy): Allows setting custom typeface to Text Views
* [Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader): Library to load and cache images by URL
* [Autofit TextView](https://github.com/grantland/android-autofittextview): Auto shrink large texts to adapt to available space

### Google Maps Setup

https://developers.google.com/maps/documentation/android-api/start

## Installation

### Gradle dependency

Include this dependency in your `build.gradle` file to reference this library in your project

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/wakup/sdk"
    }
}

dependencies {
    compile 'com.wakup.android:sdk:1.0.0'
}
```

### Google Maps

Wakup uses the Google Maps library to display geo-located offers in a Map View.
To setup the Google Maps API in your application you can following the [official documentation](https://developers.google.com/maps/documentation/android-api/start).

**Note:** It is important to remark that you will need to give access to the API Key for your debug and release certificates.

### Start Wakup Activity

To start the main wakup activity, that will be the entry point for the entire offers section, you will need to provide your Wakup API Key, included in a `Wakup.Options` object:

```java
// Launch Wakup
Wakup.Options wakupOptions = new Wakup.Options();
wakupOptions.apiKey = "07123456-1234-4e4e-a2a2-3ddc562a2468";
wakupOptions.actionBarIcon = R.drawable.ic_action_logo;
Wakup.instance(this).launch(wakupOptions);
```