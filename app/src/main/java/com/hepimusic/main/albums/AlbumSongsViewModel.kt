package com.hepimusic.main.albums

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.Album
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.models.mappers.toLAlbum
import com.hepimusic.models.mappers.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumSongsViewModel @Inject constructor(application: Application) : SongsViewModel(application) {

    var preferences: SharedPreferences

    val _albumSongs = MutableLiveData<List<Song>>()
    val albumSongs: LiveData<List<Song>> = _albumSongs
    var fetchAlbumSongsJob: Job? = null
    init {
        preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)
        startJob()
    }
    override val parentId: String
        get() = "[allSongsID]"

    override fun init(vararg params: Any?) {
        super.init(params)
        startAlbumSongsJob()
    }

    fun startAlbumSongsJob() {
        fetchAlbumSongsJob?.cancel()
        fetchAlbumSongsJob = CoroutineScope(Job() + Dispatchers.IO).launch {
            fetchAlbumSongs()
        }
        fetchAlbumSongsJob?.start()
    }

    fun stopAlbumSongsJob() {
        fetchAlbumSongsJob?.cancel()
    }

    private fun fetchAlbumSongs() {
        val tag = "ViewModel Observe Songs Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _albumSongs.postValue(value.items.map { it.toSong() })
            deliverResult(value.items.map { it.toSong() })
            getAlbumSongs()
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
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate
        val querySortBy = QuerySortBy("Song", "createdAt", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            com.amplifyframework.datastore.generated.model.Song::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopAlbumSongsJob()
    }
}