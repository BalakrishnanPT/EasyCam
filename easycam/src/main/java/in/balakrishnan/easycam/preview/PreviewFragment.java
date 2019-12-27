package in.balakrishnan.easycam.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import in.balakrishnan.easycam.R;
import in.balakrishnan.easycam.CameraControllerActivity;
import in.balakrishnan.easycam.CameraControllerViewModel;
import in.balakrishnan.easycam.CaptureData;
import in.balakrishnan.easycam.FileUtils;
import in.balakrishnan.easycam.StatusBarUtil;
import in.balakrishnan.easycam.capture.NumberToWords;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static in.balakrishnan.easycam.BitmapHelper.calculateInSampleSize;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPreviewFragmentInteraction}
 * interface.
 */
public class PreviewFragment extends Fragment {
    CameraControllerViewModel cameraControllerViewModel;
    RecyclerView recyclerView;
    ImageView ivClose;
    int currPos = 0, min_photo = 0, max_photo = 100;
    List<CaptureData> captureData = new ArrayList<>();
    private OnPreviewFragmentInteraction mListener;
    private PreviewRecyclerViewAdapter rvAdapter;
    private ImageView imageView;
    private String bucketName = "default";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PreviewFragment() {
    }

    public static PreviewFragment newInstance(int min_photo, int max_photo, String bucketName) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("min_photo", min_photo);
        bundle.putInt("max_photo", max_photo);
        bundle.putString("bucketName", bucketName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        showStatusBar();
        loadValueFromBundle();
        final View view = inflater.inflate(R.layout.fragment_preview_list, container, false);

        return view;
    }

    private void loadValueFromBundle() {
        Bundle bundle = getArguments();
        min_photo = bundle.getInt("min_photo");
        max_photo = bundle.getInt("max_photo");
        bucketName = bundle.getString("bucketName");
    }

    /**
     * Camera Module's Theme is set, such that status bar is hidden, to override that call this function
     */
    private void showStatusBar() {
        StatusBarUtil.showStatusBar(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraControllerViewModel = ((CameraControllerActivity) getActivity()).obtainViewModel();

        imageView = view.findViewById(R.id.iv_preview);
        ivClose = view.findViewById(R.id.iv_preview_close);
        recyclerView = view.findViewById(R.id.rv_previewList);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        captureData = getBitmapList();

        rvAdapter = new PreviewRecyclerViewAdapter(captureData, new PreviewRecyclerViewAdapter.OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(int position) {
                currPos = position;
                rvAdapter.setCurrPos(position);
                rvAdapter.notifyDataSetChanged();
                setBitmapToPreview(captureData.get(position));
            }
        });

        recyclerView.setAdapter(rvAdapter);
        // Set initial image from list in Preview
        setBitmapToPreview(captureData.get(0));
        /**
         * Close Button Click listener, Goes to {@link in.balakrishnan.components.camera.capture.CaptureFragment}
         */
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToCaptureView();
            }
        });

        /**
         * Delete Button Click Listener, Delete image from the list
         */
        view.findViewById(R.id.iv_preview_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteImage();
            }
        });
        /**
         * Add button click listener, Checks if the Minimum and Maximum photo limits are satisfied
         */
        view.findViewById(R.id.btn_prev_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraControllerViewModel.bitmapList.size() < min_photo) {
                    Toast.makeText(requireContext(), "Require minimum " + NumberToWords.convert(min_photo) + " photos", Toast.LENGTH_SHORT).show();
                    return;
                }
                mListener.onComplete();
            }
        });

        /**
         * Camera Capture button click, Opens {@link in.balakrishnan.components.camera.capture.CaptureFragment}
         */
        view.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Logic to delete image from List and update adapter
     */
    private void deleteImage() {
        FileUtils.clearFile(getContext(), bucketName, cameraControllerViewModel.bitmapList.get(currPos).getOriginalFileName());
        cameraControllerViewModel.removeAt(currPos);
        if (currPos == cameraControllerViewModel.bitmapList.size()) {
            currPos = currPos - 1;
        }
        if (cameraControllerViewModel.bitmapList.size() == 0)
            mListener.goToCaptureView();
        else
            setBitmapToPreview(cameraControllerViewModel.bitmapList.get(currPos));

        rvAdapter.setCurrPos(currPos);
        rvAdapter.updateList(getBitmapList());
        rvAdapter.notifyDataSetChanged();
    }


    /**
     * Returns bitmap list to be loaded
     *
     * @return
     */
    private List<CaptureData> getBitmapList() {
        return cameraControllerViewModel.bitmapList;
    }

    /**
     * This function is used to update the Preview Image
     *
     * @param path path of the image
     */
    void setBitmapToPreview(CaptureData path) {
        File originalImage;
        boolean isImage = path.getOriginalFileName().endsWith(".jpg");
        if (isImage) {
            originalImage = FileUtils.getFile(getContext(), bucketName, path.getOriginalFileName(), false);
        } else {
            originalImage = FileUtils.getFile(getContext(), bucketName, path.getOriginalFileName() + ".jpg");
        }
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(originalImage.getPath(), options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, options.outWidth / 4, options.outHeight / 4);
        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(originalImage.getPath(), options);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PreviewFragment.OnPreviewFragmentInteraction) {
            mListener = (PreviewFragment.OnPreviewFragmentInteraction) context;
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
     * This Listener is used to communicate with Parent Activity {@link CameraControllerActivity}
     */
    public interface OnPreviewFragmentInteraction {
        /**
         * When user clicks on camera icon or close icon
         */
        void goToCaptureView();

        /**
         * When user clicks Done button
         */
        void onComplete();
    }
}
