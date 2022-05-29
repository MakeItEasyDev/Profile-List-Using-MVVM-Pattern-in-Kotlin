package com.kotlin.task.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kotlin.task.database.dao.ProfileDao
import com.kotlin.task.database.entity.ProfileList

@Database(entities = [ProfileList::class], version = 1)
abstract class ProfileDatabase: RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: ProfileDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProfileDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }

        }
    }
}