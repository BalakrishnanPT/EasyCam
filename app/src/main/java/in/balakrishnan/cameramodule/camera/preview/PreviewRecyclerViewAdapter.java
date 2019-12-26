package in.balakrishnan.cameramodule.camera.preview;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import in.balakrishnan.cameramodule.R;
import in.balakrishnan.cameramodule.camera.CaptureData;

import java.util.List;


public class PreviewRecyclerViewAdapter extends RecyclerView.Adapter<PreviewRecyclerViewAdapter.ViewHolder> {

    private final OnListFragmentInteractionListener mListener;
    private List<CaptureData> mValues;
    private int currPos = 0;
    private static final String TAG = "PreviewRecyclerViewAdap";

    PreviewRecyclerViewAdapter(List<CaptureData> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    void setCurrPos(int currPos) {
        this.currPos = currPos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Bitmap thumbBitmap = mValues.get(position).getThumbBitmap();
        Log.d(TAG, "onBindViewHolder: " + thumbBitmap.getAllocationByteCount());
        Log.d(TAG, "onBindViewHolder: " + thumbBitmap.getHeight());
        Log.d(TAG, "onBindViewHolder: " + thumbBitmap.getWidth());
        holder.mIdView.setImageBitmap(thumbBitmap);

        if (currPos != position)
            holder.rl_bg.setBackground(null);
        else
            holder.rl_bg.setBackgroundColor(Color.parseColor("#D8F652"));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    void updateList(List<CaptureData> bitmapList) {
        this.mValues = bitmapList;
        notifyDataSetChanged();
    }

    /**
     * This interface is used to sent information from {@link PreviewRecyclerViewAdapter} and {@link PreviewFragment}
     */
    public interface OnListFragmentInteractionListener {
        /**
         * This method is triggered when a object in list is clicked
         *
         * @param position postion of item in list
         */
        void onListFragmentInteraction(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mIdView;
        RelativeLayout rl_bg;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            rl_bg = view.findViewById(R.id.rl_bg);
        }

    }
}
