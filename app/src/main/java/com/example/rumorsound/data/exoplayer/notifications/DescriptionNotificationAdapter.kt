package com.example.rumorsound.data.exoplayer.notifications

import android.app.PendingIntent
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/** Адаптер уведомлений, который предоставляет данные песен, которые играют в данный момент.
 * An adapter to provide content assets of the media currently playing.*/
class DescriptionNotificationAdapter(
    private val mediaController: MediaControllerCompat,
    private val newSongCallback: () -> Unit
) : PlayerNotificationManager.MediaDescriptionAdapter {
    override fun getCurrentContentTitle(player: Player): CharSequence {
        newSongCallback()
        return mediaController.metadata.description.title.toString()
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        return mediaController.sessionActivity
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        return mediaController.metadata.description.subtitle.toString()
    }

    //TODO() Доработать отображение картинок в уведомлениях.
    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback,
    ): Bitmap? {
        return null
//        R.drawable.logo_okabe.toDrawable().toBitmap()
    }
}