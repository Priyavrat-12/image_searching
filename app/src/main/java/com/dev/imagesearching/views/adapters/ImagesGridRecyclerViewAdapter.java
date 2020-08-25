package com.dev.imagesearching.views.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.imagesearching.R;
import com.dev.imagesearching.models.Data;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import static com.dev.imagesearching.utils.NetworkConstants.IMAGE_BASE_URL;

/**
 * Adapter class : responsible to display images data into the grid recycler views.
 */
public class ImagesGridRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Data> mImagesDataList;
    private Context mContext;

    public static final int VIEW_TYPE_IMAGE_LAYOUT = 0; // For images layout.
    public static final int VIEW_TYPE_LOADING = 1; // For the paginated loading view.

    public ImagesGridRecyclerViewAdapter (final Context context) {
        mImagesDataList = new ArrayList<>();
        mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return mImagesDataList.get(position).hashCode();
    }

    public Data getItemAtPosition (int position) {
        return null != mImagesDataList ? mImagesDataList.get(position) : null;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (VIEW_TYPE_IMAGE_LAYOUT == viewType) {
            // Getting the images layout view.
            View imageItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list_item, parent, false);
            return new ImageItemViewHolder(imageItemView);
        } else {
            // Getting the progress view.
            View loadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.endless_scroll_loading_view, parent, false);
            return new LoadingViewHolder(loadingView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // Displaying the image data.
        if (holder instanceof  ImageItemViewHolder) {
            ImageItemViewHolder imageItemViewHolder = (ImageItemViewHolder) holder;
            imageItemViewHolder.bind(mImagesDataList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return null == mImagesDataList ? 0 : mImagesDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return null == mImagesDataList.get(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_IMAGE_LAYOUT;
    }

    public void addDataToList (List<Data> imagesDataList) {

        // As a safetyNet, initialize the images data list if null.
        if (null == mImagesDataList) {
            mImagesDataList = new ArrayList<>();
        }

        // Appending the provided results into original data list.
        mImagesDataList.addAll(imagesDataList);
        notifyDataSetChanged();
    }

    /**
     * Method to remove all the elements from grid recycler view.
     */
    public void clearImagesList () {
        if (null != mImagesDataList && !mImagesDataList.isEmpty()) {
            mImagesDataList.clear();

            // Notifying the data set changes to adapter, so that the result can be reflected properly.
            notifyDataSetChanged();
        }
    }

    /**
     * Method to add the load more view in the grid recycler view.
     * @param item null item to be added
     */
    public void addLoadMoreViewToList(final Data item) {
        if (null == mImagesDataList) {
            mImagesDataList = new ArrayList<>();
        }

        mImagesDataList.add(item);
    }

    /**
     * Method to remove the loading paginated view from grid recycler view.
     */
    public void removeLoadMoreViewFromList () {
        if (null != mImagesDataList && !mImagesDataList.isEmpty()) {
            mImagesDataList.remove(mImagesDataList.size() - 1);
            notifyDataSetChanged();
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View loadingView) {
            super(loadingView);

            // Find the views here.
            progressBar = loadingView.findViewById(R.id.progressBar);

        }
    }
    private  class ImageItemViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView imageView;
        TextView imageTitleTV;

        ImageItemViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find the views here.
            imageView = itemView.findViewById(R.id.image_view);
            imageTitleTV = itemView.findViewById(R.id.tv_image_title);
        }

        void bind (Data imageData) {

            if (null != imageData) {

                // Display the data into the views.

                // Displaying the image title if available.
                String imageTitle = imageData.getTitle();
                if (!TextUtils.isEmpty(imageTitle)) {
                    imageTitleTV.setText(imageTitle);
                }

                // Loading the image into view, if available.
                String imgURL = imageData.getCover();
                if (!TextUtils.isEmpty(imgURL)) {
                    imgURL = IMAGE_BASE_URL + imgURL + ".jpg";
                    Glide.with(mContext)
                            .load(imgURL)
                            .fitCenter()
                            .into(imageView);
                }
            }
        }
    }
}
