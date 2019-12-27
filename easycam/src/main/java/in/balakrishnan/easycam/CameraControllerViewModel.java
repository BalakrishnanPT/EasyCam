package in.balakrishnan.easycam;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import in.balakrishnan.easycam.capture.CameraSelection;
import in.balakrishnan.easycam.capture.FlashType;

/**
 * Created by BalaKrishnan on 2019-07-03.
 */
public class CameraControllerViewModel extends AndroidViewModel {
    private static final String TAG = "CameraControllerViewMod";

    // This data is used to communicate result from Camera2 API, We pass thumb image
    public MutableLiveData<CaptureData> tempBitmap = new MutableLiveData<>();
    // Holds the list of all bitmaps that are selected
    public MutableLiveData<List<CaptureData>> ld_captureData = new MutableLiveData<>();
    // Bitmap list to update ld_captureData
    public List<CaptureData> bitmapList = new ArrayList<>();
    // This Queue is maintained for holding image's timestamp when 2 or more photos are taken continuously
    Queue<String> fileNames = new LinkedList<>();
    CameraSelection cameraSelection = CameraSelection.BACK;
    FlashType flashType = FlashType.OFF;

    public FlashType getFlashType() {
        return flashType;
    }

    public void setFlashType(FlashType flashType) {
        this.flashType = flashType;
    }

    public CameraControllerViewModel(@NonNull Application application) {
        super(application);
    }

    public CameraSelection getCameraSelection() {
        return cameraSelection;
    }

    public void setCameraSelection(CameraSelection cameraSelection) {
        this.cameraSelection = cameraSelection;
    }

    /**
     * Add value to queue
     *
     * @param s time stamp
     */
    public void addToQueue(String s) {
        fileNames.add(s);
    }

    /**
     * Cheks whether queue is empty, and return value of 1st element in queue
     *
     * @return time stamp
     */
    public String getPeekValue() {
        if (fileNames.size() == 0) return "";
        return fileNames.peek();
    }

    /**
     * Removes first element in queue
     */
    public void removeFirst() {
        fileNames.remove();
    }

    /**
     * This function is called when original image is received from camera2 API
     *
     * @param from old file name
     * @param to   original file name
     */
    public void changeTimeStamp(String from, String to) {
        for (CaptureData captureData : bitmapList) {
            if (captureData.getOriginalFileName().equals(from))
                captureData.setOriginalFileName(to);
        }
        ld_captureData.postValue(bitmapList);
    }

    /**
     * Add Value to the bitapList and ld_capturedata (LiveData)
     * This function is responsible to update the elemets which are linked to UI
     *
     * @param captureData
     */
    public void addToList(CaptureData captureData) {
        if (captureData.thumbBitmap == null) return;
        if (ld_captureData.getValue() == null) {
            bitmapList.add(captureData);
            ld_captureData.setValue(bitmapList);
            return;
        }

        if (ld_captureData.getValue().size() == 0 || !captureData.originalFileName.equals(ld_captureData.getValue().get(ld_captureData.getValue().size() - 1).originalFileName)) {
            bitmapList.add(captureData);
            ld_captureData.setValue(bitmapList);
        }
    }

    /**
     * This function is used to remove a value from the bitmapList and ld_captureData
     *
     * @param position position of thet element
     */
    public void removeAt(int position) {
        FileUtils.clearFile(getApplication().getApplicationContext(), "Draft", "Captured_", bitmapList.get(position).getOriginalFileName() + ".jpg");
        bitmapList.remove(position);
        ld_captureData.postValue(bitmapList);
        if (position == bitmapList.size()) {
            tempBitmap.postValue(new CaptureData(null, ""));
        }
        Log.d(TAG, "removeAt: bitmap List " + bitmapList.size());
        Log.d(TAG, "removeAt: capture " + ld_captureData.getValue().size());
    }
}
