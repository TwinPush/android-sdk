Wakup SDK Library
==================

Native Android SDK for [Wakup platform](http://wakup.net).

## Installation

To start using Wakup you have to integrate the Wakup SDK in your Android Application.

### Gradle dependency

Include this dependency in the `build.gradle` file of your application project module.

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

To start the main Wakup activity, that will be the entry point for the entire offers section, you will need to provide your Wakup API Key, wrapped in a `WakupOptions` object:

```java
// Launch Wakup with custom region and default location
Wakup.instance(this).launch(
        new WakupOptions("WAKUP_API_KEY").          // Auth API Key
                country("ES").                      // Region for address search
                defaultLocation(41.38506, 2.17340)  // For disabled location devices
);

```

Trough the `WakupOptions` object it is possible to setup the following parameters:

- **Api Key** (mandatory): Authentication token for your application
- **Country**: [ISO country code](https://en.wikipedia.org/wiki/ISO_3166-1) used for region filter on places search
- **Default location**: coordinates to find offers near to when the user does not allow application access to device location

## Customization

Wakup uses its own theme with preset styles, icons and colors that can be easily customized.
The default Wakup appearance looks like the following:

[![](http://i.imgur.com/ooIBrcWm.png)](http://i.imgur.com/ooIBrcW.png) [![](http://i.imgur.com/TJsEQNAm.png)](http://i.imgur.com/TJsEQNA.png) [![](http://i.imgur.com/sHaR41nm.png)](http://i.imgur.com/sHaR41n.png)

The application appearance can be customized by overriding the resources used by the styles and layouts of the Wakup SDK. This resources contains the prefix `'wk_'` to avoid clashing with client application resources.

The elements that are not customized will be displayed will the default look & feel.

### Colors

Main application Look & Feel will be customized by overriding the default colors used by the Wakup SDK layout.

To do so, create a file named `wakup.xml` in your project module and copy the primary colors:

```xml
<!-- Primary customization colors -->
<color name="wk_primary">#3C1E3D</color>
<color name="wk_primary_pressed">#7B4C7D</color>
<color name="wk_primary_reverse">#A47BA6</color>
<color name="wk_secondary">#809718</color>
<color name="wk_secondary_pressed">#617213</color>
<color name="wk_secondary_reverse">@color/wk_white</color>
```

This main colors are used as a reference of the entire Offers section and will determine its appearance.

By changing this resources, you will override, **at compile time** the color used by the Wakup section.

#### Deep customization

If a more thorough customization is required, you can also override the secondary colors (that are mostly based in previously defined primary colors) that will allow to set colors to every section more precisely:

```xml
<!-- Activity -->
<color name="wk_background">#D5D5D5</color>

<!-- Text color -->
<color name="wk_main_text">#505050</color>
<color name="wk_bold_text">#393939</color>
<color name="wk_light_text">#8F8F8F</color>

<!-- Action & Navigation Bar -->
<color name="wk_actionbar_bg">@color/wk_primary</color>
<color name="wk_actionbar_text">@color/wk_primary_reverse</color>
<color name="wk_actionbar_subtitle">#AFFF</color>
<color name="wk_navbar_bg">@color/wk_primary</color>
<color name="wk_navbar_pressed">@color/wk_primary_pressed</color>
<color name="wk_navbar_text">@color/wk_primary_reverse</color>
<color name="wk_navbar_divider">@color/wk_primary_reverse</color>

<!-- Offer Item -->
<color name="wk_offer_list_item_bg">@color/wk_white</color>
<color name="wk_offer_short_desc">@color/wk_secondary_reverse</color>
<color name="wk_offer_short_desc_bg">@color/wk_secondary</color>

<!-- Offer Detail -->
<color name="wk_store_offers">@color/wk_secondary_reverse</color>
<color name="wk_store_offers_bg">@color/wk_secondary</color>
<color name="wk_store_offers_bg_pressed">@color/wk_secondary_pressed</color>

<!-- Action & Filter buttons -->
<color name="wk_action_active">@color/wk_primary</color>
<color name="wk_action_pressed">@color/wk_white</color>
<color name="wk_action_inactive">@color/wk_light_text</color>

<!-- No results view -->
<color name="wk_no_results_text">#8F8F8F</color>

<!-- Search view -->
<color name="wk_search_header_bg">#F6F6F6</color>
<color name="wk_search_list_bg">@color/wk_white</color>
<color name="wk_search_icon">@color/wk_light_text</color>

```

# Dependencies

The following dependencies are used in the project:

* [AndroidStaggeredGrid](https://github.com/etsy/AndroidStaggeredGrid): Multiple height grid view
* [Async Http](http://loopj.com/android-async-http/): Library for asynchronous requests
* [Gson](http://code.google.com/p/google-gson/): Parse and serialize JSON
* [Calligraphy](https://github.com/chrisjenx/Calligraphy): Allows setting custom typeface to Text Views
* [Universal-Image-Loader](https://github.com/nostra13/Android-Universal-Image-Loader): Library to load and cache images by URL
* [Autofit TextView](https://github.com/grantland/android-autofittextview): Auto shrink large texts to adapt to available space