package com.easytools.notesfree.photosFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotosViewModel(application: Application) : AndroidViewModel(application) {
    val allPhotos: LiveData<List<Photos>>
    val repository: PhotosRepository

    init {
        val dao = PhotosDatabase.createDatabaseInstance(application).getDao()

        repository = PhotosRepository(dao)

        allPhotos = repository.allPhotos
    }

    fun insert(photo: Photos) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(photo)
        }
    }

    fun delete(photo: Photos) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(photo)
        }
    }

}
