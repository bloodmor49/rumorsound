package com.example.rumorsound.data.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat

import com.example.rumorsound.data.exoplayer.notifications.MusicNotificationManager
import com.example.rumorsound.data.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.rumorsound.data.exoplayer.callbacks.MusicPlayerEventListener
import com.example.rumorsound.data.exoplayer.notifications.MusicPlayerNotificationListener
import com.example.rumorsound.data.remote.FirebaseMusicSource
import com.example.rumorsound.other.Constants.MEDIA_ROOT_ID
import com.example.rumorsound.other.Constants.NETWORK_ERROR
import com.example.rumorsound.other.Constants.SERVICE_TAG
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/** Фоновые сервис плеера. Наследуется от MediaBrowserServiceCompat,
 * в связи с тем, что фактически приложение является файловой системой с папками, подпапками и т.д.
 * Компонент андроида, поэтому для инджекта всегда нужно помечать аннотацией AndroidEntryPoint.
 * Связущее звено для уведомлений, работы с бд MediaSessionCompat и MediaSessionConnector.
 *
 */
@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    //    Для доступа к разным способам подключения по URI
    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    //    Сам плеер
    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicSource: FirebaseMusicSource

    //    Работа с уведомлениями
    private lateinit var musicNotificationManager: MusicNotificationManager

    //    Фоновый поток для сервиса
    private var serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    //    Доступ к кнопкам управления проигрывателем
    private lateinit var mediaSession: MediaSessionCompat

    //    Подключение mediaSession к приложению
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var isPlayerInitialized = false

    var isForegroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    private lateinit var musicPlayerEventListener :  MusicPlayerEventListener

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            musicSource.fetchMediaData()
        }

        setUpMediaSession()
        setUpNotificationManager()
        setUpMediaSessionConnector()

        musicPlayerEventListener = MusicPlayerEventListener(this)
//        добавляем слушателя событий
        exoPlayer.addListener(musicPlayerEventListener)
//        показываем уведомление
        musicNotificationManager.showNotification(exoPlayer)

    }

    private fun setUpMediaSessionConnector() {
        val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) {
            currentPlayingSong = it
            preparePlayer(
                musicSource.songsList,
                it,
                true
            )
        }
        //        подключаем нашу активну сессию к управлению за счет кнопок и тд
        mediaSessionConnector = MediaSessionConnector(mediaSession)

        //        устанавливаем что делать во время приготовления песен
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)

        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())

        //        устанавливаем управление за счёт плеера
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    private fun setUpNotificationManager() {
        // к уведомлению присоединяем слушателей и токен.
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            currentSongDuration = exoPlayer.duration
        }


    }

    private fun setUpMediaSession() {

        // Позволяем запускать плеер с использованием уведомления
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }
        // Формируем новую медиа сессию воспроизведения музыки.
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        //        формируем токен, чтобы пользователь мог работать с ним
        sessionToken = mediaSession.sessionToken
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return musicSource.songsList[windowIndex].description
        }
    }

    /** Подготовка плеера к воспроизведению. Берем наш индекс песни, после чего запускаем.  */
    private fun preparePlayer(
        listOfSongs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean,
    ) {
        val currentSongIndex =
            if (currentPlayingSong == null) 0 else listOfSongs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialized && musicSource.songsList.isNotEmpty()) {
                            preparePlayer(musicSource.songsList, musicSource.songsList[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    companion object {
        var currentSongDuration = 0L
            private set
    }
}
