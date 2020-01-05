package in.balakrishnan.easycam.capture;

import android.hardware.camera2.CameraAccessException;

import java.io.IOException;

/**
 * Created by wesley on 2016/03/07.
 */
public interface Camera2Listener {

    void onCameraException(CameraAccessException cae);

    void onNullPointerException(NullPointerException npe);

    void onInterruptedException(InterruptedException ie);

    void onIOException(IOException ioe);

    void onConfigurationFailed();
}