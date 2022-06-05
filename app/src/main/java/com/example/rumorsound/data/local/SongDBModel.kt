package com.example.rumorsound.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rumorsound.domain.entities.Song

/** Сущность песен для data слоя - database. */
@Entity(tableName = "songsList")
data class SongDBModel(
    @PrimaryKey(autoGenerate = true)
    val mediaId: Int = Song.UNDEFINED_ID,
    val title: String = "",
    val subtitle: String = "###",
    val url: String = "",
) {
    companion object {
        const val UNDEFINED_ID = 0
    }
}