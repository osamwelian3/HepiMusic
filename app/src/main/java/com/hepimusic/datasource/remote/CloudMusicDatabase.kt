package com.hepimusic.datasource.remote

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Action
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.async.Cancelable
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.QuerySortBy
import com.amplifyframework.core.model.query.QuerySortOrder
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.DataStoreItemChange
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.Album
import com.amplifyframework.datastore.generated.model.Creator
import com.amplifyframework.datastore.generated.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
/*import com.hepimusic.models.Album
import com.hepimusic.models.Creator
import com.hepimusic.models.Song*/
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    suspend fun getAllSongs(): Flow<List<Song>> = suspendCoroutine { continuation ->
        val songList: MutableList<Song> = mutableListOf()

        try {
            /*Amplify.API.query(
                ModelQuery.list(Song::class.java),
                { response ->
                    for (song in response.data) {
                        songList.add(song)
                        Log.i("MyAmplifyApp MusicDatabase", song.name)
                    }

                    continuation.resume(songList)
                },
                { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
            )*/
            val tag = "ViewModel Observe Songs Item Query"
            val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Song>> = Consumer<DataStoreQuerySnapshot<Song>> { value ->
                Log.d(tag, "success on snapshot")
                Log.d(tag, "number of songs: " + value.items.size)
                Log.d(tag, "sync status: " + value.isSynced)
                continuation.resume(flowOf(value.items))
            }
            val observationStarted = Consumer { _: Cancelable ->
                Log.d(tag, "Success on cancelable")
            }
            val onObservationError = Consumer { value: DataStoreException ->
                Log.d(tag, "error on snapshot $value")
            }
            val onObservationComplete = Action {
                Log.d(tag, "complete")
            }
            val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Song.KEY.contains(key)
            val querySortBy = QuerySortBy("song", "listofuidupvotes", QuerySortOrder.ASCENDING)
            val options = ObserveQueryOptions(predicate, listOf(querySortBy))

            Amplify.DataStore.query(
                Song::class.java,
                {
                    if (it.hasNext()) {
                        it.forEach { song ->
                            songList.add(song)
                        }
                    }
                    continuation.resume(flowOf(songList.sortedByDescending { song -> song.listens.size }))
                },
                {
                    continuation.resumeWithException(it)
                }
                /*predicate,*/
                /*observationStarted,*/
                /*onQuerySnapshot,*/
                /*onObservationError,*/
                /*onObservationComplete*/
            )

        } catch (error: Exception) {
            Log.e("MyAmplifyApp MusicDatabase", "Query failure", error)
        }




    }

    suspend fun getAllAlbums(): Flow<List<Album>> = suspendCoroutine { continuation ->
        val albumList: MutableList<Album> = mutableListOf()
        Log.e("GET ALL Albums", "Get all albums called.")

        /*Amplify.API.query(
            ModelQuery.list(Album::class.java),
            { response ->
                for (album in response.data) {
                    albumList.add(album)
                    Log.i("MyAmplifyApp MusicDatabase ALBUM", album.name)
                }

                continuation.resume(albumList)
            },
            { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
        )*/

        val tag = "ViewModel Observe Songs Item Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Album>> = Consumer<DataStoreQuerySnapshot<Album>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)
            continuation.resume(flowOf(value.items))
        }
        val observationStarted = Consumer { _: Cancelable ->
            Log.d(tag, "Success on cancelable")
        }
        val onObservationError = Consumer { value: DataStoreException ->
            Log.d(tag, "error on snapshot $value")
        }
        val onObservationComplete = Action {
            Log.d(tag, "complete")
        }
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Song.KEY.contains(key)
        val querySortBy = QuerySortBy("album", "name", QuerySortOrder.ASCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))

        Amplify.DataStore.query(
            Album::class.java,
            {
                if (it.hasNext()) {
                    it.forEach { album ->
                        albumList.add(album)
                    }
                    continuation.resume(flowOf(albumList))
                }
            },
            {
                continuation.resumeWithException(it)
            }
            /*options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete*/
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

    suspend fun getAllArtists(): Flow<List<Creator>> = suspendCoroutine { continuation ->
        val tempArtistList: MutableList<Creator> = emptyList<Creator>().toMutableList()

        try {
            /*Amplify.API.query(
                ModelQuery.list(Creator::class.java),
                { response ->
                    for (artist in response.data) {
                        tempArtistList.add(artist)
                        Log.i("MyAmplifyApp MusicDatabase", artist.name)
                    }
                    continuation.resume(tempArtistList)
                },
                { error -> Log.e("MyAmplifyApp MusicDatabase", "Query failure", error) }
            )*/

            val tag = "ViewModel Observe Songs Item Query"
            val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Creator>> = Consumer<DataStoreQuerySnapshot<Creator>> { value ->
                Log.d(tag, "success on snapshot")
                Log.d(tag, "number of songs: " + value.items.size)
                Log.d(tag, "sync status: " + value.isSynced)
                continuation.resume(flowOf(value.items))
            }
            val observationStarted = Consumer { _: Cancelable ->
                Log.d(tag, "Success on cancelable")
            }
            val onObservationError = Consumer { value: DataStoreException ->
                Log.d(tag, "error on snapshot $value")
            }
            val onObservationComplete = Action {
                Log.d(tag, "complete")
            }
            val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Song.KEY.contains(key)
            val querySortBy = QuerySortBy("creator", "name", QuerySortOrder.ASCENDING)
            val options = ObserveQueryOptions(predicate, listOf(querySortBy))

            Amplify.DataStore.query(
                Creator::class.java,
                {
                    if (it.hasNext()) {
                        it.forEach { artist ->
                            tempArtistList.add(artist)
                        }
                        continuation.resume(flowOf(tempArtistList))
                    }
                },
                {
                    continuation.resumeWithException(it)
                }
                /*options,
                observationStarted,
                onQuerySnapshot,
                onObservationError,
                onObservationComplete*/
            )

        } catch (e: Exception) {
            Log.e("MyAmplifyApp MusicDatabase", "Exception while querying artist", e)
            continuation.resume(flowOf(tempArtistList))
        }


    }
}