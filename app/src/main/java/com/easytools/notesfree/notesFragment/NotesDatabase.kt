package com.easytools.notesfree.notesFragment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Notes::class), version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase(){
    abstract fun getDao(): NotesDao
    companion object{
        @Volatile
        private var INSTANCE: NotesDatabase?=null
        fun createDatabaseInstance(context: Context): NotesDatabase {
            return INSTANCE ?: synchronized(this){
                val instance= Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                    "notes_database"
                ).build()
                INSTANCE =instance
                instance
            }
        }
    }
}