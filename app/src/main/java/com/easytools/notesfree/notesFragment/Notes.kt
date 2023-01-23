package com.easytools.notesfree.notesFragment

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table")
data class Notes(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val title: String,
    val text: String,
    var itemColor: String,
    var creationDate: String,
    var modifyingDate: String,
    var date: String
){
    @Ignore
    constructor(title: String, text: String, itemColor: String, creationDate: String, modifyingDate: String, date: String) : this(0, title, text, itemColor, creationDate, modifyingDate, date)
}
