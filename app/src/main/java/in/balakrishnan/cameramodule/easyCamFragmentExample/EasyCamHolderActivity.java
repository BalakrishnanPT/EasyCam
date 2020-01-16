package in.balakrishnan.cameramodule.easyCamFragmentExample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.balakrishnan.cameramodule.R;

public class EasyCamHolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_cam_holder);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flExampleContainer, new EasyCamFragmentExample())
                .commit();
    }
}
