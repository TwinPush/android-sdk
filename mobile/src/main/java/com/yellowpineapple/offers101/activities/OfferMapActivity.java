package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.models.Store;
import com.yellowpineapple.offers101.views.OfferMapInfoView;
import com.yellowpineapple.offers101.views.OfferMapInfoView_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by agutierrez on 11/02/15.
 */
@EActivity(R.layout.activity_offers_map)
public class OfferMapActivity extends ParentActivity implements OnMapReadyCallback {

    @Extra List<Offer> offers;
    @Extra Location location;

    Offer offer = null;

    @FragmentById MapFragment mapFragment;

    Map<Marker, Offer> markersHash;

    @AfterViews
    void afterViews() {
        if (offers != null && offers.size() == 1) {
            offer = offers.get(0);
            setTitle(offer.getCompany().getName());
        }
        mapFragment.getMapAsync(this);
    }

    /* OnMapReadyCallback */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        if (offers != null) {
            markersHash = new HashMap<>();
            for (Offer offer : offers) {
                if (offer.hasLocation()) {
                    Store store = offer.getStore();
                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            OfferMapInfoView view = OfferMapInfoView_.build(OfferMapActivity.this);
                            view.setOffer(markersHash.get(marker), location);
                            return view;
                        }
                    });
                    googleMap.setMyLocationEnabled(true);
                    final Marker storeMarker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(new LatLng(store.getLatitude(), store.getLongitude()))
                                    .title(offer.getCompany().getName())
                                    .snippet(store.getAddress()));
                    markersHash.put(storeMarker, offer);
                }
            }
            centerMap(googleMap, new ArrayList<>(markersHash.keySet()));
        }
    }

    private void centerMap(final GoogleMap googleMap, final List<Marker> markers) {
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                final GoogleMap.CancelableCallback callback = new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (markers.size() == 1) {
                            markers.get(0).showInfoWindow();
                        }
                    }

                    @Override
                    public void onCancel() {}
                };

                if (location != null) {
                    // Zoom and center to display both store marker and my location
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
                    LatLngBounds bounds = builder.build();
                    // offset from edges of the map in pixels
                    int padding = Math.round(getResources().getDimension(markers.size() > 1 ? R.dimen.map_padding_multiple : R.dimen.map_padding));
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (markers.size() == 1) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(markers.get(0).getPosition()), callback);
                            }
                        }

                        @Override
                        public void onCancel() {}
                    });
                } else {
                    if (markers.size() == 1) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(markers.get(0).getPosition()));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(14), callback);
                    }
                }
            }
        });
    }

}
