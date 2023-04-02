package com.example.podcastpoc.modules.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.podcastpoc.modules.exoplayer.State.*
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import com.example.podcastpoc.modules.main.ui.main.imageUrl
import com.example.podcastpoc.modules.main.ui.main.imageUrl2
import com.example.podcastpoc.modules.main.ui.main.imageUrl3
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PodcastDataSource @Inject constructor() {

    var podcasts = emptyList<MediaMetadataCompat>()

    private fun getStaticData(): PodcastDataModel {
        val modelList = PodcastDataModel()
        val model1 = PodcastDataModel.Podcast(
            id = "1",
            title = "Podcast Title 1",
            subtitle = "subtitle 1",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
            totalListeners = 300,
            icon = imageUrl
        )

        val model2 = PodcastDataModel.Podcast(
            id = "2",
            title = "Podcast Title 2",
            subtitle = "subtitle 2",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            totalListeners = 100,
            icon = imageUrl2
        )

        val model3 = PodcastDataModel.Podcast(
            id = "3",
            title = "Podcast Title 3",
            subtitle = "subtitle 3",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            totalListeners = 9,
            icon = imageUrl3
        )

        modelList.add(model1)
        modelList.add(model2)
        modelList.add(model3)

        return modelList
    }


    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val allSongs = getStaticData()
        podcasts = allSongs.map { podcast ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, podcast.subtitle)
                .putString(METADATA_KEY_MEDIA_ID, podcast.id)
                .putString(METADATA_KEY_TITLE, podcast.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, podcast.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, podcast.icon)
                .putString(METADATA_KEY_MEDIA_URI, podcast.url)
                .putString(METADATA_KEY_ALBUM_ART_URI, podcast.icon)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, podcast.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, podcast.subtitle)
                .build()
        }
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        podcasts.forEach {podcast ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(podcast.getString(METADATA_KEY_MEDIA_URI)))

            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }


    fun asMediaItems() = podcasts.map { podcast ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(podcast.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(podcast.description.title)
            .setSubtitle(podcast.description.subtitle)
            .setMediaId(podcast.description.mediaId)
            .setIconUri(podcast.description.iconUri)
            .build()

        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if(value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}