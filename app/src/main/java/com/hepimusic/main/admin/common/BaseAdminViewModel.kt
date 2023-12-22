package com.hepimusic.main.admin.common

import android.app.Application
import android.util.Log
import androidx.databinding.BaseObservable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amplifyframework.auth.AuthUser
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
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StoragePagedListOptions
import com.hepimusic.main.admin.albums.Album
import com.hepimusic.main.admin.categories.Category
import com.hepimusic.main.admin.creators.Creator
import com.hepimusic.main.admin.songs.Song
import com.hepimusic.main.profile.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID


abstract class BaseAdminViewModel(application: Application): ViewModel() {

    private val _authUser = MutableLiveData<AuthUser>()
    val authUser: LiveData<AuthUser> = _authUser

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile

    private val _profiles = MutableLiveData<List<Profile>>()
    val profiles: LiveData<List<Profile>> = _profiles

    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _creators = MutableLiveData<List<Creator>>()
    val creators: LiveData<List<Creator>> = _creators

    private val _fileKeys = MutableLiveData<List<String>?>()
    val fileKeys : MutableLiveData<List<String>?> = _fileKeys

    var job: Job? = null

    init {
        getAuthUser()
        observeSongs()
        observeAlbums()
        observeCategories()
        observeCreators()
        observeProfile()
    }

    fun init() {
        getAuthUser()
        observeSongs()
        observeAlbums()
        observeCategories()
        observeCreators()
        observeProfile()
    }

    private fun getFileKeys() {
        Amplify.Storage.list(
            "",
            StoragePagedListOptions.builder()
                .setPageSize(1000000000)
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build(),
            {
                val keys = it.items.map { it.key }
                _fileKeys.postValue(keys)
                /*for (k in keys) {
                    if (k.contains("$key.$ext")) {
                        Log.e("EXISTS LOOP", "Image with key $key.$ext exists in storage")
                    }
                }
                if (keys.contains("$key.$ext")) {
                    Log.e("EXISTS", "Image with key $key.$ext exists in storage")
                    key = "v"+ UUID.randomUUID().toString().substring(0, 4)+key
                }*/
            },
            {

            }
        )
    }

    private fun getAuthUser() {
        Amplify.Auth.getCurrentUser(
            {
                _authUser.postValue(it)
            },
            {
                Log.e("GET CURRENT USER EXCEPTION", it.message.toString())
            }
        )
    }

    private fun observeProfile() {
        val tag = "Base Admin ViewModel Profile ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Profile>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Profile>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of admin profile: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            _profiles.postValue(value.items.map { Profile(it) })
            if (authUser.value != null) {
                val p = value.items.find {
                    it.owner == authUser.value!!.username
                }
                _profile.postValue(p?.let { Profile(it) })
            } else {
                job = CoroutineScope(Dispatchers.Main + Job()).launch {
                    authUser.observeForever { user ->
                        if (user != null) {
                            val p = value.items.find {
                                it.owner == authUser.value!!.username
                            }
                            _profile.postValue(p?.let { Profile(it) })
                            job?.cancel()
                        }
                    }
                    getAuthUser()
                }
                job?.start()
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
        val querySortBy = QuerySortBy("Profile", "createdAt", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            com.amplifyframework.datastore.generated.model.Profile::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    private fun observeSongs() {
        val tag = "Base Admin ViewModel Songs ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of admin songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            getFileKeys()

            val songsList = mutableListOf<Song>()
            value.items.sortedByDescending { it.createdAt }.forEach { song ->
                songsList.add(Song(song.key, song))
            }

            _songs.postValue(songsList.sortedByDescending { it.originalSong.createdAt })
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

    private fun observeAlbums() {
        val tag = "Base Admin ViewModel Albums ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Album>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of admin albums: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            val albumsList = mutableListOf<Album>()
            value.items.sortedByDescending { it.createdAt }.forEach { album ->
                albumsList.add(Album(album.key, album))
            }

            _albums.postValue(albumsList)
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

    private fun observeCategories() {
        val tag = "Base Admin ViewModel Categories ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Category>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Category>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of admin categories: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            val categoriesList = mutableListOf<Category>()
            value.items.sortedByDescending { it.createdAt }.forEach { category ->
                categoriesList.add(Category(category.key, category))
            }

            _categories.postValue(categoriesList)
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
        val querySortBy = QuerySortBy("Category", "createdAt", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            com.amplifyframework.datastore.generated.model.Category::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }

    private fun observeCreators() {
        val tag = "Base Admin ViewModel Creators ObserveQuery"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Creator>> = Consumer<DataStoreQuerySnapshot<com.amplifyframework.datastore.generated.model.Creator>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of admin creators: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            val creatorsList = mutableListOf<Creator>()
            value.items.sortedByDescending { it.createdAt }.forEach { creator ->
                creatorsList.add(Creator(creator.key, creator))
            }

            _creators.postValue(creatorsList)
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
        val querySortBy = QuerySortBy("Creator", "createdAt", QuerySortOrder.DESCENDING)
        val options = ObserveQueryOptions(predicate, listOf(querySortBy))
        Amplify.DataStore.observeQuery(
            com.amplifyframework.datastore.generated.model.Creator::class.java,
            options,
            observationStarted,
            onQuerySnapshot,
            onObservationError,
            onObservationComplete
        )
    }
}