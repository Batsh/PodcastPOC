package com.example.podcastpoc.modules.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.podcastpoc.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener

const val NOTIFICATION_ID = 1
const val NOTIFICATION_CHANNEL_ID = "podcast"
const val MEDIA_ROOT_ID = "root_id"
const val NETWORK_ERROR = "NETWORK_ERROR"
const val UPDATE_PLAYER_POSITION_INTERVAL = 100L


class PodcastNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: NotificationListener,
    private val newPodcastCallback: () -> Unit
) {

    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController =  MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID, NOTIFICATION_CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setSmallIcon(R.drawable.ic_music)
                setMediaSessionToken(sessionToken)
            }
    }

    fun showNotification(player: Player) {
         notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
    ): PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newPodcastCallback()
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
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap()
                .load(mediaController.metadata.description.iconUri)
                .into(object : CustomTarget<Bitmap>(){
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
}