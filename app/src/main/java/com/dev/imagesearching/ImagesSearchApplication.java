package com.dev.imagesearching;

import android.app.Application;

/**
 * (c) All rights reserved.
 *
 * Application class, can keep the necessary initializations.
 */
public class ImagesSearchApplication extends Application {

    private static ImagesSearchApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    public static ImagesSearchApplication getApplicationInstance() {
        return mApplication;
    }
}
