package in.balakrishnan.easycam;

import android.graphics.Bitmap;

/**
 * Created by BalaKrishnan on 2019-07-03.
 */
public class CaptureData {
    // Thumb bitmap
    Bitmap thumbBitmap;
    // Name of the File for Original image
    String originalFileName;

    public CaptureData(Bitmap thumbBitmap, String originalFileName) {
        this.thumbBitmap = thumbBitmap;
        this.originalFileName = originalFileName;
    }

    public Bitmap getThumbBitmap() {
        return thumbBitmap;
    }

    public void setThumbBitmap(Bitmap thumbBitmap) {
        this.thumbBitmap = thumbBitmap;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
