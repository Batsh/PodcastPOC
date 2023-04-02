package com.example.podcastpoc.modules.main.data.model

 class PodcastDataModel : ArrayList<PodcastDataModel.Podcast>() {
    data class Podcast(
        val id: String,
        val title: String,
        val subtitle: String,
        val url: String,
        val totalListeners: Int,
        val icon: String
    )
}
