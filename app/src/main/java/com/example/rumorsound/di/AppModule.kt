package com.example.rumorsound.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.rumorsound.R
import com.example.rumorsound.data.exoplayer.MusicServiceConnection
import com.example.rumorsound.presentation.adapters.SongAdapter
import com.example.rumorsound.presentation.adapters.SwipeSongAdapter
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
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.logo_okabe)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context : Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

}