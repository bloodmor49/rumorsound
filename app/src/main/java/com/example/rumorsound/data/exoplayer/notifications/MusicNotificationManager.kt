package com.example.rumorsound.data.exoplayer.notifications

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.example.rumorsound.R
import com.example.rumorsound.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.rumorsound.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/** Работа с уведомлениями плеера - менеджер по их созданию.
 * Для его работы нужно:
 * 1) Контекст откуда это вызывается.
 * 2) Токен активной сессии, чтобы менеджер знал с чем работать.
 * 3) Слушатель событий, чтобы  знать что делать при остановке или запуске уведомления (сервис пуск)
 * 4) Контроллер - нужен для того, чтобы можно было управлять кнопками и тд уведомления данной сессии.
 * 5) Адаптер - для того, чтобы адаптировать информацию для плеера уведомления. */
class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: () -> Unit,
) {

    // Создаем уведомление - даем контекст, канал и тд.
    private val notificationManager: PlayerNotificationManager

    init {
        //Создаем уведомление музыки.
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager
            .Builder(context, NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID)
            .setChannelNameResourceId(R.string.song_id)
            .setChannelDescriptionResourceId(R.string.song_name)
            .setNotificationListener(notificationListener)
            .setMediaDescriptionAdapter(
                DescriptionNotificationAdapter(context, mediaController, newSongCallback ))
            .build()
            .apply {
                setSmallIcon(R.drawable.logo_okabe)
                setMediaSessionToken(sessionToken)
            }
    }

    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }


}