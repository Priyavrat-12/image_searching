package com.dev.imagesearching.repositories;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class to fulfill the images data requests and other data operations,
 *
 * - Fetch the images data from remote as per the given search keyword.
 * - Retrieve the images data from local database,
 * - Add the comment to the given image.
 */
public class ImagesDataRepository {

    private static ImagesDataRepository sImagesDataRepository;

    // Executor service is used make the resource intensive operations on the worker threads.
    private ExecutorService mExecutorService;

    // Required private constructor to prevent the outside instantiation on the singleton repository class.
    private ImagesDataRepository () {

        // Obtaining the single worker thread for the network/ database operations.
        mExecutorService = Executors.newSingleThreadExecutor();
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
     * API method to push the comments into database for a give
     * @param imgID based on this the comment will be added for a particular image.
     * @param commentsData the comment text data.
     */
    public void addCommentsForImage (final String imgID, final String commentsData) {

        // Restart the executor service if it is stopped on shut-down.
        restartExecutorServiceIfStopped();

        // Async request to push the data into storage.
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                // Performing database operations to push the comments data for a given image.
            }
        });

    }

    private void restartExecutorServiceIfStopped () {
        if((null == mExecutorService) || (false != mExecutorService.isShutdown())) {
            mExecutorService = Executors.newSingleThreadScheduledExecutor();
        }
    }
}
