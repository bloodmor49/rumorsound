package com.example.rumorsound.di

import android.content.Context
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

//    @Singleton
//    @Provides
//    fun provideGlideInstance(
//        @ApplicationContext context: Context
//    ) = Glide.with(context).setDefaultRequestOptions(
//        RequestOptions()
//            .placeholder(R.drawable.ic_image)
//            .error(R.drawable.ic_image)
//            .diskCacheStrategy(DiskCacheStrategy.DATA)
//    )

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context : Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

    @Singleton
    @Provides
    fun provideSongAdapter() = SongAdapter()


}