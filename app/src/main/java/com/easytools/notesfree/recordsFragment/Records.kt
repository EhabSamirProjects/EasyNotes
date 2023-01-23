package com.easytools.notesfree.recordsFragment

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "records_table")
data class Records(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filename: String,
    val filePath: String,
    val duration: String,
    val itemColor: String,
    val date: String
) {
    @Ignore
    constructor(filename: String, filePath: String, duration: String, itemColor: String, date: String) : this(0, filename, filePath, duration, itemColor, date)
}
