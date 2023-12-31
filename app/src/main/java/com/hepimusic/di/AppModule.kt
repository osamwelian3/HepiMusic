package com.hepimusic.di

import android.content.Context
import android.content.SharedPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.R
import com.hepimusic.datasource.local.databases.AlbumDatabase
import com.hepimusic.datasource.local.databases.ArtistDatabase
import com.hepimusic.datasource.local.databases.SongDatabase
import com.hepimusic.datasource.remote.CloudMusicDatabase
import com.hepimusic.datasource.repositories.SongRepository
import com.hepimusic.main.explore.RecentlyPlayedDatabase
import com.hepimusic.main.playlist.PlaylistDatabase
import com.hepimusic.main.playlist.PlaylistItemsDatabase
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
    fun providePreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(@ApplicationContext context: Context) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.album_art)
            .error(R.drawable.album_art)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Singleton
    @Provides
    fun provideRecentlyPlayedDatabase(@ApplicationContext context: Context) = RecentlyPlayedDatabase(context)

    /*@Singleton
    @Provides
    fun provideBrowserInstance(@ApplicationContext context: Context): ListenableFuture<MediaBrowser> {
        return MediaBrowser.Builder(
                context,
                SessionToken(context, ComponentName(context, MusicService::class.java))
            )
                .buildAsync()
    }

    @Singleton
    @Provides
    suspend fun provideBrowser(browserFuture: ListenableFuture<MediaBrowser>): MediaBrowser = suspendCoroutine { continuation ->
        browserFuture.addListener(Runnable {
            fun run() {
                if (browserFuture.isDone && !browserFuture.isCancelled){
                    continuation.resume(browserFuture.get())
                } else {
                    runBlocking {
                        delay(4000)
                        run()
                    }
                }
            }
            run()
        }, MoreExecutors.directExecutor())
    }

    @Singleton
    @Provides
    fun provideControllerInstance(@ApplicationContext context: Context): ListenableFuture<MediaController> {
        return MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, MusicService::class.java))
        )
            .buildAsync()
    }

    @Singleton
    @Provides
    suspend fun provideController(controllerFuture: ListenableFuture<MediaController>): MediaController = suspendCoroutine { continuation ->

        controllerFuture.addListener(Runnable {
            fun run() {
                if (controllerFuture.isDone && !controllerFuture.isCancelled){
                    continuation.resume(controllerFuture.get())
                } else {
                    runBlocking {
                        delay(3000)
                        run()
                    }
                }
            }
            run()
        }, MoreExecutors.directExecutor())
    }*/

    @Singleton
    @Provides
    fun provideCloudMusicDatabase() = CloudMusicDatabase()

    @Singleton
    @Provides
    fun provideSongDatabase(@ApplicationContext context: Context) = SongDatabase(context)

    @Singleton
    @Provides
    fun provideAlbumDatabase(@ApplicationContext context: Context) = AlbumDatabase(context)

    @Singleton
    @Provides
    fun providePlaylistDatabase(@ApplicationContext context: Context) = PlaylistDatabase(context)

    @Singleton
    @Provides
    fun providePlaylistItemsDatabase(@ApplicationContext context: Context) = PlaylistItemsDatabase(context)

    @Singleton
    @Provides
    fun provideArtistsDatabase(@ApplicationContext context: Context) = ArtistDatabase(context)

    @Singleton
    @Provides
    fun provideSongRepository(musicDatabase: CloudMusicDatabase, songDatabase: SongDatabase, albumDatabase: AlbumDatabase, artistDatabase: ArtistDatabase) = SongRepository(musicDatabase, songDatabase, albumDatabase, artistDatabase)
}