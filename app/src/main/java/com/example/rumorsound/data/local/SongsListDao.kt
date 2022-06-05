package com.example.rumorsound.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


/**
 * Data Access Object - объект по работе с базой данных.
 */
@Dao
interface SongsListDao {

    @Query("SELECT * FROM songsList")
    fun getSongsList(): LiveData<List<SongDBModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(songDBModel: SongDBModel)

    @Query("SELECT * FROM songsList WHERE mediaId = :songId LIMIT 1")
    suspend fun getSong(songId: Int): SongDBModel

}