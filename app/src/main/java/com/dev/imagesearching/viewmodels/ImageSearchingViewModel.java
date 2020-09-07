package com.dev.imagesearching.viewmodels;

import android.app.Application;

import com.dev.imagesearching.models.Data;
import com.dev.imagesearching.models.ImagesResponse;
import com.dev.imagesearching.repositories.ImagesDataRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.dev.imagesearching.utils.NetworkConstants.CODE_400;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_400_DETAIL;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_401;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_401_DETAIL;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_404;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_404_DETAIL;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_500;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_500_DETAIL;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_503;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_503_DETAIL;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_INTERNET_NOT_AVAILABLE;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_INTERNET_NOT_AVAILABLE_DETAILS;
import static com.dev.imagesearching.utils.NetworkConstants.CODE_UNKNOWN;
import static com.dev.imagesearching.utils.NetworkConstants.UNKNOWN_FAILURE;

/**
 * (c) All rights reserved.
 *
 * Component of data presentation layer, responsible of the following operations.
 *
 * Communication with Data layer:
 * - Make the network call to retrieve the image search results.
 * - Providing the lifecycle aware live data instance, so that view/ UI controller can consume it.
 */
public class ImageSearchingViewModel extends AndroidViewModel {

    // For holding the freshly loaded images data responses.
    private MutableLiveData<ImagesResponse> mImagesResponseLiveData;

    // To notify about the network errors received.
    private MutableLiveData<Integer> mNetworkErrorCodeLiveData;

    // To persist the recycler view data items.
    private MutableLiveData<List<Data>> mImageRecyclerViewLiveData = new MutableLiveData<>();

    public ImageSearchingViewModel (@NonNull Application application) {
        super(application);

        init();
    }

    /**
     * To perform the necessary actions required when initializing the view model.
     */
    public void init () {

        ImagesDataRepository imageDataRepository = ImagesDataRepository.getInstance();

        // Initializing the observers related to network operations.
        mImagesResponseLiveData = imageDataRepository.getImagesResponseObservable();
        mNetworkErrorCodeLiveData = imageDataRepository.getNetworkFailureCodeObservable();

        // Initializing recycler view data items list.
        mImageRecyclerViewLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    /**
     * Method to get the appropriate error message based on the error code.
     * @param errorCode by using this error message will be calculated.
     * @return errorMessage for given error code.
     */
    public String getNetworkErrorMessages (int errorCode) {
        switch (errorCode) {

            case CODE_UNKNOWN:
                return UNKNOWN_FAILURE;

            case CODE_400:
                return CODE_400_DETAIL;

            case CODE_401:
                return CODE_401_DETAIL;

            case CODE_404:
                return CODE_404_DETAIL;

            case CODE_INTERNET_NOT_AVAILABLE:
                return CODE_INTERNET_NOT_AVAILABLE_DETAILS;

            case CODE_500:
                return CODE_500_DETAIL;

            case CODE_503:
                return CODE_503_DETAIL;

            default:
                return UNKNOWN_FAILURE;

        }
    }

    /**
     * Method to return the images data observable.
     * This will be consumed to display images in the grid recycler view.
     * @return mImagesResponseLiveData
     */
    public MutableLiveData<ImagesResponse> getImagesObservableData () {
        return mImagesResponseLiveData;
    }

    /**
     * Method to keep/ persist the recyclerview data list, which will intact w.r.t configuration changes.
     * @param newItems newly loaded items will be added to recyclerview data list.
     */
    public void addObjectsToRecyclerViewDataList (List<Data> newItems) {

        if (null != newItems && !newItems.isEmpty()) {
            List<Data> existingDataList = mImageRecyclerViewLiveData.getValue();

            // Adding the newly loaded data items to recycler view data list,
            // Since it uses the live data, so need not to notify the adapter for the dataset changed.
            if (null != existingDataList) {
                existingDataList.addAll(newItems);
                mImageRecyclerViewLiveData.setValue(existingDataList);
            }
        }
    }

    /**
     * To return the persisted data list used by recycler view.
     * @return this list is used by recycler view adapter to display the results.
     */
    public List<Data> getRecyclerViewDataList () {
        return mImageRecyclerViewLiveData.getValue();
    }

    /**
     * Method to rturn the error observable, the corresponding UI will display errors based on it.
     */
    public MutableLiveData <Integer> getErrorObservable () {
        return mNetworkErrorCodeLiveData;
    }

    /**
     * Making the API call via data layer to remote.
     * This API call will be fetching the images search results.
     * @param pageNo to perform the pagination, pageNo will be sent.
     * @param keyword for which the images results must be fetched.
     */
    public void searchImages (final int pageNo, final String keyword) {
        // Loading the image listing from remote.
        ImagesDataRepository imagesDataRepository = ImagesDataRepository.getInstance();
        imagesDataRepository.loadImagesList(pageNo, keyword);
    }
}
