package com.example.podcastpoc.modules.exoplayer.callback

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.podcastpoc.modules.exoplayer.NOTIFICATION_ID
import com.example.podcastpoc.modules.exoplayer.PodcastService
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class PodcastNotificationListener(
    private val podcastService: PodcastService
): PlayerNotificationManager.NotificationListener {
    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        podcastService.apply {
            stopForeground(true)

//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                 stopForeground(STOP_FOREGROUND_REMOVE)
//             } else {
//                 stopForeground(true)
//             }

            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        podcastService.apply {
            if(ongoing && isForegroundService) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(applicationContext, this::class.java)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForegroundService = true
            }
        }
    }
}