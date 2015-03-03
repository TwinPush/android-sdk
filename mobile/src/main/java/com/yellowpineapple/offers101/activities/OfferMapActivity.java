package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.communications.requests.OfferListRequestListener;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.models.Store;
import com.yellowpineapple.offers101.views.OfferMapInfoView;
import com.yellowpineapple.offers101.views.OfferMapInfoView_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by agutierrez on 11/02/15.
 */
@EActivity(R.layout.activity_offers_map)
public class OfferMapActivity
        extends ParentActivity
        implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    @Extra List<Offer> offers;
    @Extra Location location;

    @Extra Offer offer = null;
    boolean singleOffer = false;

    boolean shouldCenterMap = true;

    @FragmentById MapFragment mapFragment;

    Map<Marker, Offer> markersHash;

    List<Integer> displayedStores = new ArrayList<>();
    List<String> preloadedCompanies = new ArrayList<>();

    Location lastRequestLocation = null;
    private static int NEW_REQUEST_DISTANCE_METERS = 500;

    GoogleMap googleMap;
    Timer timer = new Timer();

    @AfterViews
    void afterViews() {
        if (offer != null) {
            setTitle(offer.getCompany().getName());
            offers = new ArrayList<>();
            offers.add(offer);
            singleOffer = true;
        }
        preloadCompanyLogos(offers);
        mapFragment.getMapAsync(this);
    }

    void preloadCompanyLogos(List<Offer> offers) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true).build();
        for (Offer offer : offers) {
            String logoURL = offer.getCompany().getLogo().getUrl();
            if (!preloadedCompanies.contains(logoURL)) {
                imageLoader.loadImage(offer.getCompany().getLogo().getUrl(), displayImageOptions, new SimpleImageLoadingListener());
                preloadedCompanies.add(logoURL);
            }
        }
    }

    /* OnMapReadyCallback */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.markersHash = new HashMap<>();
        googleMap.setInfoWindowAdapter(this);
        // Show more offers after user navigation when not in single mode
        if (!singleOffer) {
            googleMap.setOnCameraChangeListener(this);
            googleMap.setOnInfoWindowClickListener(this);
        }
        displayInMap(offers);
    }

    private void displayInMap(List<Offer> offers) {
        if (offers != null) {
            for (Offer offer : offers) {
                if (offer.hasLocation() && !displayedStores.contains(offer.getStore().getId())) {
                    Store store = offer.getStore();
                    googleMap.setMyLocationEnabled(true);
                    final Marker storeMarker = googleMap.addMarker(
                            new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(offer.getCategory().getIconResId()))
                                    .position(new LatLng(store.getLatitude(), store.getLongitude()))
                                    .title(offer.getCompany().getName())
                                    .snippet(store.getAddress()));
                    markersHash.put(storeMarker, offer);
                    displayedStores.add(offer.getStore().getId());
                }
            }
            if (shouldCenterMap) {
                centerMap(googleMap, new ArrayList<>(markersHash.keySet()));
            }
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
                    int padding = Math.round(getResources().getDimension(singleOffer ? R.dimen.map_padding : R.dimen.map_padding_multiple));
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    googleMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            if (singleOffer) {
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
                shouldCenterMap = false;
            }
        });
    }

    // Map Event Listeners

    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {
        // Check last request location to see if update is necessary
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LatLng mapCenter = cameraPosition.target;
                //Convert LatLng to Location
                Location newLocation = new Location("MapCenter");
                newLocation.setLatitude(mapCenter.latitude);
                newLocation.setLongitude(mapCenter.longitude);
                if (isUpdateRequired(newLocation)) {
                    loadOffersByLocation(newLocation);
                }
            }
        }, 1000);
    }

    private boolean isUpdateRequired(Location newLocation) {
        return lastRequestLocation == null || lastRequestLocation.distanceTo(newLocation) > NEW_REQUEST_DISTANCE_METERS;
    }

    @UiThread
    void loadOffersByLocation(Location location) {
        this.lastRequestLocation = location;
        setLoading(true);
        getRequestClient().findLocatedOffers(location, null, new OfferListRequestListener() {
            @Override
            public void onSuccess(List<Offer> offers) {
                preloadCompanyLogos(offers);
                displayInMap(offers);
                setLoading(false);
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                displayErrorDialog(exception);
            }
        });
    }

    @Override
     public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Offer offer = markersHash.get(marker);
        OfferMapInfoView view = OfferMapInfoView_.build(OfferMapActivity.this);
        view.setClickable(!singleOffer);
        view.setOffer(offer, location);
        return view;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Offer offer = markersHash.get(marker);
        showOfferDetailActivity(offer, location);
    }
}
