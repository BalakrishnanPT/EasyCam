package in.balakrishnan.easycam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.OrientationEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.Arrays;

import in.balakrishnan.easycam.capture.CaptureFragment;
import in.balakrishnan.easycam.capture.CaptureFragmentBuilder;
import in.balakrishnan.easycam.preview.PreviewFragment;


public class CameraControllerActivity extends AppCompatActivity implements PreviewFragment.OnPreviewFragmentInteraction, CaptureFragment.OnCaptureInteractionListener {
    public static final int REQUEST_CAMERA_REQUEST_CODE = 113;
    private static final String TAG = "CameraControllerActivit";
    private static final int PERMISSION_REQUEST_CODE = 897;
    PreviewFragment previewFragment;
    CaptureFragment captureFragment;
    in.balakrishnan.easycam.capture.video.CaptureFragment fragment;
    int orientation = 0;
    int temp = 0;
    CameraBundle io;
    Bundle bundle = new Bundle();
    boolean havePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_controller);
        bundle = savedInstanceState;
        Log.d(TAG, "onCreate: ");
        io = getIntent().getParcelableExtra("inputData");
        checkPermissionAndLaunch("from activity onCreate");
    }

    public void checkPermissionAndLaunch(String s) {
        Log.d(TAG, "checkPermissionAndLaunch: " + s);
        if (checkPermission())
            setup(bundle);
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setup(bundle);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // TODO: 2019-12-26 Request Camera permission
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == REQUEST_CAMERA_REQUEST_CODE) {
            checkPermissionAndLaunch("from activity onActivityResult");
        }

    }

    private void setup(Bundle savedInstanceState) {
        havePermission = true;
        setupOrientationListener();
        if (io.isClearBucket())
            FileUtils.clearAllFiles(this, io.getBucket());
        if (io.isPreLoaded())
            loadFilesFromBucket();
        if (null == savedInstanceState) {
            goToCaptureView();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted

            return false;
        }
        return true;
    }

    private void loadFilesFromBucket() {
        File[] files = FileUtils.getAllFiles(this, io.getBucket());
        Arrays.sort(files);
        for (File file : files) {
            Log.d(TAG, "loadFilesFromBucket: " + file.getName());
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), new BitmapFactory.Options());
            CaptureData captureData = new CaptureData(bitmap, file.getName());
            obtainViewModel().addToList(captureData);
        }
    }

    private void setupOrientationListener() {
        //Orientation Listener to detect the rotation
        OrientationEventListener mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int rotation) {
                if (rotation > 330 && rotation < 360 || rotation < 30) {
                    orientation = 0;
                } else if (rotation > 60 && rotation < 120) {
                    orientation = 3;
                } else if (rotation > 150 && rotation < 210) {
                    orientation = 2;
                } else if (rotation > 240 && rotation < 300) {
                    orientation = 1;
                }
                if (temp != orientation) {
                    int rotationResult = temp - orientation;
                    if (rotationResult == -1 || rotationResult == 3) {
                        captureFragment.rotateView(90);
                    } else if (rotationResult == 1 || rotationResult == -3) {
                        captureFragment.rotateView(-90);
                    }
                    temp = orientation;
                }
            }
        };
        mOrientationListener.enable();

    }

    /**
     * When fragments or the activity requires ViewModel, This function has to be called.
     * <p>
     * NOTE : ViewModel is shared between fragments, If ViewModel is updated from one fragment,
     * updates occur in other fragment / Activity too.
     *
     * @return {CameraControllerViewModel.class}
     */
    public CameraControllerViewModel obtainViewModel() {
        return new ViewModelProvider(this).get(CameraControllerViewModel.class);
    }

    @Override
    public void goToCaptureView() {
        try {
            captureFragment = new CaptureFragmentBuilder().setBundle(io).createCaptureFragment();
            fragment = new in.balakrishnan.easycam.capture.video.CaptureFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_container, fragment)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onComplete() {

        int t = 0;
        Log.d(TAG, "onComplete: onComplete called");
        File directory = new File(FileUtils.getFilePath(this, io.getBucket()));
        File[] imageList = directory.listFiles();
        String[] paths = new String[imageList.length];
        if (imageList != null) {
            for (File file : imageList) {
                paths[t++] = file.getPath();
            }
        }
        Arrays.sort(paths);
        Intent intent = new Intent();
        intent.putExtra("resultData", paths);
        setResult(AppCompatActivity.RESULT_OK, intent);
        if (TextUtils.isEmpty(io.getClassName())) {
            finish();
        } else {
            ClassLauncher classLauncher = new ClassLauncher(this);
            try {
                Bundle bundle = new Bundle();
                bundle.putStringArray("resultData", paths);
                classLauncher.launchActivity(io.className, bundle);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void goToPreviewMode() {
        previewFragment = PreviewFragment.newInstance(io.getMin_photo(), io.getMax_photo(), io.getBucket());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container, previewFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (!havePermission) super.onBackPressed();
        if (isBackStack()) {
            Log.d(TAG, "onBackPressed: " + io.isSetResultOnBackPressed());
            if (io.isSetResultOnBackPressed())
                onComplete();
            super.onBackPressed();
        }
    }

    /**
     * This function is used to check if Preview fragment or upload fragment is top of the stack.
     * If it is present then it returns true, otherwise false
     *
     * @return boolean
     */

    private boolean isBackStack() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null)
                if (fragment instanceof PreviewFragment) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    goToCaptureView();
                    return false;
                }
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: ");
    }

    /**
     * Returns current orientation of the `device`
     *
     * @return
     */
    public int getOrientation() {
        return orientation;
    }

}
