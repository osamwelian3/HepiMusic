package com.hepimusic.main.playlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hepimusic.main.songs.SongsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
open class PlaylistViewModel @Inject constructor(application: Application, playlistDatabase: PlaylistDatabase, playlistItemsDatabase: PlaylistItemsDatabase) : SongsViewModel(application) {

    val playlistItemsRepository: PlaylistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)
    val playlistRepository: PlaylistRepository = PlaylistRepository(playlistDatabase.dao)

    override val parentId: String
        get() = "[allSongsID]"

    val _data = MutableLiveData<List<PlaylistItem>>()
    lateinit var playlistItems: LiveData<List<PlaylistItem>>

    val _dataPlaylist = MutableLiveData<List<Playlist>>()
    lateinit var playlists: LiveData<List<Playlist>>

    override var sortOrder: String? = "RANDOM() LIMIT 5"

    init {
        viewModelScope.launch {
            playlistItems = playlistItemsRepository.playlistItems
            playlists = playlistRepository.playlists
        }
    }

    /*override fun init(vararg params: Any?) {
        if (params.isNotEmpty()) {
            selectionArgs = arrayOf(params[0].toString())
        }
        super.init()
    }*/


    fun deliverResult(playlistId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                playlists.value?.forEach {
                    it.songsCount = playlistItemsRepository.fetchSongCount(it.id)
                }
                /*if (data.value != items) data.postValue(items)*/
            }
        }
    }

    fun reverseSelection(index: Int): Boolean {
        return playlists.value?.let {
            if (it.size > index) {
                it[index].selected = !it[index].selected
                true
            } else false
        } ?: false

    }

}
