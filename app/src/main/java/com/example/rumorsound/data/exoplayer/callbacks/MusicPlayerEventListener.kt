package com.example.rumorsound.data.exoplayer.callbacks

import android.widget.Toast
import com.example.rumorsound.data.exoplayer.MusicService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

/** Слушатель реагирует на изменения в плеере. */
class MusicPlayerEventListener(
    private val musicService: MusicService,
) : Player.Listener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }

//    override fun onPlaybackStateChanged(playBackState: Int) {
//        super.onPlaybackStateChanged(playBackState)
//        if (playBackState == Player.STATE_READY) {
//            musicService.stopForeground(false)
//        }
//    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService, "Неизвестная ошибка", Toast.LENGTH_LONG).show()
    }
}