package in.balakrishnan.cameramodule.camera;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by BalaKrishnan on 2019-07-15.
 * <p>
 * This Object is used to create {@link CameraControllerActivity}
 * This model is used to configure both {@link com.toyaround.android.camera.capture.CaptureFragment}
 * and {@link com.toyaround.android.camera.preview.PreviewFragment}
 */
public class CameraBundle implements Parcelable {
    public static final Creator<CameraBundle> CREATOR = new Creator<CameraBundle>() {
        @Override
        public CameraBundle createFromParcel(Parcel in) {
            return new CameraBundle(in);
        }

        @Override
        public CameraBundle[] newArray(int size) {
            return new CameraBundle[size];
        }
    };
    private static final String TAG = "CameraBundle";
    String className;
    /**
     * Preview visibility control set true to make it visible
     */
    private boolean previewIconVisibility;
    /**
     * Set flag as true to enable preview page redirection
     */
    private boolean previewPageRedirection;
    /**
     * Set this flag as true to enable no of photos taken above Preview icon, This is linked with {@see previewIconVisibility}
     */
    private boolean previewEnableCount;
    /**
     * Set this flag true to enable / visible done button,
     * which is usually used for finishing the capture of image
     */
    private boolean enableDone;
    /**
     * This String used to alter the value of Done / Finish button
     */
    private String doneButtonString;
    /**
     * This 'int' value is used to set value background for Capture button
     */
    private int captureButtonDrawable;
    /**
     * This 'int' value is used to set value background for Done button
     */
    private int doneButtonDrawable;
    /**
     * This value specifies minimum number of photos required
     */
    private int min_photo;
    /**
     * This value specifies maximum number of photos allowed
     */
    private int max_photo;
    /**
     * Set this flag as true, if you need to take single image
     * If you set this as true, After taking single picture camera module will be closed automatically.
     */
    private boolean singlePhotoMode;
    /**
     * Set this flag as true to make preview as full screen preview
     */
    private boolean fullscreenMode;
    /**
     * Set this flag as true to enable manual focusing in capture module
     */
    private boolean manualFocus;
    /**
     * Set this flag as true to enable 'Preview' and 'Done' views animation using orientation
     */
    private boolean enableRotationAnimation;
    private String bucket;
    private boolean clearBucket;
    private boolean preLoaded;
    private boolean setResultOnBackPressed;

    public CameraBundle(
            boolean previewIconVisibility,
            boolean previewPageRedirection,
            boolean previewEnableCount,
            boolean enableDone,
            String doneButtonString,
            int captureButtonDrawable,
            int doneButtonDrawable,
            int min_photo,
            int max_photo,
            boolean singlePhotoMode,
            boolean fullscreenMode,
            boolean manualFocus,
            boolean enableRotationAnimation,
            String bucket,
            String className,
            boolean clearBucket,
            boolean preLoaded,
            boolean setResultOnBackPressed) {
        this.previewIconVisibility = previewIconVisibility;
        this.previewPageRedirection = previewPageRedirection;
        this.previewEnableCount = previewEnableCount;
        this.enableDone = enableDone;
        this.doneButtonString = doneButtonString;
        this.captureButtonDrawable = captureButtonDrawable;
        this.doneButtonDrawable = doneButtonDrawable;
        this.min_photo = min_photo;
        this.max_photo = max_photo;
        this.singlePhotoMode = singlePhotoMode;
        this.fullscreenMode = fullscreenMode;
        this.manualFocus = manualFocus;
        this.enableRotationAnimation = enableRotationAnimation;
        this.bucket = bucket;
        this.className = className;
        this.clearBucket = clearBucket;
        this.preLoaded = preLoaded;
        this.setResultOnBackPressed = setResultOnBackPressed;
        Log.d(TAG, "CameraBundle: " + setResultOnBackPressed);
    }


