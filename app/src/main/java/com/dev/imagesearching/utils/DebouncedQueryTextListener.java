package com.dev.imagesearching.utils;
import android.os.SystemClock;
import android.util.Log;

import androidx.appcompat.widget.SearchView;

/**
 * (c) All rights reserved.
 *
 * To make the a debounce in the search field for a given period of time.
 */
public abstract class DebouncedQueryTextListener implements SearchView.OnQueryTextListener {

    private static final String TAG = "DebouncedOnQueryTextLis";
    private static final long THRESHOLD_MILLIS = 250L;
    private long lastClickMillis;
    private int calls = 0;

    protected DebouncedQueryTextListener() {

    }

    public abstract void onQueryDebounce(String text);

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickMillis > THRESHOLD_MILLIS) {
            calls += 1;
            onQueryDebounce(newText);
            Log.d(TAG, "onQueryTextChange: " + calls);
        }
        lastClickMillis = now;
        return true;
    }

}
