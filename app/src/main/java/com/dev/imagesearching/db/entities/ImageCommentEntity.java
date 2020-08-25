package com.dev.imagesearching.db.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "image_comment_table")
public class ImageCommentEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "imageID")
    private String mImageID;

    @NonNull
    @ColumnInfo(name = "imageComment")
    private String mCommentMessage;

    @NonNull
    public String getImageID() {
        return mImageID;
    }

    public void setImageID(@NonNull String imageID) {
        this.mImageID = imageID;
    }

    @NonNull
    public String getCommentMessage() {
        return mCommentMessage;
    }

    public void setCommentMessage(@NonNull String commentMessage) {
        this.mCommentMessage = commentMessage;
    }
}
