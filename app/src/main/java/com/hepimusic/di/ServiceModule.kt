package com.hepimusic.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(@ApplicationContext context: Context, audioAttributes: AudioAttributes) = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()
/*
    @ServiceScoped
    @Provides
    fun provideCloudMusicDatabase() = CloudMusicDatabase()

    @ServiceScoped
    @Provides
    fun provideSongDatabase(@ApplicationContext context: Context) = SongDatabase(context)

    @ServiceScoped
    @Provides
    fun provideAlbumDatabase(@ApplicationContext context: Context) = AlbumDatabase(context)*/


/*
    @ServiceScoped
    @Provides
    fun provideRecentlyPlayedDatabase(@ApplicationContext context: Context) = RecentlyPlayedDatabase(context)
*/


//    @ServiceScoped
//    @Provides
//    fun provideSongRepository(cloudMusicDatabase: CloudMusicDatabase, songDatabase: SongDatabase, albumDatabase: AlbumDatabase) = SongRepository(cloudMusicDatabase, songDatabase, albumDatabase)

}