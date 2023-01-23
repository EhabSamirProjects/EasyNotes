package com.easytools.notesfree.recordsFragment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Records::class), version = 1, exportSchema = false)
abstract class RecordsDatabase : RoomDatabase(){
    abstract fun getDao(): RecordsDao

    companion object{
        @Volatile
        private var INSTANCE: RecordsDatabase? = null
        fun createDatabaseInstance(context: Context): RecordsDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordsDatabase::class.java,
                    "records_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}
