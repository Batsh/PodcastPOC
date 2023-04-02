package com.example.podcastpoc.modules.main.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.podcastpoc.core.helpers.ResultState
import com.example.podcastpoc.modules.exoplayer.isPlaying
import com.example.podcastpoc.R
import com.example.podcastpoc.core.helpers.collectWithLaunch
import com.example.podcastpoc.core.helpers.setImage
import com.example.podcastpoc.core.helpers.toPodcast
import com.example.podcastpoc.databinding.FragmentMainBinding
import com.example.podcastpoc.modules.main.adapters.PodcastAdapter
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import com.example.podcastpoc.modules.main.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


const val imageUrl = "data:image/jpeg;base64,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"
const val imageUrl2 = "https://static.vecteezy.com/system/resources/previews/002/157/610/original/illustrations-concept-design-podcast-channel-free-vector.jpg"
const val imageUrl3 = "https://media.wired.com/photos/620eb0f39266d5d11c07b3c5/master/pass/Gear-Podcast-Gear-1327244548.jpg"

@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    companion object {
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var podcastAdapter: PodcastAdapter

    private val viewModel: MainViewModel by activityViewModels()
    private var shouldUpdateSeekbar = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleState()
        binding.rv.adapter = podcastAdapter

        binding.button.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            transaction.replace(R.id.container, SecondFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        podcastAdapter.setOnItemClickListener {
            viewModel.playOrToggleSong(it, true)
            updateUI(it)
        }

        binding.forwardIv.setOnClickListener {
            viewModel.skipToNextSong()
        }

        binding.prevIv.setOnClickListener {
            viewModel.skipToPreviousSong()
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })
    }

    private fun updateUI(podcast: PodcastDataModel.Podcast) {
        binding.titleTv.text = podcast.title
        binding.iconIv.setImage(podcast.icon)
        binding.totalTv.text = "${podcast.totalListeners} Listeners"
        binding.playIv.setOnClickListener { _->
            viewModel.playOrToggleSong(podcast, true)
        }
    }

    private fun handleState() {
        collectWithLaunch(viewModel.mediaItems) {
            when(it) {
                is ResultState.Error -> Unit
                ResultState.Idle -> Unit
                ResultState.Loading -> binding.pb.isVisible = true
                is ResultState.Success -> {
                    binding.pb.isVisible = false
                    updateUI(it.data.last())

                    podcastAdapter.podcasts = it.data
                }
            }
        }


        collectWithLaunch(viewModel.playbackState) {
            binding.playIv.setImageResource(
                if (it?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )

            binding.seekbar.progress = it?.position?.toInt() ?: 0

        }

        // current playing podcast changed
        collectWithLaunch(viewModel.curPlayingPodcast) {
            if (it == null) return@collectWithLaunch

            it.toPodcast()?.let { it1 -> updateUI(it1) }
        }

        collectWithLaunch(viewModel.curPlayerPosition) {
            if(shouldUpdateSeekbar) {
                binding.seekbar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        collectWithLaunch(viewModel.curPodcastDuration) {
            binding.seekbar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            binding.durationEndTv.text = dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.durationStartTv.text = dateFormat.format(ms)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMainBinding = FragmentMainBinding.inflate(inflater, container, false)
        this.binding = binding
        return binding.root
    }
}