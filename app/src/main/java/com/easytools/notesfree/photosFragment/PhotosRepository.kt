package com.easytools.notesfree.photosFragment

class PhotosRepository(private val photosDao: PhotosDao) {

    val allPhotos = photosDao.getAllPhotos()

    suspend fun insert(photo: Photos){
        photosDao.insert(photo)
    }

    suspend fun delete(photo: Photos){
        photosDao.delete(photo)
    }

}
