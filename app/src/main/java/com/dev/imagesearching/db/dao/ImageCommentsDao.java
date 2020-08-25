package com.dev.imagesearching.db.dao;

import com.dev.imagesearching.db.entities.ImageCommentEntity;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * (c) All rights reserved.
 *
 * Set of SQL statements required to manipulate the data.
 */
@Dao
public interface ImageCommentsDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    long addComment (ImageCommentEntity entity);

    @Query("SELECT * FROM image_comment_table WHERE imageID = :imageID")
    ImageCommentEntity retrieveExistingComment (@NonNull final String imageID);
}