    protected CameraBundle(Parcel in) {
        previewIconVisibility = in.readByte() != 0;
        previewPageRedirection = in.readByte() != 0;
        previewEnableCount = in.readByte() != 0;
        enableDone = in.readByte() != 0;
        doneButtonString = in.readString();
        captureButtonDrawable = in.readInt();
        doneButtonDrawable = in.readInt();
        min_photo = in.readInt();
        max_photo = in.readInt();
        singlePhotoMode = in.readByte() != 0;
        fullscreenMode = in.readByte() != 0;
        manualFocus = in.readByte() != 0;
        enableRotationAnimation = in.readByte() != 0;
        bucket = in.readString();
        className = in.readString();
        clearBucket = in.readByte() != 0;
        preLoaded = in.readByte() != 0;
        setResultOnBackPressed = in.readByte() != 0;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (previewIconVisibility ? 1 : 0));
        dest.writeByte((byte) (previewPageRedirection ? 1 : 0));
        dest.writeByte((byte) (previewEnableCount ? 1 : 0));
        dest.writeByte((byte) (enableDone ? 1 : 0));
        dest.writeString(doneButtonString);
        dest.writeInt(captureButtonDrawable);
        dest.writeInt(doneButtonDrawable);
        dest.writeInt(min_photo);
        dest.writeInt(max_photo);
        dest.writeByte((byte) (singlePhotoMode ? 1 : 0));
        dest.writeByte((byte) (fullscreenMode ? 1 : 0));
        dest.writeByte((byte) (manualFocus ? 1 : 0));
        dest.writeByte((byte) (enableRotationAnimation ? 1 : 0));
        dest.writeString(bucket);
        dest.writeString(className);
        dest.writeByte((byte) (clearBucket ? 1 : 0));
        dest.writeByte((byte) (preLoaded ? 1 : 0));
        dest.writeByte((byte) (setResultOnBackPressed ? 1 : 0));

    }

    public boolean isPreLoaded() {
        return preLoaded;
    }

    public void setPreLoaded(boolean preLoaded) {
        this.preLoaded = preLoaded;
    }

    public boolean isClearBucket() {
        return clearBucket;
    }

    public void setClearBucket(boolean clearBucket) {
        this.clearBucket = clearBucket;
    }

    public boolean isPreviewIconVisibility() {
        return previewIconVisibility;
    }

    public void setPreviewIconVisibility(boolean previewIconVisibility) {
        this.previewIconVisibility = previewIconVisibility;
    }

    public boolean isSetResultOnBackPressed() {
        return setResultOnBackPressed;
    }

    public boolean isPreviewPageRedirection() {
        return previewPageRedirection;
    }

    public void setPreviewPageRedirection(boolean previewPageRedirection) {
        this.previewPageRedirection = previewPageRedirection;
    }

    public boolean isPreviewEnableCount() {
        return previewEnableCount;
    }

    public void setPreviewEnableCount(boolean previewEnableCount) {
        this.previewEnableCount = previewEnableCount;
    }

    public boolean isEnableDone() {
        return enableDone;
    }

    public void setEnableDone(boolean enableDone) {
        this.enableDone = enableDone;
    }

    public String getDoneButtonString() {
        return doneButtonString;
    }

    public void setDoneButtonString(String doneButtonString) {
        this.doneButtonString = doneButtonString;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public int getCaptureButtonDrawable() {
        return captureButtonDrawable;
    }

    public void setCaptureButtonDrawable(int captureButtonDrawable) {
        this.captureButtonDrawable = captureButtonDrawable;
    }

    public int getDoneButtonDrawable() {
        return doneButtonDrawable;
    }

    public void setDoneButtonDrawable(int doneButtonDrawable) {
        this.doneButtonDrawable = doneButtonDrawable;
    }

    public int getMin_photo() {
        return min_photo;
    }

    public void setMin_photo(int min_photo) {
        this.min_photo = min_photo;
    }

    public int getMax_photo() {
        return max_photo;
    }

    public void setMax_photo(int max_photo) {
        this.max_photo = max_photo;
    }

    public boolean isSinglePhotoMode() {
        return singlePhotoMode;
    }

    public void setSinglePhotoMode(boolean singlePhotoMode) {
        this.singlePhotoMode = singlePhotoMode;
    }

    public boolean isFullscreenMode() {
        return fullscreenMode;
    }

    public void setFullscreenMode(boolean fullscreenMode) {
        this.fullscreenMode = fullscreenMode;
    }

    public boolean isManualFocus() {
        return manualFocus;
    }

    public void setManualFocus(boolean manualFocus) {
        this.manualFocus = manualFocus;
    }

    public boolean isEnableRotationAnimation() {
        return enableRotationAnimation;
    }

    public void setEnableRotationAnimation(boolean enableRotationAnimation) {
        this.enableRotationAnimation = enableRotationAnimation;
    }

    @Override
    public String toString() {
        return "CameraBundle{" +
                "previewIconVisibility=" + previewIconVisibility +
                ", previewPageRedirection=" + previewPageRedirection +
                ", previewEnableCount=" + previewEnableCount +
                ", enableDone=" + enableDone +
                ", doneButtonString='" + doneButtonString + '\'' +
                ", captureButtonDrawable=" + captureButtonDrawable +
                ", doneButtonDrawable=" + doneButtonDrawable +
                ", min_photo=" + min_photo +
                ", max_photo=" + max_photo +
                ", singlePhotoMode=" + singlePhotoMode +
                ", fullscreenMode=" + fullscreenMode +
                ", manualFocus=" + manualFocus +
                ", enableRotationAnimation=" + enableRotationAnimation +
                '}';
    }
}
