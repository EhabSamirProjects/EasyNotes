package com.easytools.notesfree.notesFragment

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class NotesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(note: Notes)

    @Delete
    abstract suspend fun delete(note: Notes)

    @Query("SELECT * FROM notes_table ORDER BY " +
            "CASE WHEN :byModifying = 1 THEN date END DESC, " +
            "CASE WHEN :byModifying = 0 THEN id END DESC")
    abstract fun getAllNotes(byModifying: Boolean): LiveData<List<Notes>>

    @Update
    abstract fun updateNote(note: Notes)
}
