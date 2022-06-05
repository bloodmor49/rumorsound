package com.example.rumorsound.data.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.util.Log
import androidx.core.net.toUri
import com.example.rumorsound.other.State.*
import com.example.rumorsound.data.remote.MusicDatabase
import com.example.rumorsound.other.State
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Класс обработки получаемых песен из Firebase.
 * Берет загруженный список объектов Song из Firebase.
 * Далее в саспенд функции переданной в контекст IO обрабатываем состояние песни
 * и добавляем её в спиоск с использованием мета данных. Объект метаданных содержит всю информацию
 * о песне. После того, как все данные сформировались - меняем состояние на инициализировано.
 * и заливаем всё в список для проигрывателя.
 * */
class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase,
) {

    var songsList = emptyList<MediaMetadataCompat>()

    /** Получение данных из firebase.*/
    suspend fun fetchMediaData() = withContext(Dispatchers.Main) {
        state = STATE_INITIALIZING
        val songsListFromFB = musicDatabase.getMainCollection()
        Log.d("MusicLoad", "GETSONGFROMMUSICDB: $songsListFromFB")
        songsList = songsListFromFB.map {
            Builder()
                .putString(METADATA_KEY_ARTIST, it.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, it.mediaId.toString())
                .putString(METADATA_KEY_TITLE, it.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, it.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, it.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI, it.url)
                .putString(METADATA_KEY_ALBUM_ART_URI, it.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, it.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, it.subtitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

    /** Преобразование в MediaSource.  */
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songsList.forEach {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(it.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    /** Преобразование в mediaItem для ExoPlayer */
    fun asMediaItems() = songsList.map {
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setMediaId(it.description.mediaId)
            .setIconUri(it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    /** Состояние песни. Своего рода буферизация при работе с Firestore.
     * Фактически ждем, когда все скачается, а потом запускаем плеер и все такое.  */
    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    /**  */
    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }
}