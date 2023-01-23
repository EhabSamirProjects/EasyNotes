package com.easytools.notesfree.todoFragment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Todo::class), version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase(){
    abstract fun getDao(): TodoDao

    companion object{
        @Volatile
        private var INSTANCE: TodoDatabase?=null
        fun createDatabaseInstance(context: Context): TodoDatabase {
            return INSTANCE ?: synchronized(this){
                val instance= Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                ).build()
                INSTANCE =instance
                instance
            }
        }
    }

}
