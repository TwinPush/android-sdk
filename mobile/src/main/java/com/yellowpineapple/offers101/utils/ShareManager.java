package com.yellowpineapple.offers101.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.activities.ParentActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by agutierrez on 26/02/15.
 */
public class ShareManager {

    public static void shareImage(ParentActivity context, Bitmap image, String fileName, String shareDialogTitle, String text) {
        Uri bmpUri = getBitmapUri(image, fileName);
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Launch sharing dialog for image
            context.startActivity(Intent.createChooser(shareIntent, shareDialogTitle));
        } else {
            context.displayErrorDialog(context.getString(R.string.share_offer_error));
        }
    }

    private static Uri getBitmapUri(Bitmap bitmap, String fileName) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileName);
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
