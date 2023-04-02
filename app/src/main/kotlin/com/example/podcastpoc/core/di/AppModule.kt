
package com.example.podcastpoc.core.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.podcastpoc.modules.exoplayer.PodcastServiceConnection
import com.example.podcastpoc.R
import com.example.podcastpoc.modules.main.adapters.SwipePodcastAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePodcastServiceConnection(
        @ApplicationContext context: Context
    ) = PodcastServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipePodcastAdapter() = SwipePodcastAdapter()

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context)
        .setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.podcast)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
}
