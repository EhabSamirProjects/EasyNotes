package com.easytools.notesfree.photosFragment

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "photos_table")
data class Photos(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bitmap: Bitmap
) {
    @Ignore
    constructor(bitmap: Bitmap) : this(0, bitmap)
}
