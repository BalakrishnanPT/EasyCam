package in.balakrishnan.easycam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class BitmapHelper {
    public static Uri getFileUriForBitmap(@NonNull Bitmap resource) {
        File f;
        FileOutputStream fos;
        try {
            //create a file to write bitmap data
            f = File.createTempFile("ImageChange", "new");
            f.createNewFile();

//Convert bitmap to byte array
            Bitmap bitmap = resource;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return Uri.fromFile(f);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This function is used to update the Preview Image
     *
     * @param path path of the image
     */
    public static void setFileToPreview(String path, ImageView imageView) {
        File originalImage;
        originalImage = new File(path);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalImage.getPath(), options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, options.outWidth / 4, options.outHeight / 4);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(originalImage.getPath(), options);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * This functions is used to calculate inSampleSize based on reqWidth and reqHeight
     *
     * @param options   BitmapFactory.Options
     * @param reqWidth  Required Width
     * @param reqHeight Required Height
     * @return Integer value
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}