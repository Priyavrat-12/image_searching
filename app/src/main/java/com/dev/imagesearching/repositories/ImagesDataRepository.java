package com.dev.imagesearching.repositories;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.dev.imagesearching.ImagesSearchApplication;
import com.dev.imagesearching.api.ApiClient;
import com.dev.imagesearching.api.RemoteApiInterface;
import com.dev.imagesearching.db.ImagesSearchDatabaseHelper;
import com.dev.imagesearching.db.dao.ImageCommentsDao;
import com.dev.imagesearching.db.entities.ImageCommentEntity;
import com.dev.imagesearching.models.ImagesResponse;
import com.dev.imagesearching.utils.AppUtils;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.dev.imagesearching.utils.NetworkConstants.AUTHORIZATION_HEADER_KEY;
import static com.dev.imagesearching.utils.NetworkConstants.AUTHORIZATION_HEADER_VALUE;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_INTERNET_NOT_AVAILABLE;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_UNKNOWN;

/**
 * Repository class to fulfill the images data requests and other data operations,
 *
 * - Fetch the images data from remote as per the given search keyword.
 * - Retrieve the images data from local database,
 * - Add the comment to the given image.
 */
public class ImagesDataRepository {

    private static ImagesDataRepository sImagesDataRepository;

    private static final String TAG = ImagesDataRepository.class.getSimpleName();

    // Executor service is used make the resource intensive operations on the worker threads.
    private ExecutorService mDBTransactionExecutorService;

    private MutableLiveData<ImagesResponse> mImagesResponseLiveData;
    private MutableLiveData<Integer> mErrorCodeLiveData;
    private MutableLiveData<Long> mDBInsertResultLiveData;
    private MutableLiveData<String> mExistingRecordLiveData;

    // Required private constructor to prevent the outside instantiation on the singleton repository class.
    private ImagesDataRepository() {

        // Initializing the various required live data.
        mImagesResponseLiveData = new MutableLiveData<>();
        mErrorCodeLiveData = new MutableLiveData<>();

        mDBInsertResultLiveData = new MutableLiveData<>();
        mExistingRecordLiveData = new MutableLiveData<>();

        // Obtaining the single worker thread for the network/ database operations.
        mDBTransactionExecutorService = Executors.newSingleThreadExecutor();
    }

    public static ImagesDataRepository getInstance() {

        // Using the lazy loading with double lock, to avoid multiple instance creation from different threads.
        if (null == sImagesDataRepository) {

            synchronized (ImagesDataRepository.class) {

                if (null == sImagesDataRepository) {
                    sImagesDataRepository = new ImagesDataRepository();
                }
            }
        }

        return sImagesDataRepository;
    }

    /**
     * API method to push the comments into database for a given image.
     *
     * @param context required to access the database.
     * @param imgID        based on this the comment will be added for a particular image.
     * @param commentsData the comment text data.
     */
    public void addCommentForImage(@NonNull final Context context, @NonNull final String imgID, final String commentsData) {

        // Return as the context is not available, which is required to access the database.
        if (null == context) {
            Log.e(TAG, "Context is required to access the DB.");
            return;
        }

        // Return as image Id or the comment data is provided as null.
        if (null == imgID || null == commentsData) {
            Log.e(TAG, "Proper image ID and data required to store comment.");
            return;
        }

        // Restart the executor service if it is stopped on shut-down.
        restartExecutorServiceIfStopped();

        // Async request to push the data into storage.
        mDBTransactionExecutorService.execute(() -> {

            // Performing database operations to push the comments data for a given image.
            ImagesSearchDatabaseHelper databaseHelper = ImagesSearchDatabaseHelper.getsDatabaseInstance(context);
            ImageCommentsDao imageCommentsDao = databaseHelper.imageCommentsDao();

            // Return as the Dao object is not available.
            if (null == imageCommentsDao) {
                return;
            }

            // Making the database operation to add comment for a given image.
            // If the comment already exist for the given image, then it will be replaced.
            ImageCommentEntity entity = new ImageCommentEntity();
            entity.setCommentMessage(commentsData);
            entity.setImageID(imgID);

            long insertionResult = imageCommentsDao.addComment(entity);

            // Notifying the observer about the comment insertion results.
            mDBInsertResultLiveData.postValue(insertionResult);

        });
    }

