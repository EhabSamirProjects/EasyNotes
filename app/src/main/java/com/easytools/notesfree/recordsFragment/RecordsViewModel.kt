package com.easytools.notesfree.recordsFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordsViewModel(application: Application) : AndroidViewModel(application) {
    val allRecords: LiveData<List<Records>>
    val repository: RecordsRepository

    init {
        val dao = RecordsDatabase.createDatabaseInstance(application).getDao()

        repository = RecordsRepository(dao)

        allRecords = repository.allRecords
    }

    fun insert(record: Records) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(record)
        }
    }

    fun delete(record: Records) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(record)
        }
    }

}
