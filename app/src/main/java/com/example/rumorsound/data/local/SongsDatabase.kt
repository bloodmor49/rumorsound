package com.example.rumorsound.data.local

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * БД песен с использованием Room. Синглтон (единая бд).
 */
@Database(entities = [SongDBModel::class], version = 1, exportSchema = false)
abstract class SongsDatabase : RoomDatabase() {

    abstract fun songsListDao(): SongsListDao

    companion object {

        private var INSTANCE: SongsDatabase? = null
        private val LOCK = Any()
        private const val DB_NAME = "songsList"

        fun getInstance(application: Application): SongsDatabase {
            INSTANCE?.let { return it }
            synchronized(LOCK) {
                INSTANCE?.let { return it }
                val db = Room.databaseBuilder(
                    application,
                    SongsDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = db
                return db
            }
        }
    }
}