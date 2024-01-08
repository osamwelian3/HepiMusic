package com.hepimusic.main.albums

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
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import com.hepimusic.models.mappers.toLAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class AlbumsViewModel @Inject constructor(
    val application: Application
    /*val browser: MediaBrowser*/
): BaseMediaStoreViewModel<Album>(application) {

    val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums
    var fetchAlbumsJob: Job? = null

    init {
        startJob()
    }

    override val parentId: String
        get() = "[albumID]"

    final override val repository: MediaStoreRepository<Album>
        get() = AlbumsRepository(application, liveBrowser)

    fun startJob() {
        fetchAlbumsJob?.cancel()
        fetchAlbumsJob = CoroutineScope(Job() + Dispatchers.IO).launch {
            getAlbums()
        }
        fetchAlbumsJob?.start()
    }

    fun stopJob() {
        fetchAlbumsJob?.cancel()
    }

    private fun getAlbums() {
        val tag = "ViewModel Observe Albums Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of albums: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _albums.postValue(value.items.map { it.toLAlbum() })
            deliverResult(value.items.map { it.toLAlbum() })
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
        val querySortBy = QuerySortBy("Album", "createdAt", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            com.amplifyframework.datastore.generated.model.Album::class.java,
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