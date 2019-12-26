package in.balakrishnan.cameramodule.camera;

import android.util.Log;

import in.balakrishnan.cameramodule.R;


/**
 * This CameraBundleBuilder is used to create {@link CameraBundle} for {@link CameraControllerActivity}
 */
public class CameraBundleBuilder {
    private static final String TAG = "CameraBundleBuilder";
    /**
     * Preview visibility control set true to make it visible
     */
    private boolean previewIconVisiblity = true;
    /**
     * Set flag as true to enable preview page redirection
     */
    private boolean previewPageRedirection = true;
    /**
     * Set this flag as true to enable no of photos taken above Preview icon, This is linked with {@see previewIconVisiblity}
     */
    private boolean previewEnableCount = true;
    /**
     * Set this flag true to enable / visible done button,
     * which is usually used for finishing the capture of image
     */
    private boolean enableDone = true;
    /**
     * This String used to alter the value of Done / Finish button, Default value is "Done"
     */
    private String doneButtonString = "Done";
    /**
     * This 'int' value is used to set value background for Capture button
     */
    private int captureButtonDrawable = R.drawable.circle;
    /**
     * This 'int' value is used to set value background for Done button
     */
    private int doneButtonDrawable = R.drawable.circle;
    /**
     * This value specifies minimum number of photos required, Default value is 0
     */
    private int min_photo = 0;
    /**
     * This value specifies maximum number of photos allowed,  Default value is 100
     */
    private int max_photo = 100;
    /**
     * Set this flag as true, if you need to take single image
     * If you set this as true, After taking single picture camera module will be closed automatically.
     */
    private boolean singlePhotoMode = false;
    /**
     * Set this flag as true to make preview as full screen preview
     */
    private boolean fullscreenMode = false;
    /**
     * Set this flag as true to enable manual focusing in capture module
     */
    private boolean manualFocus = true;
    /**
     * Set this flag as true to enable 'Preview' and 'Done' views animation using orientation
     */
    private boolean enableRotationAnimation = true;

    private String bucketName;

    private String className;

    private boolean clearBucket = false;

    private boolean preLoaded = false;

    private boolean setResultOnBackPressed = false;


    public CameraBundleBuilder setPreLoaded(boolean preLoaded) {
        this.preLoaded = preLoaded;
        return this;
    }

    public CameraBundleBuilder setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public CameraBundleBuilder setClearBucket(boolean clearBucket) {
        this.clearBucket = clearBucket;
        return this;
    }

    public CameraBundleBuilder setPreviewIconVisiblity(boolean previewIconVisiblity) {
        this.previewIconVisiblity = previewIconVisiblity;
        return this;
    }

    public CameraBundleBuilder setPreviewPageRedirection(boolean previewPageRedirection) {
        this.previewPageRedirection = previewPageRedirection;
        return this;
    }

    public CameraBundleBuilder setPreviewEnableCount(boolean previewEnableCount) {
        this.previewEnableCount = previewEnableCount;
        return this;
    }

    public CameraBundleBuilder setEnableDone(boolean enableDone) {
        this.enableDone = enableDone;
        return this;
    }

    public CameraBundleBuilder setDoneButtonString(String doneButtonString) {
        this.doneButtonString = doneButtonString;
        return this;
    }

    public CameraBundleBuilder setCaptureButtonDrawable(int captureButtonDrawable) {
        this.captureButtonDrawable = captureButtonDrawable;
        return this;
    }

    public CameraBundleBuilder setDoneButtonDrawable(int doneButtonDrawable) {
        this.doneButtonDrawable = doneButtonDrawable;
        return this;
    }

    public CameraBundleBuilder setMin_photo(int min_photo) {
        this.min_photo = min_photo;
        return this;
    }

    public CameraBundleBuilder setMax_photo(int max_photo) {
        this.max_photo = max_photo;
        return this;
    }

    public CameraBundleBuilder setSinglePhotoMode(boolean singlePhotoMode) {
        this.singlePhotoMode = singlePhotoMode;
        return this;
    }

    public CameraBundleBuilder setFullscreenMode(boolean fullscreenMode) {
        this.fullscreenMode = fullscreenMode;
        return this;
    }

    public CameraBundleBuilder setManualFocus(boolean manualFocus) {
        this.manualFocus = manualFocus;
        return this;
    }

    public CameraBundleBuilder setEnableRotationAnimation(boolean enableRotationAnimation) {
        this.enableRotationAnimation = enableRotationAnimation;
        return this;
    }

    public CameraBundleBuilder setClassName(String className) {
        this.className = className;
        return this;
    }

    public CameraBundleBuilder setResultOnBackPress(boolean launchNextActivity) {
        this.setResultOnBackPressed = launchNextActivity;
        return this;
    }


    public CameraBundle createCameraBundle() {
        Log.d(TAG, "CameraBundle: " + setResultOnBackPressed);
        return new CameraBundle(previewIconVisiblity, previewPageRedirection, previewEnableCount, enableDone, doneButtonString, captureButtonDrawable, doneButtonDrawable, min_photo, max_photo, singlePhotoMode, fullscreenMode, manualFocus, enableRotationAnimation, bucketName, className, clearBucket, preLoaded, setResultOnBackPressed);
    }

}