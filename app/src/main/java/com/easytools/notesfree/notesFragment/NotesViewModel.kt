package com.easytools.notesfree.notesFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.easytools.notesfree.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    val allNotes: LiveData<List<Notes>>
    val repository: NotesRepository

    init {
        val dao = NotesDatabase.createDatabaseInstance(application).getDao()

        repository = NotesRepository(dao)

        allNotes = if(MyApplication.sortItemsBy == "m") {
            repository.allNotesOne
        }
        else repository.allNotesTwo

    }

   fun insert(note: Notes){
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(note)
        }
    }

    fun delete(note: Notes){
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(note)
        }
    }

    fun updateNote(note: Notes) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }
}
