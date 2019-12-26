package in.balakrishnan.cameramodule;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.balakrishnan.cameramodule.camera.CameraBundleBuilder;
import in.balakrishnan.cameramodule.camera.CameraControllerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, CameraControllerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("inputData", new CameraBundleBuilder()
                .setFullscreenMode(false)
                .setDoneButtonString("Add")
                .setSinglePhotoMode(false)
                .setMax_photo(3)
                .setManualFocus(true)
                .setBucketName(getClass().getName())
                .setPreviewEnableCount(true)
                .setPreviewIconVisiblity(true)
                .setPreviewPageRedirection(true)
                .setEnableDone(false)
                .setClearBucket(true)
                .createCameraBundle());
        startActivityForResult(intent, 214);
    }
}
