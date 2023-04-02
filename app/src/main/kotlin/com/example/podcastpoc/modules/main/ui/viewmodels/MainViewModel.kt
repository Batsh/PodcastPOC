package com.example.podcastpoc.modules.main.ui.viewmodels

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podcastpoc.core.helpers.ResultState
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import com.example.podcastpoc.modules.exoplayer.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val podcastServiceConnection: PodcastServiceConnection
) : ViewModel() {
    private val _mediaItems = MutableStateFlow<ResultState<List<PodcastDataModel.Podcast>>>(
        ResultState.Idle)
    val mediaItems = _mediaItems.asStateFlow()

    val isConnected = podcastServiceConnection.isConnected
    val networkError = podcastServiceConnection.networkError
    val curPlayingPodcast = podcastServiceConnection.curPlayingPodcast
    val playbackState = podcastServiceConnection.playbackState

    private val _curPodcastDuration = MutableStateFlow<Long>(0)
    val curPodcastDuration = _curPodcastDuration.asStateFlow()

    private val _curPlayerPosition = MutableStateFlow<Long>(0)
    val curPlayerPosition = _curPlayerPosition.asStateFlow()

    init {
        viewModelScope.launch {
            _mediaItems.emit(ResultState.Loading)
            subscribe()
            updateCurrentPlayerPosition()
        }
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos) {
                    _curPlayerPosition.emit(pos ?: 0)
                    _curPodcastDuration.emit(PodcastService.currentPodcastDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

    private fun unsubscribe() {
        podcastServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    private fun subscribe() {
        podcastServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onError(parentId: String) {
                super.onError(parentId)
                Log.d("tagg", "error2")
            }
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                viewModelScope.launch {
                    val items = children.map {
                        PodcastDataModel.Podcast(
                            id = it.mediaId!!,
                            title = it.description.title.toString(),
                            subtitle = it.description.subtitle.toString(),
                            url = it.description.mediaUri.toString(),
                            icon = it.description.iconUri.toString(),
                            totalListeners = 12342
                        )
                    }
                    _mediaItems.emit(ResultState.Success(items))
                }
            }
        })
    }

    fun skipToNextSong() {
        podcastServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        podcastServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        podcastServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: PodcastDataModel.Podcast, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false

        if(isPrepared && mediaItem.id ==
            curPlayingPodcast.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) podcastServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> podcastServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            if (mediaItem.id.isNotEmpty()) {
                podcastServiceConnection.transportControls.playFromMediaId(mediaItem.id, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribe()
    }
}
