package com.yellowpineapple.offers101.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;

import java.util.List;

import lombok.Getter;

/**
 * Created by agutierrez on 13/02/15.
 */
public class NotificationFactory {

    @Getter Context context;

    private static NotificationFactory INSTANCE = null;

    public static NotificationFactory getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new NotificationFactory(context);
        }
        return INSTANCE;
    }

    private NotificationFactory(Context context) {
        this.context = context;
    }

    public void showWearableOffers(final List<Offer> offers, final Location location) {
        final int NOTIFICATION_COUNT = 4;
        final ImageLoader imageLoader = ImageLoader.getInstance();
        final Context context = getContext();
        if (offers.size() > 0) {
            for (final Offer offer : offers.subList(0, Math.min(NOTIFICATION_COUNT, offers.size() - 1))) {
                if (offer.hasLocation()) {
                    imageLoader.loadImage(offer.getThumbnail().getUrl(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            // Create builder for the main notification
                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_action_logo)
                                            .setContentTitle(offer.getCompany().getName())
                                            .setContentIntent(getContentIntent(offer)).setGroup("101Offers");

                            NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
                            bigStyle.bigText(String.format("(%s)\n%s: %s",
                                    offer.getHumanizedDistance(context, location),
                                    offer.getShortOffer(),
                                    offer.getShortDescription()));
                            notificationBuilder.setStyle(bigStyle);


                            NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
                            // Hide 101 icon
                            //wearableExtender.setHintHideIcon(true);

                            // Set background color
//                                int color = Color.parseColor(String.format("#%s", offer.getThumbnail().getRgbColor()));
//                                Drawable colorDrawable = new ColorDrawable(color);
//                                Bitmap colorBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
//                                colorBitmap.eraseColor(color);
//                                wearableExtender.setBackground(colorBitmap);

                            // Add second page
                            NotificationCompat.BigPictureStyle pictureStyle = new NotificationCompat.BigPictureStyle()
                                    .bigPicture(loadedImage)
                                    .setBigContentTitle(offer.getShortDescription());
                            Notification pictureNotification =
                                    new NotificationCompat.Builder(context)
                                            .setStyle(pictureStyle)
                                            .build();
                            wearableExtender.addPage(pictureNotification);


                            // Add actions
                            {
                                NotificationCompat.Action action =
                                        new NotificationCompat.Action.Builder(android.R.drawable.ic_dialog_map,
                                                "Ver en mapa", showMapIntent(offer))
                                                .build();
                                wearableExtender.addAction(action);
                            }

                            {
                                NotificationCompat.Action action =
                                        new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_save,
                                                "Guardar oferta", showMapIntent(offer))
                                                .build();
                                wearableExtender.addAction(action);

                                // Extend the notification builder with the wearable features
                                notificationBuilder.extend(wearableExtender);
                            }


                            Notification notification = notificationBuilder.build();

                            // Issue the notification
                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                            notificationManager.notify(offer.getId(), notification);
                        }
                    });
                }
            }
        }
    }

    PendingIntent showMapIntent(Offer offer) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(String.format("geo:0,0?q=%f,%f",
                offer.getStore().getLatitude(),
                offer.getStore().getLongitude())));
        PendingIntent notificationIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return notificationIntent;
    }

    public static String ON_OFFER_OPENED_ACTION = "offer_open";
    public static String EXTRA_OFFER = "offer";
    protected PendingIntent getContentIntent(Offer offer) {
        // Prepare the intent which should be launched on notification action
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(getContext().getPackageName());
        intent.setAction(ON_OFFER_OPENED_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_OFFER, offer);
        // Prepare the pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), offer.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }

    protected Bitmap scaleBitmap(Bitmap bitmap, int size) {
        Bitmap resizedBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = size;
        int newHeight = size;
        float multFactor;
        if (originalHeight > originalWidth) {
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight * multFactor);
        } else if (originalWidth > originalHeight) {
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth * multFactor);
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

}
