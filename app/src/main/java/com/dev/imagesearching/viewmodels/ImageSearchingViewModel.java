package com.dev.imagesearching.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class ImageSearchingViewModel extends AndroidViewModel {

    public ImageSearchingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Object> retrieveImagesData () {
        return null;
    }
}