    /**
     * API method to retrieve the previously added comment for given image if any.
     */
    public void getPreviousCommentForImage (@NonNull Context context, @NonNull final String imageID) {

        // Return as the context is not available, which is required to access the database.
        if (null == context) {
            Log.e(TAG, "Context is required to access the DB.");
            return;
        }

        if (TextUtils.isEmpty(imageID)) {
            Log.e(TAG, "Image ID is required to retrieve the existing comment.");
            return;
        }

        // Restart the executor service if it is stopped on shut-down.
        restartExecutorServiceIfStopped();

        // Retrieving the existing comment on a worker thread.
        mDBTransactionExecutorService.execute(() -> {

            // Performing database operations to push the comments data for a given image.
            ImagesSearchDatabaseHelper databaseHelper = ImagesSearchDatabaseHelper.getsDatabaseInstance(context);
            ImageCommentsDao imageCommentsDao = databaseHelper.imageCommentsDao();

            // Return as the Dao object is not available.
            if (null == imageCommentsDao) {
                return;
            }

            // Making the database operation to add comment for a given image.
            // If the comment already exist for the given image, then it will be replaced.
            ImageCommentEntity dataRetrievalResult = imageCommentsDao.retrieveExistingComment(imageID);
            if (null != dataRetrievalResult) {
                // Notifying the observer about the comment retrieval results.
                mExistingRecordLiveData.postValue(dataRetrievalResult.getCommentMessage());
            }
        });
    }

    /**
     * To initiate the network request which will retrieve the images data list.
     *
     * @param pageNo  required for paging, it must be greater than zero.
     * @param keyword For the given keyword the results would be fetched.
     */
    public void loadImagesList(final int pageNo, final String keyword) {

        // Return if the page number is not proper or the keyword is null.
        if (pageNo < 0 || null == keyword) {
            return;
        }

        // Check if the device is connected to network and internet connection is active.

        if (AppUtils.isNetworkAvailable(ImagesSearchApplication.getApplicationInstance())) {
            ApiClient apiClient = ApiClient.getInstance();

            RemoteApiInterface remoteApiInterface = apiClient.getApiInterface();

            if (null != remoteApiInterface) {

                // Preparing the header map for network request.
                Map<String, String> headerMap = prepareImageSearchAPIHeaderMap();

                remoteApiInterface.getImagesList(headerMap, pageNo, keyword).enqueue(new Callback<ImagesResponse>() {
                    @Override
                    public void onResponse(Call<ImagesResponse> call, Response<ImagesResponse> response) {

                        // Notifying the view to stop the indicator as response is received.
                        //mIsLoading.postValue(false);
                        try {
                            if (response.isSuccessful()) {
                                ImagesResponse imgResponse = response.body();

                                mImagesResponseLiveData.postValue(imgResponse);

                            } else {
                                // Sending the proper error Http response code.
                                mErrorCodeLiveData.postValue(response.code());
                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            mErrorCodeLiveData.postValue(CODE_UNKNOWN);
                        }
                    }

                    @Override
                    public void onFailure(Call<ImagesResponse> call, Throwable t) {
                        t.printStackTrace();
                        // Throwing the retrofit error.
                        mErrorCodeLiveData.postValue(CODE_UNKNOWN);
                    }
                });
            }
        } else {
            // Internet is not connected.
            mErrorCodeLiveData.postValue(CODE_INTERNET_NOT_AVAILABLE);
        }
    }

    /**
     * To prepare required headers for image search API.
     * @return headerMap, required in network request.
     */
    private Map<String, String> prepareImageSearchAPIHeaderMap () {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(AUTHORIZATION_HEADER_KEY, AUTHORIZATION_HEADER_VALUE);

        return headerMap;
    }

    /**
     * To restart the executor service is stopped or shutdown.
     */
    private void restartExecutorServiceIfStopped () {
        if (null == mDBTransactionExecutorService || mDBTransactionExecutorService.isShutdown()) {
            mDBTransactionExecutorService = Executors.newSingleThreadScheduledExecutor();
        }
    }

    /**
     * Will be observed to track the comment insert operation.
     */
    public MutableLiveData<Long> getInsertRecordResultObservable () {
        return mDBInsertResultLiveData;
    }

    /**
     * To keep track if the previously added comment found or not.
     */
    public MutableLiveData <String> getExistingRecordObservable() {
        return mExistingRecordLiveData;
    }

    /**
     * To get the images data observable.
     */
    public MutableLiveData<ImagesResponse> getImagesResponseObservable () {
        return mImagesResponseLiveData;
    }

    /**
     * To identify the cause of network failure.
     */
    public MutableLiveData<Integer> getNetworkFailureCodeObservable () {
        return mErrorCodeLiveData;
    }
}
