package com.dev.imagesearching.db;

import android.content.Context;

import com.dev.imagesearching.db.dao.ImageCommentsDao;
import com.dev.imagesearching.db.entities.ImageCommentEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import static com.dev.imagesearching.db.DBConstants.CURRENT_DATABASE_VERSION;
import static com.dev.imagesearching.db.DBConstants.DATABASE_NAME;

/**
 * (c) All rights reserved.
 *
 * Singleton database helper class.
 */
@Database(entities = {ImageCommentEntity.class}, version = CURRENT_DATABASE_VERSION, exportSchema = false)
public abstract class ImagesSearchDatabaseHelper extends RoomDatabase {

    public abstract ImageCommentsDao imageCommentsDao ();

    private static volatile ImagesSearchDatabaseHelper sDatabaseInstance;

    public static ImagesSearchDatabaseHelper getsDatabaseInstance (final Context context) {
        if (null == sDatabaseInstance) {

            synchronized (ImagesSearchDatabaseHelper.class) {
                if (null == sDatabaseInstance) {
                    sDatabaseInstance = Room.databaseBuilder(context.getApplicationContext(),
                            ImagesSearchDatabaseHelper.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return sDatabaseInstance;
    }
}
