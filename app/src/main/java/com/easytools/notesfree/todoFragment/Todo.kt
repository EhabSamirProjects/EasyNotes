package com.easytools.notesfree.todoFragment

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "todo_table")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    val title: String,
    val text: String,
    val isChecked: Boolean,
    var itemColor: String,
    var creationDate: String,
    var modifyingDate: String,
    var date: String
){
    @Ignore
    constructor(title: String, text: String, isChecked: Boolean, itemColor: String, creationDate: String, modifyingDate: String, date: String) : this(0, title, text, isChecked, itemColor, creationDate, modifyingDate, date)
}
