package com.example.podcastpoc.modules.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.podcastpoc.modules.exoplayer.callback.PodcastNotificationListener
import com.example.podcastpoc.modules.exoplayer.callback.PodcastPlaybackPreparer
import com.example.podcastpoc.modules.exoplayer.callback.PodcastPlayerEventListener
import com.example.podcastpoc.core.helpers.getPendingIntentFlag
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class PodcastService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var defaultDataSource: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var podcastDataSource: PodcastDataSource

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private lateinit var podcastNotificationManager: PodcastNotificationManager
    var isForegroundService = false

    private var curPlayingPodcast: MediaMetadataCompat? = null

    private var isPlayerInitialized = false
    private lateinit var podcastEventListener: PodcastPlayerEventListener

    companion object {
        var currentPodcastDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            podcastDataSource.fetchMediaData()
        }
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, getPendingIntentFlag())
        }

        mediaSession = MediaSessionCompat(this, "PODCAST_SERVICE_TAG").apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        podcastNotificationManager = PodcastNotificationManager(this,
            mediaSession.sessionToken,
            PodcastNotificationListener(this)
        ) {
            // when the current podcast changes
            currentPodcastDuration = if (exoPlayer.duration > 0)
                exoPlayer.duration
            else 0

        }

        val podcastPlaybackRepeater = PodcastPlaybackPreparer(podcastDataSource) {
            curPlayingPodcast = it
            preparePodcast(
                podcastDataSource.podcasts,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(podcastPlaybackRepeater)
        mediaSessionConnector.setQueueNavigator(PodcastQueueNavigator() )
        mediaSessionConnector.setPlayer(exoPlayer)

        podcastEventListener = PodcastPlayerEventListener(this)
        exoPlayer.addListener(podcastEventListener)
        podcastNotificationManager.showNotification(exoPlayer)
    }

    // when media changes
    private inner class PodcastQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return podcastDataSource.podcasts[windowIndex].description
        }
    }

    private fun preparePodcast(
        podcasts: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val currentPodcastIndex = if(curPlayingPodcast == null) 0 else podcasts.indexOf(itemToPlay)
            exoPlayer.setMediaSource(podcastDataSource.asMediaSource(defaultDataSource))
            exoPlayer.prepare()
            exoPlayer.seekTo(currentPodcastIndex, 0L)
            exoPlayer.playWhenReady = playNow
        }

    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        //each album, playlist has their own id
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId) {
            MEDIA_ROOT_ID -> {
                 val resultsSet = podcastDataSource.whenReady { isInit ->
                     if (isInit) {
                         result.sendResult(podcastDataSource.asMediaItems())
                         if (!isPlayerInitialized && podcastDataSource.podcasts.isNotEmpty()) {
                             preparePodcast(podcastDataSource.podcasts, podcastDataSource.podcasts.first(),
                             false)
                             isPlayerInitialized = true
                         }
                     } else {
                         mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                         result.sendResult(null)
                     }
                 }

                if (!resultsSet) {
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
        exoPlayer.removeListener(podcastEventListener )
        exoPlayer.release()
    }
}