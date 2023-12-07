package com.hepimusic.main.profile

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.amplifyframework.datastore.DataStoreItemChange
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.Song
import com.hepimusic.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(application: Application): ViewModel() {
    private val _username = MutableLiveData<String>().apply { postValue("") }
    val username: LiveData<String> = _username
    private val _userId = MutableLiveData<String>().apply { postValue("") }
    val userId: LiveData<String> = _userId
    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> = _profile
    val preferences: SharedPreferences = application.getSharedPreferences("main", MODE_PRIVATE)


    private val _favourites = MutableLiveData<List<Song>>()
    val favourites: LiveData<List<Song>> = _favourites

    init {
        Amplify.Auth.getCurrentUser(
            { user ->
                if (!preferences.getBoolean(Constants.AUTH_TYPE_SOCIAL, false)) {
                    _username.postValue(user.username)
                    _userId.postValue(user.userId)
                    Amplify.DataStore.query(
                        com.amplifyframework.datastore.generated.model.Profile::class.java,
                        Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.eq(user.username)),
                        {
                            if (it.hasNext()) {
                                _profile.postValue(Profile(it.next()))
                            }
                        },
                        {

                        }
                    )
                    Amplify.DataStore.observe(
                        com.amplifyframework.datastore.generated.model.Profile::class.java,
                        Where.matches(com.amplifyframework.datastore.generated.model.Profile.NAME.eq(user.username)).queryPredicate,
                        {

                        },
                        {
                            if (it.type() == DataStoreItemChange.Type.UPDATE) {
                                _profile.postValue(Profile(it.item()))
                            }
                        },
                        {

                        },
                        {

                        }
                    )
                } else {
                    _username.postValue(preferences.getString(Constants.USERNAME, ""))
                    Amplify.DataStore.query(
                        com.amplifyframework.datastore.generated.model.Profile::class.java,
                        Where.matches(com.amplifyframework.datastore.generated.model.Profile.EMAIL.eq(preferences.getString(Constants.USERNAME, ""))),
                        {
                            if (it.hasNext()) {
                                _profile.postValue(Profile(it.next()))
                                Log.e("profile name", it.next().email.toString())
                                /*val profile = Profile(it.next().copyOfBuilder().build())
                                Log.e("profile name", profile.originalProfile.email.toString())
                                _profile.postValue(profile)*/
                            } else {
                                Log.e("profile name", it.next().name.toString())
                            }
                        },
                        {

                        }
                    )
                    Amplify.DataStore.observe(
                        com.amplifyframework.datastore.generated.model.Profile::class.java,
                        Where.matches(com.amplifyframework.datastore.generated.model.Profile.EMAIL.eq(preferences.getString(Constants.USERNAME, ""))).queryPredicate,
                        {

                        },
                        {
                            if (it.type() == DataStoreItemChange.Type.UPDATE) {
                                _profile.postValue(Profile(it.item()))
                            }
                        },
                        {

                        },
                        {

                        }
                    )
                }
                getFavourites()
            },
            {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(application.applicationContext, it.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        )
        viewModelScope.launch {
            profile.observeForever {
                _username.postValue(it.originalProfile.name)
            }
        }

    }

    fun getFavourites() {
        val tag = "ViewModel Observe Favourite Songs Query"
        val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Song>> = Consumer<DataStoreQuerySnapshot<Song>> { value ->
            Log.d(tag, "success on snapshot")
            Log.d(tag, "number of favourite songs: " + value.items.size)
            Log.d(tag, "sync status: " + value.isSynced)

            val songs = mutableListOf<Song>()
            value.items.filter { it.listOfUidUpVotes.size > 0 }.sortedByDescending { it.listOfUidUpVotes.size }.forEach {
                if (it.listOfUidUpVotes?.find { upVoteKey -> upVoteKey == userId.value } != null) {
                    songs.add(it)
                }
            }
            Log.d(tag, "number of favourite songs: " + songs.size)

            val favourites = songs.map { it.copyOfBuilder().key("[item]${it.key}").build() }

            _favourites.postValue(favourites)
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
        val predicate: QueryPredicate = Where.matches(Song.LIST_OF_UID_UP_VOTES.gt(0)).queryPredicate // Song.KEY.contains(key)
        val querySortBy = QuerySortBy("Song", "listOfUidUpVotes", QuerySortOrder.DESCENDING)
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

    fun overrideCurrentItems(items: List<Song>) {
        _favourites.postValue(items)
        _favourites.value = items
    }
}