package in.balakrishnan.easycam.capture;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import in.balakrishnan.easycam.R;
import in.balakrishnan.easycam.CameraControllerActivity;
import in.balakrishnan.easycam.CameraControllerViewModel;
import in.balakrishnan.easycam.CaptureData;
import in.balakrishnan.easycam.FileUtils;
import in.balakrishnan.easycam.StatusBarUtil;
import in.balakrishnan.easycam.imageBadgeView.ImageBadgeView;

import static android.media.ExifInterface.ORIENTATION_UNDEFINED;


public class CaptureFragment extends Fragment
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Conversion from screen rotation to JPEG orientation for original image.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    /**
     * Conversion from screen rotation to JPEG orientation for thumb image.
     */
    private static final SparseIntArray THUMB_ORIENTATIONS = new SparseIntArray();
    /**
     * Static value for camera permission
     */
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "CaptureFragment";
    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;
    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;
    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;
    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;
    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

        THUMB_ORIENTATIONS.append(Surface.ROTATION_0, 0);
        THUMB_ORIENTATIONS.append(Surface.ROTATION_90, 270);
        THUMB_ORIENTATIONS.append(Surface.ROTATION_180, 180);
        THUMB_ORIENTATIONS.append(Surface.ROTATION_270, 90);
    }

    //************************** Setup Flags **********************************

    CameraControllerViewModel cameraControllerViewModel;
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */

    boolean isCameraOpen = false;
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

    //*************************************************************************
    /**
     * Set this flag as true to enable 'Preview' and 'Done' views animation using orientation
     */
    private boolean enableRotationAnimation;
    /**
     * This int value is used to set boundary value to check the click
     */
    private int CLICK_ACTION_THRESHOLD = 200;
    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;
    private String mFrontCameraId;
    private String mBackCameraId;
    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * The {@link Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    /**
     * This is the output file for our picture.
     */
    private File mFile;
    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;
    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    private CameraSelection cameraSelection = CameraSelection.BACK;
    private OnCaptureInteractionListener mListener;
    private boolean mManualFocusEngaged = false;
    private ImageBadgeView badgeImageView;
    private float startX;
    private float startY;
    private long mLastClickTime = 0L;
    private int fromDegree = 0;
    private TextView btnDone;
    private AppCompatButton btnCapture;
    private String bucketName = "default";
    private ImageView ivSwitchCamera, ivFlash;
    private FlashType flashType = FlashType.OFF;
    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "requestCameraPermission: mSurfaceTextureListener ");
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: " + width);
            Log.d(TAG, "onSurfaceTextureSizeChanged: " + height);
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Log.d(TAG, "process: focused ");
                    capturePicture(result);
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        Log.d(TAG, "process: captureStillPicture STATE_WAITING_NON_PRECAPTURE ");
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        private void capturePicture(CaptureResult result) {
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (afState == null || cameraControllerViewModel.getCameraSelection() == CameraSelection.FRONT) {
                Log.d(TAG, "process: captureStillPicture afState == null ");
                mState = STATE_PICTURE_TAKEN;
                captureStillPicture();
            } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                    CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                // CONTROL_AE_STATE can be null on some devices
                Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    mState = STATE_PICTURE_TAKEN;
                    Log.d(TAG, "captureStillPicture: CONTROL_AE_STATE_CONVERGED");
                    captureStillPicture();
                } else {
                    runPrecaptureSequence();
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private boolean launchNextActivity = false;

    public CaptureFragment() {

    }

    static CaptureFragment newInstance(boolean previewIconVisibility,
                                       boolean previewPageRedirection,
                                       boolean previewEnableCount,
                                       boolean enableDone,
                                       String doneButtonString,
                                       int captureButtonDrawable,
                                       int doneButtonDrawable,
                                       int MIN_PHOTO,
                                       int MAX_PHOTO,
                                       boolean singlePhotoMode,
                                       boolean fullscreenMode,
                                       boolean manualFocus,
                                       boolean enableRotationAnimation,
                                       String bucketName,
                                       boolean launchNextActivity) {

        Bundle args = new Bundle();
        args.putBoolean("previewIconVisibility", previewIconVisibility);
        args.putBoolean("previewPageRedirection", previewPageRedirection);
        args.putBoolean("previewEnableCount", previewEnableCount);
        args.putBoolean("enableDone", enableDone);
        args.putString("doneButtonString", doneButtonString);
        args.putInt("captureButtonDrawable", captureButtonDrawable);
        args.putInt("doneButtonDrawable", doneButtonDrawable);
        args.putInt("MIN_PHOTO", MIN_PHOTO);
        args.putInt("MAX_PHOTO", MAX_PHOTO);
        args.putBoolean("singlePhotoMode", singlePhotoMode);
        args.putBoolean("fullscreenMode", fullscreenMode);
        args.putBoolean("manualFocus", manualFocus);
        args.putBoolean("enableRotationAnimation", enableRotationAnimation);
        args.putBoolean("launchNextActivity", launchNextActivity);
        args.putString("bucketName", bucketName);

        CaptureFragment fragment = new CaptureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Load values from Bundle when ever require
     */
    public void loadValuesFromBundle() {
        Bundle b = getArguments();
        previewIconVisibility = b.getBoolean("previewIconVisibility", true);
        previewPageRedirection = b.getBoolean("previewPageRedirection", true);
        previewEnableCount = b.getBoolean("previewEnableCount", true);
        enableDone = b.getBoolean("enableDone", true);
        doneButtonString = b.getString("doneButtonString", "Done");
        captureButtonDrawable = b.getInt("captureButtonDrawable", 0);
        doneButtonDrawable = b.getInt("doneButtonDrawable", 0);
        min_photo = b.getInt("MIN_PHOTO", 0);
        max_photo = b.getInt("MAX_PHOTO", 100);
        singlePhotoMode = b.getBoolean("singlePhotoMode", false);
        fullscreenMode = b.getBoolean("fullscreenMode", false);
        manualFocus = b.getBoolean("manualFocus", true);
        enableRotationAnimation = b.getBoolean("enableRotationAnimation", true);
        launchNextActivity = b.getBoolean("launchNextActivity", true);
        bucketName = b.getString("bucketName", "");
    }

    public boolean isPreviewIconVisiblity() {
        return previewIconVisibility;
    }

    public void setPreviewIconVisiblity(boolean previewIconVisiblity) {
        this.previewIconVisibility = previewIconVisiblity;
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

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */

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

    public int getMIN_PHOTO() {
        return min_photo;
    }

    public void setMIN_PHOTO(int MIN_PHOTO) {
        this.min_photo = MIN_PHOTO;
    }

    public int getMAX_PHOTO() {
        return max_photo;
    }

    public void setMAX_PHOTO(int MAX_PHOTO) {
        this.max_photo = MAX_PHOTO;
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

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        StatusBarUtil.showStatusBar(this);
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {

        cameraControllerViewModel = ((CameraControllerActivity) getActivity()).obtainViewModel();
        cameraSelection = cameraControllerViewModel.getCameraSelection();
        flashType = cameraControllerViewModel.getFlashType();
        loadValuesFromBundle();

        initializeViews(view);

        setUpView(view);

        setUpListeners(view);
    }

    /**
     * Setup observers for Live Data in the {@link CameraControllerViewModel} and Listeners for view clicks
     *
     * @param view Root view
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setUpListeners(final View view) {
        cameraControllerViewModel.ld_captureData.observe(getViewLifecycleOwner(), new Observer<List<CaptureData>>() {
            @Override
            public void onChanged(List<CaptureData> captureData) {
                Log.d(TAG, "onChanged: ");
                if (previewIconVisibility & cameraControllerViewModel.bitmapList.size() > 0) {
                    // Set Thumb image from List
                    badgeImageView.setImageBitmap(captureData.get(captureData.size() - 1).getThumbBitmap());
                    if (previewEnableCount)
                        //Update the No of image in view
                        badgeImageView.setBadgeValue(cameraControllerViewModel.bitmapList.size());
                } else {
                    // This part is called when there is no value in list
                    // Removes the Preview view from teh view
                    badgeImageView.setImageBitmap(null);
                    badgeImageView.setBadgeValue(0);
                }
                if (captureData.size() >= max_photo)
                    view.findViewById(R.id.btn_capture).setAlpha(.5f);
                else
                    view.findViewById(R.id.btn_capture).setAlpha(1.0f);

            }
        });

        cameraControllerViewModel.tempBitmap.observe(getViewLifecycleOwner(), new Observer<CaptureData>() {
            @Override
            public void onChanged(CaptureData captureData) {
                Log.d(TAG, "onChanged: " + captureData.getOriginalFileName());
                if (cameraControllerViewModel.bitmapList.size() < 5) {
                    cameraControllerViewModel.addToList(captureData);
                }
            }
        });

        view.findViewById(R.id.ibv_icon2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previewPageRedirection && cameraControllerViewModel.bitmapList.size() > 0 && TextUtils.isEmpty(cameraControllerViewModel.getPeekValue()))
                    mListener.goToPreviewMode();
            }
        });

        view.findViewById(R.id.btn_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mis-clicking prevention, using threshold of 600 ms
                if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "onClick: " + cameraControllerViewModel.bitmapList.size());
                if (cameraControllerViewModel.bitmapList.size() >= max_photo) {
                    Toast.makeText(requireContext(), "You have reached maximum limit", Toast.LENGTH_SHORT).show();
                    return;
                }
                takePicture();
            }
        });
        /**
         * Close button Click Listener
         */
        view.findViewById(R.id.iv_capture_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });
        /**
         * Done button Click Listener
         */
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(cameraControllerViewModel.getPeekValue())) {
                    Log.d(TAG, "onClick: " + min_photo);
                    Log.d(TAG, "onClick: " + cameraControllerViewModel.bitmapList.size());
                    if (cameraControllerViewModel.bitmapList.size() < min_photo) {
                        Toast.makeText(requireContext(), "Require minimum " + NumberToWords.convert(min_photo) + (min_photo > 1 ? " photos" : " photo"), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mListener.onComplete();
                }
            }
        });
        /**
         * Preview View Touch Listener for Manual focus
         */
        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch: ");
                if (manualFocus)
                    switch (motionEvent.getAction()) {

                        case MotionEvent.ACTION_DOWN: {
                            startX = motionEvent.getX();
                            startY = motionEvent.getY();
                            break;
                        }

                        case MotionEvent.ACTION_UP: {
                            float endX = motionEvent.getX();
                            float endY = motionEvent.getY();
                            if (isAClick(startX, endX, startY, endY)) {
                                Log.d(TAG, "onTouch: MANUAL FOCUS.");
                                startManualFocus(view, motionEvent);
                            }
                            break;
                        }
                    }

                return true;
            }
        });

        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCamera(true);
            }
        });

        ivFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                flashType = FlashType.getInstance((flashType.getCurrentType() + 1) % 3);
                cameraControllerViewModel.setFlashType(flashType);
                Log.d(TAG, "onClick: " + flashType);
                createCameraPreviewSession();
                ivFlash.setImageDrawable(getResources().getDrawable(cameraControllerViewModel.getFlashType().getResourceId()));
            }

        });
    }

    /**
     * Initialize view objects
     *
     * @param view
     */
    private void initializeViews(View view) {
        mTextureView = view.findViewById(R.id.texture);
        mTextureView.setFullScreenMode(fullscreenMode);
        btnDone = view.findViewById(R.id.btn_capture_add);
        btnCapture = view.findViewById(R.id.btn_capture);
        badgeImageView = view.findViewById(R.id.ibv_icon2);
    }

    /**
     * Setup views with control variables from {@link CaptureFragmentBuilder}
     *
     * @param view
     */
    private void setUpView(View view) {
        if (!fullscreenMode) {
            ConstraintLayout layout = view.findViewById(R.id.capture_container);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(layout);
            constraintSet.connect(R.id.texture, ConstraintSet.TOP, R.id.iv_capture_close, ConstraintSet.BOTTOM, 0);
            constraintSet.connect(R.id.texture, ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.texture, ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 0);
            constraintSet.applyTo(layout);
            mTextureView.requestLayout();
        }
        if (!previewIconVisibility)
            badgeImageView.setVisibility(View.GONE);
        if (previewEnableCount) {
            badgeImageView.visibleBadge(false);
        }
        if (captureButtonDrawable != 0)
            btnCapture.setBackground(getActivity().getDrawable(captureButtonDrawable));
        if (doneButtonDrawable != 0)
            btnDone.setBackground(getActivity().getDrawable(doneButtonDrawable));
        btnDone.setVisibility(enableDone ? View.VISIBLE : View.GONE);
        btnDone.setText(doneButtonString);
        ivSwitchCamera = view.findViewById(R.id.iv_switch_cam);
        ivFlash = view.findViewById(R.id.iv_flash);
        ivFlash.setImageDrawable(getResources().getDrawable(cameraControllerViewModel.getFlashType().resourceId));
        Log.d(TAG, "setUpView: " + cameraControllerViewModel.getFlashType());
    }

    /**
     * Detect the touch event is a click or not
     *
     * @param startX initial X in view
     * @param endX   end x in view
     * @param startY initial Y in view
     * @param endY   end Y in view
     * @return true if it is a click else false
     */
    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }

    /**
     * Logic for starting manual Focus
     *
     * @param view        root view
     * @param motionEvent Click motion event to detect and for a focused area
     * @return true if focused else
     */
    private boolean startManualFocus(View view, MotionEvent motionEvent) {
        Log.d(TAG, "startManualFocus: called");

        if (mManualFocusEngaged) {
            Log.d(TAG, "startManualFocus: Manual focus already engaged");
            return true;
        }

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);

            final Rect sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            //TODO: here I ju
            //
            // st flip x,y, but this needs to correspond with the sensor orientation (via SENSOR_ORIENTATION)
            final int y = (int) ((motionEvent.getX() / (float) view.getWidth()) * (float) sensorArraySize.height());
            final int x = (int) ((motionEvent.getY() / (float) view.getHeight()) * (float) sensorArraySize.width());
            final int halfTouchWidth = (int) motionEvent.getTouchMajor(); //TODO: this doesn't represent actual touch size in pixel. Values range in [3, 10]...
            final int halfTouchHeight = (int) motionEvent.getTouchMinor();
            MeteringRectangle focusAreaTouch = new MeteringRectangle(Math.max(x - halfTouchWidth, 0),
                    Math.max(y - halfTouchHeight, 0),
                    halfTouchWidth * 2,
                    halfTouchHeight * 2,
                    MeteringRectangle.METERING_WEIGHT_MAX - 1);

            CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mManualFocusEngaged = false;

                    if (request.getTag() == "FOCUS_TAG") {
                        //the focus trigger is complete -
                        //resume repeating (preview surface will get frames), clear AF trigger
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null);

                        //reset to get ready to capture a picture
                        try {
                            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e(TAG, "startManualFocus: Manual AF failure: " + failure);
                    mManualFocusEngaged = false;
                }
            };

            //first stop the existing repeating request
            mCaptureSession.stopRepeating();

            //cancel any existing AF trigger (repeated touches, etc.)
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);

            //Now add a new AF trigger with focus region
            if (isMeteringAreaAFSupported()) {
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{focusAreaTouch});
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{focusAreaTouch});
            }
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            mPreviewRequestBuilder.setTag("FOCUS_TAG"); //we'll capture this later for resuming the preview

            //then we ask for a single request (not repeating!)
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallbackHandler, mBackgroundHandler);
            mManualFocusEngaged = true;

        } catch (CameraAccessException e) {
            e.printStackTrace();
            return true;
        }

        return true;
    }

    /**
     * To check isMeteringAreaAFSupported, This is called from {@see startManualFocus} check AF is supported
     *
     * @return
     */
    private boolean isMeteringAreaAFSupported() {

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        return characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) >= 1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mFile = FileUtils.getFile(getContext(), bucketName, "pic.jgp");
    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            {
                Log.d(TAG, "requestCameraPermission: onResume ");

                openCamera(mTextureView.getWidth(), mTextureView.getHeight());
            }
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera(false);
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    mFrontCameraId = cameraId;
                } else if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mBackCameraId = cameraId;
                }
                if (!TextUtils.isEmpty(mFrontCameraId) && !TextUtils.isEmpty(mBackCameraId) && ivSwitchCamera != null) {
                    ivSwitchCamera.setVisibility(View.VISIBLE);
                }
            }

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);

                // We don't use a front facing camera in this sample.
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cameraControllerViewModel.getCameraSelection() == CameraSelection.BACK) {
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                } else if (cameraControllerViewModel.getCameraSelection() == CameraSelection.FRONT)
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        continue;
                    }


                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, /*maxImages*/2);

                mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(final ImageReader reader) {
                        Log.d(TAG, "setOnImageAvailableListener: " + cameraControllerViewModel.getPeekValue());
                        Image mImage = reader.acquireLatestImage();
                        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        FileOutputStream output = null;
                        Bitmap rotatedBitmap = null;
                        Bitmap originalBitmap = null;

                        try {
                            String currTime = "" + System.currentTimeMillis();
                            File mFile = new File(getContext().getCacheDir(), "pic.jpg");
                            output = new FileOutputStream(mFile);
                            output.write(bytes);

                            originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);

                            ExifInterface exif = new ExifInterface(mFile.getPath());
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ORIENTATION_UNDEFINED);
                            Log.d(TAG, "onImageAvailable: exif " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));

                            int rotationAngle = 0;
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                                rotationAngle = 90;
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                                rotationAngle = 180;
                            if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                                rotationAngle = 270;

                            Matrix matrix = new Matrix();
                            matrix.setRotate(rotationAngle, (float) originalBitmap.getWidth() / 2, (float) originalBitmap.getHeight() / 2);
                            rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

                            File originalImage = FileUtils.getFile(getContext(), bucketName, cameraControllerViewModel.getPeekValue() + ".jpg");
                            if (originalImage.exists()) {
                                originalImage.delete();
                            }
                            if (cameraControllerViewModel.getCameraSelection() == CameraSelection.FRONT) {
                                Log.d(TAG, "onImageAvailable: Fliped");
                                Matrix imageFlipMatrix = new Matrix();
                                imageFlipMatrix.setScale(-1, 1);
                                rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), imageFlipMatrix, true);
                            }
                            originalImage = FileUtils.getFile(getContext(), bucketName, currTime + ".jpg");
                            FileOutputStream originalStream = new FileOutputStream(originalImage);
                            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, originalStream);
                            originalStream.close();
                            cameraControllerViewModel.changeTimeStamp(cameraControllerViewModel.getPeekValue(), currTime);
                            cameraControllerViewModel.removeFirst();
                            if (singlePhotoMode)
                                mListener.onComplete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            mImage.close();
                            if (null != output) {
                                try {
                                    output.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            // recycle bitmap storage
                            if (originalBitmap != null)
                                originalBitmap.recycle();
                            if (rotatedBitmap != null)
                                rotatedBitmap.recycle();

                        }
                    }
                }, mBackgroundHandler);

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;


                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;
                if (mFlashSupported && ivFlash != null) {
                    Log.d(TAG, "setUpCameraOutputs: flash " + flashType);
                    ivFlash.setImageDrawable(getResources().getDrawable(cameraControllerViewModel.getFlashType().resourceId));
                    ivFlash.setVisibility(View.VISIBLE);
                } else {
                    ivFlash.setVisibility(View.GONE);
                }
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Open camera and attach callbacks and ThreadHandler
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestCameraPermission: openCamera ");
//            ((CameraControllerActivity) getActivity()).checkPermissionAndLaunch("from fragment openCamera");
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager systemService = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        CameraManager manager = systemService;
        try {
            boolean b = mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS);
            if (!b) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            isCameraOpen = true;
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera(boolean reopen) {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
            isCameraOpen = false;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
            if (reopen)
                switchCamera();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_AUTO);
                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    private void takePicture() {
        if (isCameraOpen)
            lockFocus();
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        Log.d(TAG, "captureStillPicture: ");
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mTextureView, "alpha", 1f, .0f);
        fadeOut.setDuration(100);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mTextureView, "alpha", .0f, 1f);
        fadeIn.setDuration(100);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        Bitmap bm = mTextureView.getBitmap();
        Matrix matrix = new Matrix();
        int rotation = ((CameraControllerActivity) getActivity()).getOrientation();
        matrix.setRotate(THUMB_ORIENTATIONS.get(rotation), (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        CaptureData captureData = new CaptureData(rotatedBitmap, "" + System.currentTimeMillis());
        cameraControllerViewModel.addToQueue(captureData.getOriginalFileName());
        cameraControllerViewModel.tempBitmap.postValue(captureData);
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO);
            setAutoFlash(captureBuilder);

            // Orientation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d(TAG, mFile.toString());

                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, mBackgroundHandler);
            unlockFocus();
            mAnimationSet.start();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        int deviceOrientation = ORIENTATIONS.get(rotation);
        return (deviceOrientation + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            if (mPreviewRequestBuilder == null || mCaptureCallback == null || mBackgroundHandler == null || mCaptureSession == null)
                return;
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to set Auto Flash if flash is supported by device and flash is enabled by user
     *
     * @param requestBuilder {@link CaptureRequest.Builder}
     */
    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            switch (cameraControllerViewModel.getFlashType().currentType) {
                case 2:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    break;
                case 1:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                    break;
                case 0:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON);
            }
        } else {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCaptureInteractionListener) {
            mListener = (OnCaptureInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This function is used to rotate the views with device rotation.
     *
     * @param t angle to rotate
     */
    public void rotateView(final int t) {
        if (enableRotationAnimation) {
            AnimationSet animSet = new AnimationSet(true);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            animSet.setFillEnabled(true);
            final RotateAnimation animRotate = new RotateAnimation(fromDegree, fromDegree + t,
                    RotateAnimation.ZORDER_TOP, 0.5f,
                    RotateAnimation.ZORDER_TOP, 0.5f);
            animRotate.setDuration(500);
            animRotate.setFillAfter(true);
            animSet.addAnimation(animRotate);
            badgeImageView.startAnimation(animSet);
            ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(btnDone,
                    "rotation", fromDegree, fromDegree + t);
            imageViewObjectAnimator.setInterpolator(new DecelerateInterpolator());
            imageViewObjectAnimator.setDuration(500); // miliseconds
            imageViewObjectAnimator.start();
            fromDegree = t + fromDegree;
        }
    }

    public void switchCamera() {
        cameraSelection = cameraSelection == CameraSelection.BACK ? CameraSelection.FRONT : CameraSelection.BACK;
        cameraControllerViewModel.setCameraSelection(cameraSelection);
        reopenCamera();
    }

    private void reopenCamera() {
        if (mTextureView.isAvailable()) {
            Log.d(TAG, "requestCameraPermission: reopenCamera ");
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * This Listener is used to communicate with Parent Activity {@link CameraControllerActivity}
     */
    public interface OnCaptureInteractionListener {
        /**
         * When user clicks on preview icon
         */
        void goToPreviewMode();

        /**
         * When user clicks Done button
         */
        void onComplete();
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

}