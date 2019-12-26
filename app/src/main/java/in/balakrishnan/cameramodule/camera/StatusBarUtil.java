package in.balakrishnan.cameramodule.camera;

import android.view.View;

import androidx.fragment.app.Fragment;

public class StatusBarUtil {
    /**
     * Camera Module's Theme is set, such that status bar is hidden, to override that call this function
     *
     * @param previewFragment
     */
    public static void showStatusBar(Fragment previewFragment) {
        previewFragment.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        final int flags = View.SYSTEM_UI_FLAG_VISIBLE;
        previewFragment.getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);
        final View decorView = previewFragment.getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }

    public static void hideStatusBar(Fragment fragment) {
        fragment.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        fragment.getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);
        final View decorView = fragment.getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });
    }
}