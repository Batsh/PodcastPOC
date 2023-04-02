package com.example.podcastpoc.modules.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.podcastpoc.core.helpers.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PodcastServiceConnection(
    context: Context,
) {
    private val _isConnected = MutableStateFlow<Event<ResultState<Boolean>>>(Event(ResultState.Idle))
    val isConnected: StateFlow<Event<ResultState<Boolean>>> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow<Event<ResultState<Boolean>>>(Event(ResultState.Idle))
    val networkError: StateFlow<Event<ResultState<Boolean>>> = _networkError.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState: StateFlow<PlaybackStateCompat?> = _playbackState.asStateFlow()

    private val _curPlayingPodcast = MutableStateFlow<MediaMetadataCompat?>(null)
    val curPlayingPodcast: StateFlow<MediaMetadataCompat?> = _curPlayingPodcast.asStateFlow()

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            PodcastService::class.java
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaContollerCallback())
            }
            CoroutineScope(Dispatchers.IO).launch {
                _isConnected.emit(Event(ResultState.Success(true)))
            }
        }

        override fun onConnectionSuspended() {
            CoroutineScope(Dispatchers.IO).launch {
                _isConnected.emit(
                    Event(
                        ResultState.Error(
                            "The connection was suspended", 100
                        )
                    )
                )
            }
        }

        override fun onConnectionFailed() {
            CoroutineScope(Dispatchers.IO).launch {
                _isConnected.emit(
                    Event(
                        ResultState.Error(
                            "Couldn't connect to media browser", 100
                        )
                    )
                )
            }
        }
    }

    private inner class MediaContollerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            CoroutineScope(Dispatchers.IO).launch {
                _playbackState.emit(state)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            CoroutineScope(Dispatchers.IO).launch {
                _curPlayingPodcast.emit(metadata)
            }
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            CoroutineScope(Dispatchers.IO).launch {
                when (event) {
                    NETWORK_ERROR -> _networkError.emit(
                        Event(
                            ResultState.Error(
                                "Couldn't connect to the server. Please check your internet connection.",
                                null
                            )
                        )
                    )
                }
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}