package com.easytools.notesfree.recordsFragment

class RecordsRepository(private val recordsDao: RecordsDao) {

    val allRecords = recordsDao.getAllRecords()

    suspend fun insert(record: Records){
        recordsDao.insert(record)
    }

    suspend fun delete(record: Records){
        recordsDao.delete(record)
    }

}
