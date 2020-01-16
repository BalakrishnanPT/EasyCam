package in.balakrishnan.cameramodule.easyCamFragmentExample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import in.balakrishnan.cameramodule.R;
import in.balakrishnan.easycam.capture.EasyCamFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class EasyCamFragmentExample extends EasyCamFragment {
    private static final String TAG = "EasyCamFragmentExample";

    public EasyCamFragmentExample() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_example, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnCapture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    @Override
    public void getImageThumb(Bitmap bitmap) {
        Log.d(TAG, "getImageThumb: ");
    }

    @Override
    public void getImage(Bitmap bitmap) {
        Log.d(TAG, "getImage: ");
    }

    @Override
    public int getTextureResource() {
        return R.id.texture;
    }

}
