package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.models.Store;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by agutierrez on 11/02/15.
 */
@EActivity(R.layout.activity_offers_map)
public class OfferMapActivity extends ParentActivity implements OnMapReadyCallback {

    @Extra Offer offer;
    @Extra Location location;

    @FragmentById MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
    }

    @OptionsItem(android.R.id.home)
    void onHomePressed() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        slideOutTransition();
    }

    @AfterViews
    void afterViews() {
        if (offer != null) {
            setTitle(offer.getCompany().getName());
            mapFragment.getMapAsync(this);
        }
    }

    /* OnMapReadyCallback */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (offer != null && offer.getStore() != null && offer.getStore().getLocation() != null) {
            Store store = offer.getStore();
            googleMap.setMyLocationEnabled(true);
            final MarkerOptions storeMarker = new MarkerOptions()
                    .position(new LatLng(store.getLatitude(), store.getLongitude()))
                    .title(store.getAddress());
            googleMap.addMarker(storeMarker);
            centerMap(googleMap, storeMarker.getPosition());
        }
    }

    private void centerMap(final GoogleMap googleMap, final LatLng marker) {
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (location != null) {
                    // Zoom and center to display both store marker and my location
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(marker);
                    builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                    LatLngBounds bounds = builder.build();
                    // offset from edges of the map in pixels
                    int padding = Math.round(getResources().getDimension(R.dimen.map_padding));
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.animateCamera(cu);
                } else {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                }
            }
        });
    }

}
