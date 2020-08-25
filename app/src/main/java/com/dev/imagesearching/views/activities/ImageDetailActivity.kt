package com.dev.imagesearching.views.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dev.imagesearching.R
import com.dev.imagesearching.utils.AppUtils
import com.dev.imagesearching.utils.NetworkConstants.IMAGE_BASE_URL
import com.dev.imagesearching.viewmodels.ImageDetailsViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_image_detail.*

/**
 * (c) All rights reserved.
 *
 * Activity class to display the image details.
 * This will also facilitates the user to add comment on the image.
 */
class ImageDetailActivity : BaseActivity() {

    private lateinit var imageSearchViewModel : ImageDetailsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        // Taking the actions required on load of the screen.
        init()

        // Setting up the observers to listen various async/ data updates.
        setObservers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            // Handling the back press on toolbar back button.
            onBackPressed()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
    override fun init () {

        // Initializing the view model.
        imageSearchViewModel = ViewModelProvider(this).get(ImageDetailsViewModel::class.java)

        // Setting up the image title as toolbar title.
        val imageTitle:String? = intent.getStringExtra("imageTitle")
        if (null != imageTitle) {

            val toolbarObj: Toolbar = findViewById(R.id.toolbar)
            toolbarObj.setTitle(imageTitle)

            // Displaying the back button on toolbar.
            setSupportActionBar(toolbarObj)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }


        // Displaying the image into details screen image view, if the image url is available.
        var imageURL:String? = intent.getStringExtra("imageURL")
        if (null != imageURL) {

            imageURL = IMAGE_BASE_URL + imageURL + ".jpg"
            Glide.with(this)
                    .load(imageURL)
                    .fitCenter()
                    .into(imageViewDetails)
        }

        // Obtaining the imageID from intent.
        val imageID:String? = intent.getStringExtra("imageID")

        if (null == imageID) {

            // Notify the user that he won't be able to add the comment.
            Snackbar.make(btn_submit, "Internal error, unable to add comment to the image.", LENGTH_LONG).show()

        } else {

            // Try to fetch if there is any previous comment added to the given image.
            retrieveExistingComment(imageID)
        }

        // Listening the click events on submit button.
        btn_submit.setOnClickListener {

            // Hiding the keyboard.
            AppUtils.hideKeyboard(this, btn_submit)

            val commentTextInput: String = commentBoxET.text.toString().trim()

            if (TextUtils.isEmpty(commentTextInput)) {
                // Prompt the user that comment box can't be left empty while adding a comment.
                Snackbar.make(commentBoxET, "Comment is required.", Snackbar.LENGTH_SHORT).show()
            } else {
                // Proceed to add the comment.
                if (null != imageID)
                    addComment(imageID, commentTextInput)
            }
        }
    }

    /**
     * Adding the following observers.
     * 1. Observer for retrieving the previous comment if any.
     * 2. Observer for comment insertion result if any.
     */
    override fun setObservers() {

        // Adding the existing comment data observer.
        val existingCommentLiveData: MutableLiveData<String> = imageSearchViewModel.existingRecordObservable
        existingCommentLiveData.observe(this, Observer {

            if (null != existingCommentLiveData.value) {
                // If existing comment found in database then populate the comment box with it.
                val existingCommentString: String? = existingCommentLiveData.value
                commentBoxET.setText(existingCommentString)

                // Resetting the existing comment live data, once it is consumed.
                existingCommentLiveData.value = null
            }

        })

        // Adding comment insertion observer.
        val commentInsertLiveData: MutableLiveData<Long> = imageSearchViewModel.dbInsertionObservable
        commentInsertLiveData.observe(this, Observer {

            // Prompt the user about the comment adding results.
            val commentInsertResult: Long? = commentInsertLiveData.value
            if (null != commentInsertResult) {
                val resultMsg: String = if (0 >  commentInsertResult) "Error in adding comment" else "Comment added successfully"
                Snackbar.make(btn_submit, resultMsg, LENGTH_LONG).show()

                // Resetting the value of comment insert notifier live data, after consuming it.
                commentInsertLiveData.value = null
            }

        })
    }

    /**
     * Method to store the comment in database for a given image.
     */
    private fun addComment (imageID: String, commentData: String) {
        imageSearchViewModel.addCommentOnImage(imageID, commentData)
    }

    /**
     * Method to retrieve the existing comment on image if available.
     */
    private fun retrieveExistingComment (imageID: String) {
        imageSearchViewModel.getExistingCommentOnImage(imageID)
    }
}