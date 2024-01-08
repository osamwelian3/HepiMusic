package com.hepimusic.main.explore

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import com.amplifyframework.datastore.generated.model.Song
import com.hepimusic.common.Constants
import com.hepimusic.main.albums.Album
import com.hepimusic.main.albums.AlbumsViewModel
import com.hepimusic.models.mappers.toLAlbum
import com.hepimusic.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class ExploreViewModel @Inject constructor(
    application: Application,
    /*browser: MediaBrowser,*/
    recentlyPlayedDatabase: RecentlyPlayedDatabase
): AlbumsViewModel(application /*browser*/) {
    private lateinit var preferences: SharedPreferences

    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.e("CHANGED KEY", key!!)
            when (key) {
                Constants.INITIALIZATION_COMPLETE -> {
                    Log.e("PREFERENCE CHANGED", sharedPreferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false).toString())
                    loadData()
                }
            }
        }
    val _trendingSongs = MutableLiveData<List<Song>>()
    val trendingSongs: LiveData<List<Song>> = _trendingSongs

    val _exploreAlbums = MutableLiveData<List<Album>>()
    val exploreAlbums: LiveData<List<Album>> = _exploreAlbums
    var fetchExploreAlbumsJob: Job? = null

    private lateinit var recentlyPlayedRepository: RecentlyPlayedRepository
    lateinit var recentlyPlayed: LiveData<List<RecentlyPlayed>>

    override var sortOrder: String? = "RANDOM() LIMIT 5"

    val tempCount = MutableLiveData<Int>().apply { postValue(0) }

    init {
        viewModelScope.launch {
            preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)
            preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
            val recentDao = recentlyPlayedDatabase.dao
            recentlyPlayedRepository = RecentlyPlayedRepository(recentDao)
            recentlyPlayed = withContext(Dispatchers.IO) { recentlyPlayedRepository.recentlyPlayed }
            startExploreJob()
        }
    }



    fun startExploreJob() {
        if (::recentlyPlayed.isInitialized) {
            fetchExploreAlbumsJob?.cancel()
            fetchExploreAlbumsJob = CoroutineScope(Job() + Dispatchers.IO).launch {
                getTrending()
                getExploreAlbums()
            }
            fetchExploreAlbumsJob?.start()
        }
    }

    fun stopExploreJob() {
        fetchExploreAlbumsJob?.cancel()
    }

    private fun getExploreAlbums() {
        val tag = "ViewModel Observe Albums Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of albums: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _exploreAlbums.postValue(value.items.map { it.toLAlbum() })
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

    private fun getTrending() {
        val tag = "ViewModel Observe Trending Songs Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Song>> = Consumer<DataStoreQuerySnapshot<Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of trending songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            val songs = mutableListOf<Song>()
            value.items.filter { it.trendingListens.size > 0 }.sortedByDescending { it.trendingListens.size }.forEach {
                val song = it.copyOfBuilder()
                    .trendingListens(filterListenCount(it.trendingListens))
                    .build()
                songs.add(song)
            }

            val trending = songs.map { it.copyOfBuilder().key("[item]${it.key}").build() }

            _trendingSongs.postValue(trending)
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
        val predicate: QueryPredicate = Where.matches(Song.TRENDING_LISTENS.gt(0)).queryPredicate // Song.KEY.contains(key)
        val querySortBy = QuerySortBy("Song", "trendingListens", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            Song::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    fun filterListenCount(listenCount: List<String>): List<String> {
        val threeDaysAgo = LocalDateTime.now().minusDays(30)

        val filteredListens = listenCount.reversed().map { json ->
            /*Log.e("LISTEN JSON", Json.parseToJsonElement(json).jsonObject["timestamp"].toString().removeSurrounding("\""))*/
            Json.parseToJsonElement(json)
        }.filter { listen ->
            val listenTimeStamp = LocalDateTime.parse(listen.jsonObject["timestamp"].toString().removeSurrounding("\""), DateTimeFormatter.ISO_DATE_TIME)
            /*if (listenTimeStamp.isAfter(threeDaysAgo)) {
                Log.e("LISTEN TIMESTAMP", listenTimeStamp.toString())
                Log.e("THREE DAYS AGO TIMESTAMP", threeDaysAgo.toString())
                Log.e("IS AFTER THREE DAYS AGO", listenTimeStamp.isAfter(threeDaysAgo).toString())
            }*/
            listenTimeStamp.isAfter(threeDaysAgo)
        }

        return filteredListens.map { listen ->
            /*Log.e("Filtered listens", listen.jsonObject.toString())*/
            listen.jsonObject.toString()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopExploreJob()
    }
}