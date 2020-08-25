package com.dev.imagesearching.viewmodels;

import android.app.Application;

import com.dev.imagesearching.repositories.ImagesDataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * (c) All rights reserved.
 *
 * Component of data presentation layer, responsible of the following operations.
 *
 * Communication with Data layer:
 * - Retrieve the previously added comment from database.
 * - To make the database insertion call for adding the comment on image.
 *
 * - Providing the lifecycle aware live data instance, so that view/ UI controller can consume it.
 */
public class ImageDetailsViewModel extends AndroidViewModel {

    private MutableLiveData<Long> mDBInsertResultLiveData;
    private MutableLiveData<String> mExistingRecordLiveData;

    public ImageDetailsViewModel(@NonNull Application application) {
        super(application);

        init();
    }

    /**
     * To perform the necessary actions required when initializing the view model.
     */
    public void init () {

        ImagesDataRepository imageDataRepository = ImagesDataRepository.getInstance();

        // Initializing the observers related to database operations.
        mDBInsertResultLiveData = imageDataRepository.getInsertRecordResultObservable();
        mExistingRecordLiveData = imageDataRepository.getExistingRecordObservable();

    }

    /**
     * Getter for live data used to observe the comment addition results.
     * @return mDBInsertResultLiveData
     */
    public MutableLiveData <Long> getDBInsertionObservable () {
        return mDBInsertResultLiveData;
    }

    /**
     * Getter for live data used to observe the existing comment retrieval results.
     * @return mExistingRecordLiveData
     */
    public MutableLiveData <String> getExistingRecordObservable () {
        return mExistingRecordLiveData;
    }

    /**
     * Method to add/ update comment for a given image.
     * @param imageID based on this comment will be stored/ updated to specific image.
     * @param commentData to be stored or updated based on imageID.
     */
    public void addCommentOnImage (@NonNull final String imageID, @NonNull String commentData) {
        ImagesDataRepository imagesDataRepository = ImagesDataRepository.getInstance();
        imagesDataRepository.addCommentForImage(getApplication().getApplicationContext(), imageID, commentData);
    }

    /**
     * Method to retrieve the existing comment added to the
     * @param imageID based on this, comment data will be retrieved if exist.
     */
    public void getExistingCommentOnImage (@NonNull final String imageID) {
        ImagesDataRepository imagesDataRepository = ImagesDataRepository.getInstance();
        imagesDataRepository.getPreviousCommentForImage(getApplication().getApplicationContext(), imageID);
    }
}
