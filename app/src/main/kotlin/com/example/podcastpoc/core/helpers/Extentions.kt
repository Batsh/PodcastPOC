package com.example.podcastpoc.core.helpers

import android.app.PendingIntent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.podcastpoc.R
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import com.example.podcastpoc.modules.main.ui.main.imageUrl
import com.example.podcastpoc.modules.main.ui.main.imageUrl2
import com.example.podcastpoc.modules.main.ui.main.imageUrl3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

fun <T> Fragment.collectWithLaunch(
    flow: Flow<T>,
    flowCollector: FlowCollector<T>,
) {
    viewLifecycleOwner.lifecycleScope.launch {
        flow.collect(flowCollector)
    }
}
fun <T> AppCompatActivity.collectWithLaunch(
    flow: Flow<T>,
    flowCollector: FlowCollector<T>,
) {
    lifecycleScope.launch {
        flow.collect(flowCollector)
    }
}


fun Fragment.setImage(url: String, imageView: ImageView) {
    Glide
        .with(requireActivity())
        .load(url)
        .centerCrop()
        .placeholder(R.drawable.podcast)
        .into(imageView)
}

fun ImageView.setImage(url: String) {
    Glide
        .with(context)
        .load(url)
        .centerCrop()
        .placeholder(R.drawable.podcast)
        .into(this)
}

fun getPendingIntentFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}

fun MediaMetadataCompat.toPodcast(): PodcastDataModel.Podcast? {
    return description?.let {
        PodcastDataModel.Podcast(
            id = it.mediaId ?: "",
            title = it.title.toString(),
            subtitle = it.subtitle.toString(),
            url = it.mediaUri.toString(),
            icon = it.iconUri.toString(),
            totalListeners = 12342
        )
    }
}