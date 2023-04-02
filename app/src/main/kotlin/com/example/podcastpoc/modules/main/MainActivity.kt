package com.example.podcastpoc.modules.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.podcastpoc.core.helpers.ResultState
import com.example.podcastpoc.modules.exoplayer.isPlaying
import com.example.podcastpoc.R
import com.example.podcastpoc.core.helpers.collectWithLaunch
import com.example.podcastpoc.core.helpers.toPodcast
import com.example.podcastpoc.databinding.ActivityMainBinding
import com.example.podcastpoc.modules.main.adapters.SwipePodcastAdapter
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import com.example.podcastpoc.modules.main.ui.main.MainFragment
import com.example.podcastpoc.modules.main.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var swipePodcastAdapter: SwipePodcastAdapter

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by viewModels()

    private var curPlayingPodcast: PodcastDataModel.Podcast? = null

    private var playbackState: PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeToObservers()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        binding.viewpager.adapter = swipePodcastAdapter
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipePodcastAdapter.podcasts[position])
                } else {
                    curPlayingPodcast = swipePodcastAdapter.podcasts[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            curPlayingPodcast?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
    }

    private fun switchViewPagerToCurrentPodcast(podcast: PodcastDataModel.Podcast) {
        val newItemIndex = swipePodcastAdapter.podcasts.indexOf(podcast)
        if(newItemIndex != -1) {
            binding.viewpager.currentItem = newItemIndex
            curPlayingPodcast = podcast
        }
    }

    private fun subscribeToObservers() {
        // load data and setup adapter
        collectWithLaunch(mainViewModel.mediaItems) {
            when(it) {
                is ResultState.Error -> Log.d("tagg", "error")
                ResultState.Idle -> Unit
                ResultState.Loading -> Log.d("tagg", "Loading")
                is ResultState.Success -> {
                    it.data.let { podcast ->
                        swipePodcastAdapter.podcasts = podcast
                        if(podcast.isNotEmpty()) {
                            glide.load((curPlayingPodcast ?: podcast[0]).icon).into(binding.ivCurPodcastImage)
                        }
                        switchViewPagerToCurrentPodcast(curPlayingPodcast ?: return@collectWithLaunch)
                    }
                }
            }
        }

        // current playing podcast changed
        collectWithLaunch(mainViewModel.curPlayingPodcast) {
            if (it == null) return@collectWithLaunch

            curPlayingPodcast = it.toPodcast()
            glide.load(curPlayingPodcast?.icon).into(binding.ivCurPodcastImage)
            switchViewPagerToCurrentPodcast(curPlayingPodcast ?: return@collectWithLaunch)
        }


        // is podcast playing?
        collectWithLaunch(mainViewModel.playbackState) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        // is connected
        collectWithLaunch(mainViewModel.isConnected) {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is ResultState.Error -> Snackbar.make(
                        binding.root,
                        result.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }

        // network error
        collectWithLaunch(mainViewModel.networkError) {
            it.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is ResultState.Error -> Snackbar.make(
                        binding.root,
                        result.message,
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}