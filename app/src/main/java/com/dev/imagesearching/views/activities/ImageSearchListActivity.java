package com.dev.imagesearching.views.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dev.imagesearching.R;
import com.dev.imagesearching.models.Data;
import com.dev.imagesearching.models.ImagesResponse;
import com.dev.imagesearching.utils.AppUtils;
import com.dev.imagesearching.utils.DebouncedQueryTextListener;
import com.dev.imagesearching.utils.RecyclerItemClickListener;
import com.dev.imagesearching.utils.WrapContentGridLayoutManager;
import com.dev.imagesearching.viewmodels.ImageSearchingViewModel;
import com.dev.imagesearching.views.adapters.ImagesGridRecyclerViewAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.dev.imagesearching.utils.NetworkConstants.CODE_DEFAULT;

public class ImageSearchListActivity extends BaseActivity {

    // Page no. required to fetch the results for endless scroll.
    private int mImageListPageNo = 1;   // Initial value is 1, it can be incremented by one, with scrolls.

    // To enter the image search keyword.
    private SearchView mSearchView;

    // Adapter required to display the images in grid.
    private ImagesGridRecyclerViewAdapter mImageRecyclerViewAdapter;

    // View Model.
    private ImageSearchingViewModel mImageSearchingViewModel;

    // Identify if the network request is already running.
    private boolean mIsLoading;

    // UI Views.
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mImageListRecyclerView;
    private ProgressBar mMainProgressBarView;

