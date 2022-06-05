package com.example.rumorsound.data.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.rumorsound.domain.entities.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            mediaId = it.mediaId?.toInt() ?: Song.UNDEFINED_ID,
            title = it.title.toString(),
            url = it.mediaUri.toString(),
            subtitle = it.subtitle.toString()
        )
    }
}