package com.hepimusic.main.songs

import android.app.Application
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
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import com.hepimusic.models.mappers.toLAlbum
import com.hepimusic.models.mappers.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class SongsViewModel @Inject constructor(val application: Application): BaseMediaStoreViewModel<Song>(application) {

    val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs
    var fetchSongsJob: Job? = null

    init {
        startJob()
    }

    override val parentId: String
        get() = "[allSongsID]"

    override val repository: MediaStoreRepository<Song>
        get() = SongsRepository(application, liveBrowser)

    fun startJob() {
        fetchSongsJob?.cancel()
        fetchSongsJob = CoroutineScope(Job() + Dispatchers.IO).launch {
            getSongs()
        }
        fetchSongsJob?.start()
    }

    fun stopJob() {
        fetchSongsJob?.cancel()
    }

    private fun getSongs() {
        val tag = "ViewModel Observe Songs Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _songs.postValue(value.items.map { it.toSong() })
            deliverResult(value.items.map { it.toSong() })
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
        stopJob()
    }
}