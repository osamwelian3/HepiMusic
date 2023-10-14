package com.hepimusic.datasource.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.DataStoreItemChange
import com.hepimusic.models.Album
import com.hepimusic.models.Creator
import com.hepimusic.models.Song
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CloudMusicDatabase {

    private val songCollection: List<Song>
        get() {
            val tempSongList: MutableList<Song> = mutableListOf()
            Amplify.API.query(
                ModelQuery.list(Song::class.java),
                { response ->
                    for (song in response.data) {
                        tempSongList.add(song)
                        Log.i("MyAmplifyApp", song.name)
                    }
                },
                { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
            )
            return tempSongList
        }

    suspend fun getAllSongs(): List<Song> = suspendCoroutine { continuation ->
        val songList: MutableList<Song> = mutableListOf()

        Amplify.API.query(
            ModelQuery.list(Song::class.java),
            { response ->
                for (song in response.data) {
                    songList.add(song)
                    Log.i("MyAmplifyApp MusicDatabase", song.name)
                }

                continuation.resume(songList)
            },
            { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
        )

    }

    suspend fun getAllAlbums(): List<Album> = suspendCoroutine { continuation ->
        val albumList: MutableList<Album> = mutableListOf()
        Log.e("GET ALL Albums", "Get all albums called.")

        Amplify.API.query(
            ModelQuery.list(Album::class.java),
            { response ->
                for (album in response.data) {
                    albumList.add(album)
                    Log.i("MyAmplifyApp MusicDatabase ALBUM", album.name)
                }

                continuation.resume(albumList)
            },
            { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
        )

    }

//    suspend fun getAllAlbums(): List<Album> = suspendCoroutine { continuation ->
//        val tempAlbumList: MutableList<Album> = mutableListOf()
//
//        try {
//            Amplify.API.query(
//                ModelQuery.list(Album::class.java),
//                { response ->
//                    for (album in response.data) {
//                        tempAlbumList.add(album)
//                        Log.i("MyAmplifyApp MusicDatabase", album.name)
//                    }
//                    continuation.resume(tempAlbumList)
//                },
//                { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
//            )
//        } catch (e: Exception) {
//            Log.e("MyAmplifyApp MusicDatabase", "Exception while querying albums", e)
//            continuation.resume(tempAlbumList)
//        }
//
//
//    }

    suspend fun getAllArtists(): List<Creator> = suspendCoroutine { continuation ->
        val tempArtistList: MutableList<Creator> = emptyList<Creator>().toMutableList()

        try {
            Amplify.API.query(
                ModelQuery.list(Creator::class.java),
                { response ->
                    for (artist in response.data) {
                        tempArtistList.add(artist)
                        Log.i("MyAmplifyApp MusicDatabase", artist.name)
                    }
                    continuation.resume(tempArtistList)
                },
                { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
            )
        } catch (e: Exception) {
            Log.e("MyAmplifyApp MusicDatabase", "Exception while querying artist", e)
            continuation.resume(tempArtistList)
        }


    }
}