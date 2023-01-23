package com.easytools.notesfree.recordsFragment

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class RecordsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(record: Records)

    @Delete
    abstract suspend fun delete(record: Records)

    @Query("SELECT * FROM records_table ORDER BY id DESC")
    abstract fun getAllRecords(): LiveData<List<Records>>
}
