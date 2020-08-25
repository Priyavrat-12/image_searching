package com.dev.imagesearching.views.activities;

import android.os.Bundle;

import com.dev.imagesearching.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * (c) All rights reserved.
 * Base activity class, can hold the common functionality required across the activities.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void init () {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public abstract void setObservers ();
}
