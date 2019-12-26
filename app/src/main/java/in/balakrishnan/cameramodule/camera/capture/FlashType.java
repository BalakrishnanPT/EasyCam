package in.balakrishnan.cameramodule.camera.capture;


import in.balakrishnan.cameramodule.R;

/**
 * Created by BalaKrishnan
 */
public enum FlashType {
    OFF(R.drawable.ic_flash_off, 0),
    ON(R.drawable.ic_flash_on, 1),
    AUTO(R.drawable.ic_flash_auto, 2);
    int resourceId;
    int currentType;

    FlashType(int resourceId, int currentType) {
        this.resourceId = resourceId;
        this.currentType = currentType;
    }

    public static FlashType getInstance(int t) {
        switch (t) {
            case 1:
                return ON;
            case 2:
                return AUTO;
            default:
                return OFF;
        }
    }

    public int getCurrentType() {
        return currentType;
    }

    public int getResourceId() {
        return resourceId;
    }
}
