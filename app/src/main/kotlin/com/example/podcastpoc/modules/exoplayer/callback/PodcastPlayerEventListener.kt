package com.example.podcastpoc.modules.exoplayer.callback

import android.app.Service
import android.os.Build
import android.widget.Toast
import com.example.podcastpoc.modules.exoplayer.PodcastService
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class PodcastPlayerEventListener(
    private val podcastService: PodcastService
): Player.Listener {
    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(podcastService, "Error", Toast.LENGTH_SHORT).show()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
    }
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
//            podcastService.stopForeground(false)
//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                podcastService.stopForeground(Service.STOP_FOREGROUND_DETACH)
            } else {
                podcastService.stopForeground(false)
            }
        }

    }
}