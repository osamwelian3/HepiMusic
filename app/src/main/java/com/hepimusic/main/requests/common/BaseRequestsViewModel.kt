package com.hepimusic.main.requests.common

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amplifyframework.api.graphql.MutationType
import com.amplifyframework.api.graphql.SubscriptionType
import com.amplifyframework.api.graphql.model.ModelSubscription
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.core.Action
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.async.Cancelable
import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.QuerySortBy
import com.amplifyframework.core.model.query.QuerySortOrder
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.Profile
import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlayerCopy
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestPlaylistCopy
import com.amplifyframework.datastore.generated.model.RequestSong
import com.amplifyframework.datastore.generated.model.Song
import com.google.gson.Gson
import com.hepimusic.common.Constants.AUTH_USER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseRequestsViewModel(application: Application): ViewModel() {

    val preferences = application.getSharedPreferences("main", MODE_PRIVATE)

    val _players = MutableLiveData<List<RequestPlayerCopy>>()
    val players: LiveData<List<RequestPlayerCopy>> = _players

    val _currentPlayer = MutableLiveData<RequestPlayerCopy>()
    val currentPlayer: LiveData<RequestPlayerCopy> = _currentPlayer

    val _playlists = MutableLiveData<List<RequestPlaylistCopy>>()
    val playlists: LiveData<List<RequestPlaylistCopy>> = _playlists

    val _requestSongs = MutableLiveData<List<RequestSong>>()
    val requestSongs: LiveData<List<RequestSong>> = _requestSongs

    val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    var job: Job? = null
    var job2: Job? = null
    var job3: Job? = null

    init {
        job = CoroutineScope(Job() + Dispatchers.IO).launch {
            getSongs()
            getPlaylists()
            getRequestSongs()
            getPlayers()
        }
    }

    fun initialize() {
        job?.cancel()
        job = CoroutineScope(Job() + Dispatchers.IO).launch {
            getSongs()
            getPlaylists()
            getRequestSongs()
            getPlayers()
        }
    }

    private fun getPlayers() {
        val tag = "Base Requests ViewModel Players ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<RequestPlayer>> = Consumer<DataStoreQuerySnapshot<RequestPlayer>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of request players: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            for (p in value.items) {
                val uString = preferences.getString(AUTH_USER, null)
                if (uString != null && uString != "null" && uString != "" && p.ownerData.contains(uString.toString())) {
                    _currentPlayer.postValue(p.toRequestPlayerCopy())
                }
            }

            if (playlists.value.isNullOrEmpty()) {
                _players.postValue(value.items.map { it.toRequestPlayerCopy() })
            } else {
                _players.postValue(value.items.map { player ->
                    player.toRequestPlayerCopy(playlists.value?.map { it.toRequestPlaylist() }?.filter { it.player.key == player.key }
                        ?: emptyList())
                })
            }
            job2?.cancel()
            job2 = CoroutineScope(Job() + Dispatchers.Main).launch {
                playlists.observeForever { playlistsList ->
                    if (!playlistsList.isNullOrEmpty()) {
                        _players.postValue(value.items.map { player ->
                            player.toRequestPlayerCopy(playlists.value?.map { it.toRequestPlaylist() }
                                ?.filter { it.player.key == player.key }
                                ?: emptyList())
                        })
                    }
                }
            }
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
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.contains(authUser.value!!.username).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.username)).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.userId))).queryPredicate
        val querySortBy = QuerySortBy("RequestPlayer", "createdDate", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            RequestPlayer::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    private fun getPlaylists() {
        val tag = "Base Requests ViewModel Playlists ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<RequestPlaylist>> = Consumer<DataStoreQuerySnapshot<RequestPlaylist>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of request playlists: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            if (requestSongs.value.isNullOrEmpty()) {
                _playlists.postValue(value.items.map { it.toRequestPlaylistCopy() })
            } else {
                _playlists.postValue(value.items.map { playlist -> playlist.toRequestPlaylistCopy(requestSongs.value?.filter { it.playlist.key == playlist.key }) })
            }

            job3?.cancel()
            job3 = CoroutineScope(Job() + Dispatchers.Main).launch {
                requestSongs.observeForever { requestSongsList ->
                    if (!requestSongsList.isNullOrEmpty()) {
                        _playlists.postValue(value.items.map { playlist -> playlist.toRequestPlaylistCopy(requestSongs.value?.filter { it.playlist.key == playlist.key }) })
                    }
                }
            }
            getPlayers()
            /*if (players.value.isNullOrEmpty()) {
                getPlayers()
            } else {
                _players.postValue(players.value!!.map { it.toRequestPlayer() }.map { player -> player.toRequestPlayerCopy(playlists.value?.filter { it.player.key == player.key } ?: emptyList()) })
            }*/
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
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.contains(authUser.value!!.username).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.username)).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.userId))).queryPredicate
        val querySortBy = QuerySortBy("RequestPlaylist", "createdDate", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            RequestPlaylist::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    private fun getRequestSongs() {
        val tag = "Base Requests ViewModel RequestSongs ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<RequestSong>> = Consumer<DataStoreQuerySnapshot<RequestSong>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of request requestSongs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _requestSongs.postValue(synchronizeLists(_requestSongs, value.items))
            getPlaylists()
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
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.contains(authUser.value!!.username).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.username)).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.userId))).queryPredicate
        val querySortBy = QuerySortBy("RequestSong", "createdDate", QuerySortOrder.ASCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            RequestSong::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
        /*Amplify.API.subscribe(
            ModelSubscription.onCreate(RequestSong::class.java),
            {
                Log.e("SUBSCRIPTION ESTABLISHED", it)
            },
            {
                if (!it.hasErrors()) {
                    val newSongs = mutableListOf<RequestSong>()
                    requestSongs.value?.let { it1 -> newSongs.addAll(it1) }
                    newSongs.add(it.data)
                    _requestSongs.postValue(newSongs)
                    getPlaylists()
                } else {

                }
            },
            {
                Log.e("SUBSCRIPTION EXCEPTION", it.message.toString())
            },
            {
                Log.e("SUBSCRIPTION COMPLETED", "Subscription completed")
            }
        )
        Amplify.API.subscribe(
            ModelSubscription.onUpdate(RequestSong::class.java),
            {
                Log.e("SUBSCRIPTION ESTABLISHED", it)
            },
            { updatedSong ->
                if (!updatedSong.hasErrors()) {
                    val newSongs = mutableListOf<RequestSong>()
                    requestSongs.value?.let { it1 -> newSongs.addAll(it1.filter { it.key != updatedSong.data.key }) }
                    newSongs.add(updatedSong.data)
                    _requestSongs.postValue(newSongs)
                    getPlaylists()
                } else {
                    Log.e("SUBSCRIPTION ERRORS", updatedSong.errors.toString())
                }
            },
            {
                Log.e("SUBSCRIPTION EXCEPTION", it.message.toString())
            },
            {
                Log.e("SUBSCRIPTION COMPLETED", "Subscription completed")
            }
        )
        Amplify.API.subscribe(
            ModelSubscription.onDelete(RequestSong::class.java),
            {
                Log.e("SUBSCRIPTION ESTABLISHED", it)
            },
            { updatedSong ->
                if (!updatedSong.hasErrors()) {
                    val newSongs = mutableListOf<RequestSong>()
                    requestSongs.value?.let { it1 -> newSongs.addAll(it1.filter { it.key != updatedSong.data.key }) }
                    _requestSongs.postValue(newSongs)
                    getPlaylists()
                } else {
                    Log.e("SUBSCRIPTION ERRORS", updatedSong.errors.toString())
                }
            },
            {
                Log.e("SUBSCRIPTION EXCEPTION", it.message.toString())
            },
            {
                Log.e("SUBSCRIPTION COMPLETED", "Subscription completed")
            }
        )*/
    }

    private fun getSongs() {
        val tag = "Base Requests ViewModel Songs ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Song>> = Consumer<DataStoreQuerySnapshot<Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of request songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _songs.postValue(value.items)
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
        val predicate: QueryPredicate = Where.matchesAll().queryPredicate // Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.contains(authUser.value!!.username).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.username)).or(com.amplifyframework.datastore.generated.model.Profile.OWNER.contains(authUser.value!!.userId))).queryPredicate
        val querySortBy = QuerySortBy("Song", "createdAt", QuerySortOrder.ASCENDING)
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

    fun synchronizeLists(listA: MutableLiveData<List<RequestSong>>, listB: MutableList<RequestSong>): MutableList<RequestSong> {
        if (listA.value.isNullOrEmpty()) {
            return listB
        }
        // Copy listB to work on
        val copyOfListB = listB.toMutableList()

        // Update or add songs from listA to listB
        for (songA in listA.value!!) {
            var existingSongB = copyOfListB.find { it.key == songA.key }
            if (existingSongB != null) {
                // Update properties if song exists
                if (existingSongB.createdAt == null || existingSongB.updatedAt == null) {
                    val idx = copyOfListB.indexOf(existingSongB)
                    copyOfListB.removeIf { it.key == existingSongB!!.key }
                    existingSongB = songA
                    copyOfListB.add(idx, existingSongB)
                }

            } else {
                // Add song if it doesn't exist
                copyOfListB.add(songA)
            }
        }

        // Remove songs from listB that are not in listA
        val iterator = copyOfListB.iterator()
        while (iterator.hasNext()) {
            val songB = iterator.next()
            if (listA.value!!.none { it.primaryKeyString == songB.primaryKeyString }) {
                iterator.remove()
            }
        }

        // Update listB with the synchronized list
        /*listB.clear()
        listB.addAll(copyOfListB)*/
        return copyOfListB
    }

    override fun onCleared() {
        super.onCleared()
        job3?.cancel()
        job2?.cancel()
        job?.cancel()
    }
}