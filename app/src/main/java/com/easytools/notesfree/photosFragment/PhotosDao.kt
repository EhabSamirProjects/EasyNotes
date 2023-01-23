package com.easytools.notesfree.photosFragment

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class PhotosDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(photo: Photos)

    @Delete
    abstract suspend fun delete(photo: Photos)

    @Query("SELECT * FROM photos_table ORDER BY id ASC")
    abstract fun getAllPhotos(): LiveData<List<Photos>>
}
