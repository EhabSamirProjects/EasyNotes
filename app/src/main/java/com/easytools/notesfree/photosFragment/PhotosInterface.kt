package com.easytools.notesfree.photosFragment

import android.graphics.Bitmap

interface PhotosInterface{

    fun deletePhoto(selectedPhotos: List<Photos>)

    fun fullScreen(bitmap: Bitmap)

    fun showContextualActionMode(photo: Photos)

}
