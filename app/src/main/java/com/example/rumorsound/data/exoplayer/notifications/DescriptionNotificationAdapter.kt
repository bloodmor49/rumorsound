package com.example.rumorsound.data.exoplayer.notifications

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

/** Адаптер уведомлений, который предоставляет данные песен, которые играют в данный момент.
 * An adapter to provide content assets of the media currently playing.*/
class DescriptionNotificationAdapter(
    private val context : Context,
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

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback,
    ): Bitmap? {
        Glide.with(context).asBitmap()
            .load(mediaController.metadata.description.iconUri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
        return null
    }
}