    // Number of columns in grid view based on the orientation or screen.
    private static final int PORTRAIT_RECYCLER_VIEW_COL_SPAN = 3;
    private static final int LANDSCAPE_RECYCLER_VIEW_COL_SPAN = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search_list);

        // Initially loading the views, setting up observers etc.
        init();
    }

    @Override
    public void init() {
        super.init();

        // Finding the views.
        mImageListRecyclerView = findViewById(R.id.image_grid_recycler_view);
        mMainProgressBarView = findViewById(R.id.search_screen_main_progressbar);
        mSearchView = findViewById(R.id.search_view_et);
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);

        // Adding various required listeners to the grid recycler views.
        mImageListRecyclerView.setHasFixedSize(true);
        // Initially setting up the column span based on the orientation of screen.
        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
            mGridLayoutManager = new WrapContentGridLayoutManager(ImageSearchListActivity.this, PORTRAIT_RECYCLER_VIEW_COL_SPAN);
        } else {
            mGridLayoutManager = new WrapContentGridLayoutManager(ImageSearchListActivity.this, LANDSCAPE_RECYCLER_VIEW_COL_SPAN);
        }
        mImageListRecyclerView.setLayoutManager(mGridLayoutManager);
        mImageListRecyclerView.addOnScrollListener(mImagesRecyclerViewOnScrollListener);
        mImageListRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, (view, position) -> {

            // Moving to details screen with required data.
            navigateToDetailActivity(position);
        }));

        // Initializing the view model.
        mImageSearchingViewModel =  new ViewModelProvider(this).get(ImageSearchingViewModel.class);

        // Setting up the adapter to images grid recycler view.

        // Initializing the adapter with the observable data list, so that data changes can reflect directly, even without notifying the adapter for data set changes.
        List<Data> recyclerViewDataList = mImageSearchingViewModel.getRecyclerViewDataList();
        mImageRecyclerViewAdapter = new ImagesGridRecyclerViewAdapter(ImageSearchListActivity.this, recyclerViewDataList);
        mImageListRecyclerView.setAdapter(mImageRecyclerViewAdapter);

        // Setting up the observer to listen the network updates.
        setObservers();
    }

    @Override
    public void setObservers() {

        // Adding observer to listen the data retrieval updates and based on that refresh the images grid recycler view.
        MutableLiveData<ImagesResponse> imagesLiveData = mImageSearchingViewModel.getImagesObservableData();

        imagesLiveData.observe(this, (Observer<ImagesResponse>) imagesResponse -> {

            mIsLoading = false;

            if(mImageListPageNo == 1) {
                // hide the main progress bar, for first page.
                mMainProgressBarView.setVisibility(View.GONE);
            } else {
                // hide the pagination view.
                mImageRecyclerViewAdapter.removeLoadMoreViewFromList();
            }

            // Retrieved the images data updates, populating the recycler view with it.
            if (null != imagesResponse) {

                Log.e("ImageSearching", "Setting up data to adapter.");

                // Hiding the soft input keyboard, if opened.
                AppUtils.hideKeyboard(ImageSearchListActivity.this, mSearchView);

                // If the images data list is not null/ empty, then only try to update the adapter.
                List<Data> imagesDataList = imagesResponse.getDataList();
                if (null != imagesDataList && !imagesDataList.isEmpty()) {

                    // Adding the data to persisted data list under the view model.
                    mImageSearchingViewModel.addObjectsToRecyclerViewDataList(imagesDataList);

                    // Resetting the value to images live data, otherwise it will be keep positing the same data when the next UI controller is attached.
                    imagesLiveData.setValue(null);
                }
            }
        });

        // Listening to the network error events.
        mImageSearchingViewModel.getErrorObservable().observe(this, (Observer<Integer>) errorCode -> {

            mIsLoading = false;

            // Only consume the error network response if the error code is not CODE_DEFAULT.
            if (errorCode != CODE_DEFAULT) {

                // Obtaining the appropriate error message based on the error code.
                String errorMessage = mImageSearchingViewModel.getNetworkErrorMessages(errorCode);

                // Prompt the user about network error.
                Snackbar.make(mImageListRecyclerView, errorMessage, Snackbar.LENGTH_LONG).show();

                if(mImageListPageNo == 1) {
                    // hide the main progress bar, for the first page.
                    mMainProgressBarView.setVisibility(View.GONE);
                } else {
                    // hide the pagination view.
                    mImageRecyclerViewAdapter.removeLoadMoreViewFromList();
                }

                // Resetting the Error observable to CODE_DEFAULT, to avoid unwanted live data updates.
                mImageSearchingViewModel.getErrorObservable().setValue(CODE_DEFAULT);
            }
        });
    }

    final private DebouncedQueryTextListener mOnQueryTextListener = new DebouncedQueryTextListener() {
        @Override
        public void onQueryDebounce(String queryText) {
            if (null != queryText
                    && ! queryText.trim().isEmpty()
                    && ! mIsLoading) {

                // Resetting the page no, so that the request will be considered as fresh request rather than load more.
                mImageListPageNo = 1;

                // Making the network call to fetch the results from remote for the given query.
                clearExistingImageRecords();

                // Making the network request for the very first time.
                fetchImagesData(queryText);
            }
        }
    };

    final RecyclerView.OnScrollListener mImagesRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Log.e("SCROLL", "On Scroll");

            GridLayoutManager imagesGridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

            if (dy > 0 && null != imagesGridLayoutManager) {

                //total no. of items
                int totalItemCount = imagesGridLayoutManager.getItemCount();

                //last visible item position
                int lastVisibleItem = imagesGridLayoutManager.findLastCompletelyVisibleItemPosition();

                if (!mIsLoading) {
                    int visibleThreshold = 5;
                    if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {

                        // Scroll position reached at the end of the list, loading more events if available.
                        mIsLoading = true;
                        mImageListPageNo = mImageListPageNo + 1;

                        mImageRecyclerViewAdapter.addLoadMoreViewToList(null);

                        recyclerView.post(() -> mImageRecyclerViewAdapter.notifyItemInserted(mImageRecyclerViewAdapter.getItemCount() - 1));

                        // Making the paginated network request.
                        String queryText = mSearchView.getQuery().toString().trim();
                        fetchImagesData(queryText);
                    }
                }
            }
        }
    };

    /**
     * Method to make the network request with the given keyword.
     * @param queryString images data will be retrieved based on this query string.
     */
    private void fetchImagesData(final String queryString) {

        if (1 == mImageListPageNo){
            // Making the progress bar visible for the first page.
            mMainProgressBarView.setVisibility(View.VISIBLE);
        }

        // Proceeding to make the image search network request.
        mImageSearchingViewModel.searchImages(mImageListPageNo, queryString);
    }

    /**
     * Method to clear the grid recycler view records when there is a fresh search from search view.
     */
    private void clearExistingImageRecords() {
        if (null != mImageRecyclerViewAdapter && mImageRecyclerViewAdapter.getItemCount() > 0) {
            mImageRecyclerViewAdapter.clearImagesList();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Changing the grid recycler view columns at runtime when the configuration changes happens.
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mGridLayoutManager = new WrapContentGridLayoutManager(ImageSearchListActivity.this, PORTRAIT_RECYCLER_VIEW_COL_SPAN);
        } else {
            mGridLayoutManager = new WrapContentGridLayoutManager(ImageSearchListActivity.this, LANDSCAPE_RECYCLER_VIEW_COL_SPAN);
        }
        mImageListRecyclerView.setLayoutManager(mGridLayoutManager);
    }

    /**
     * Utility method to make transition from image listing activity to image details activity.
     * @param position data will be captured from adapter based on click.
     */
    private void navigateToDetailActivity (final int position) {
        // Moving to detail activity, if the image data is present.
        Data clickItemData = mImageRecyclerViewAdapter.getItemAtPosition(position);

        if (null != clickItemData) {
            String imageID = clickItemData.getId();
            String imageURL = clickItemData.getCover();
            String imageTitle = clickItemData.getTitle();

            Intent navigationIntent = new Intent(ImageSearchListActivity.this, ImageDetailActivity.class);
            navigationIntent.putExtra("imageTitle", imageTitle);
            navigationIntent.putExtra("imageID", imageID);
            navigationIntent.putExtra("imageURL", imageURL);
            startActivity(navigationIntent);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {

        // Persisting the search box text and network request pending flag.
        outPersistentState.putString("SEARCH_BOX_TEXT", mSearchView.getQuery().toString().trim());
        outPersistentState.putBoolean("IS_NETWORK_REQUEST_PENDING", mIsLoading);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {

        // Restoring the saved state after configuration changes.
        if (null != savedInstanceState) {

            String searchQuery = savedInstanceState.getString("SEARCH_BOX_TEXT");
            mIsLoading = savedInstanceState.getBoolean("IS_NETWORK_REQUEST_PENDING");

            // Adding the query text to the search views.
            mSearchView.setQuery(searchQuery, false);
        }

        super.onRestoreInstanceState(savedInstanceState);
    }
}
