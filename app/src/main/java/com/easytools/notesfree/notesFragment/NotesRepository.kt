package com.easytools.notesfree.notesFragment

class NotesRepository(private val notesDao: NotesDao) {

    val allNotesOne = notesDao.getAllNotes(true)
    val allNotesTwo = notesDao.getAllNotes(false)

    suspend fun insert(note: Notes){
        notesDao.insert(note)
    }

    suspend fun delete(note: Notes){
        notesDao.delete(note)
    }

    suspend fun updateNote(note: Notes){
        notesDao.updateNote(note)
    }
}
