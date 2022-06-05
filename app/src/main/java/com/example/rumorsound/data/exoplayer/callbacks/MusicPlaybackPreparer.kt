package com.example.rumorsound.data.exoplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.rumorsound.data.exoplayer.FirebaseMusicSource
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector


/** Класс готовности, которому передаются действия по подготовке и воспроизведению.
 * Фактически ждет, когда всё загрузится из FB*/
class MusicPlaybackPreparer(
    private val musicSource: FirebaseMusicSource,
    private val playerPrepared: (MediaMetadataCompat?) -> Unit,
) : MediaSessionConnector.PlaybackPreparer {
    override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?) =
        false

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) = Unit

    //Когда данные загрузились, мы меняем состояние готовности плеера на готов.
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        musicSource.whenReady {
            val itemToPlay = musicSource.songsList.find { mediaId == it.description.mediaId }
            playerPrepared(itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit


}