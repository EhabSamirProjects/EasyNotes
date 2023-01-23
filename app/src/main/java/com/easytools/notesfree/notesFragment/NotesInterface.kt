package com.easytools.notesfree.notesFragment

interface NotesInterface{

    fun deleteNote(selectedNotes: List<Notes>)

    fun updateNote(note: Notes)

    fun showContextualActionMode(note: Notes)

}
