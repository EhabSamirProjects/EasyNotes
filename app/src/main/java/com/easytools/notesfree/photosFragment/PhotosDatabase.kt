package com.easytools.notesfree.photosFragment

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = arrayOf(Photos::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PhotosDatabase : RoomDatabase(){
    abstract fun getDao(): PhotosDao

    companion object{
        @Volatile
        private var INSTANCE: PhotosDatabase? = null
        fun createDatabaseInstance(context: Context): PhotosDatabase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotosDatabase::class.java,
                    "photos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}
