package ca.finalproject.notetaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by gustavo on 2017-04-29.
 */

public class ImageLoader {
    //http://stackoverflow.com/questions/19648957/take-photo-w-camera-intent-and-display-in-imageview-or-textview
    public static Bitmap getThumbnail(Uri uri, Context context) throws IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        //The image of the thumbnail will be 1/4th the height/width of the original
        bitmapOptions.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);

        input.close();

        return getThumbnailBitmap(bitmap);
    }

    /**
     * Rotates the image
     * @param bitmap
     * @return
     * @throws IOException
     */
    private static Bitmap getThumbnailBitmap(Bitmap bitmap) throws IOException {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(bitmap, 0, 0, 500, 500, matrix, true);
    }
}