package in.balakrishnan.easycam.capture;


import in.balakrishnan.easycam.R;
import in.balakrishnan.easycam.CameraBundle;
import in.balakrishnan.easycam.CameraControllerActivity;

/**
 * This class is used to configure {@link CaptureFragment} values are obtained from {@link CameraBundle}
 * from {@link CameraControllerActivity}
 */
public class CaptureFragmentBuilder {
    /**
     * Preview visibility control set true to make it visible
     */
    private boolean previewIconVisiblity = true;
    /**
     * Set flag as true to enable preview page redirection
     */
    private boolean previewPageRedirection = true;
    /**
     * Set this flag as true to enable no of photos taken above Preview icon,
     * This is linked with {@see previewIconVisiblity}
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

    private boolean launchNextActivity = false;

    private String bucketName = "default";

    private CaptureFragmentBuilder setPreviewIconVisiblity(boolean previewIconVisiblity) {
        this.previewIconVisiblity = previewIconVisiblity;
        return this;
    }

    private CaptureFragmentBuilder setPreviewPageRedirection(boolean previewPageRedirection) {
        this.previewPageRedirection = previewPageRedirection;
        return this;
    }

    private CaptureFragmentBuilder setPreviewEnableCount(boolean previewEnableCount) {
        this.previewEnableCount = previewEnableCount;
        return this;
    }

    private CaptureFragmentBuilder setEnableDone(boolean enableDone) {
        this.enableDone = enableDone;
        return this;
    }

    private CaptureFragmentBuilder setDoneButtonString(String doneButtonString) {
        this.doneButtonString = doneButtonString;
        return this;
    }

    private CaptureFragmentBuilder setCaptureButtonDrawable(int captureButtonDrawable) {
        this.captureButtonDrawable = captureButtonDrawable;
        return this;
    }

    private CaptureFragmentBuilder setDoneButtonDrawable(int doneButtonDrawable) {
        this.doneButtonDrawable = doneButtonDrawable;
        return this;
    }

    private CaptureFragmentBuilder setMIN_PHOTO(int min_photo) {
        this.min_photo = min_photo;
        return this;
    }

    private CaptureFragmentBuilder setMAX_PHOTO(int max_photo) {
        this.max_photo = max_photo;
        return this;
    }

    private CaptureFragmentBuilder setSinglePhotoMode(boolean singlePhotoMode) {
        this.singlePhotoMode = singlePhotoMode;
        return this;
    }

    private CaptureFragmentBuilder setFullscreenMode(boolean fullscreenMode) {
        this.fullscreenMode = fullscreenMode;
        return this;
    }

    private CaptureFragmentBuilder setManualFocus(boolean manualFocus) {
        this.manualFocus = manualFocus;
        return this;
    }

    private CaptureFragmentBuilder setEnableRotationAnimation(boolean enableRotationAnimation) {
        this.enableRotationAnimation = enableRotationAnimation;
        return this;
    }

    public CaptureFragmentBuilder setLaunchNextActivity(boolean launchNextActivity) {
        this.launchNextActivity = launchNextActivity;
        return this;
    }

    private void setBucketName(String bucket) {
        this.bucketName = bucket;

    }

    /**
     * This method is used to create Capture Fragment based on the inputs from {@see in.balakrishnan.components.camera.CameraBundle}
     *
     * @param bundle Input from Triggered activity, Parcelable object, Default values have been specified in the {@see in.balakrishnan.components.camera.CameraBundle}
     * @return CaptureFragmentBuilder
     */
    public CaptureFragmentBuilder setBundle(CameraBundle bundle) {
        setEnableRotationAnimation(bundle.isEnableRotationAnimation());
        setPreviewPageRedirection(bundle.isPreviewPageRedirection());
        setPreviewIconVisiblity(bundle.isPreviewIconVisibility());
        setPreviewEnableCount(bundle.isPreviewEnableCount());
        setManualFocus(bundle.isManualFocus());
        setEnableDone(bundle.isEnableDone());
        setMAX_PHOTO(bundle.getMax_photo());
        setMIN_PHOTO(bundle.getMin_photo());
        setCaptureButtonDrawable(bundle.getCaptureButtonDrawable());
        setDoneButtonDrawable(bundle.getDoneButtonDrawable());
        setDoneButtonString(bundle.getDoneButtonString());
        setSinglePhotoMode(bundle.isSinglePhotoMode());
        setFullscreenMode(bundle.isFullscreenMode());
        setBucketName(bundle.getBucket());
        setLaunchNextActivity(bundle.isSetResultOnBackPressed());
        return this;
    }


    public CaptureFragment createCaptureFragment() throws Exception {
        if (min_photo >= max_photo)
            throw new Exception("Check min_photo and max_photo values");

        return CaptureFragment.newInstance(previewIconVisiblity, previewPageRedirection, previewEnableCount, enableDone, doneButtonString, captureButtonDrawable, doneButtonDrawable, min_photo, max_photo, singlePhotoMode, fullscreenMode, manualFocus, enableRotationAnimation, bucketName, launchNextActivity);
    }
}