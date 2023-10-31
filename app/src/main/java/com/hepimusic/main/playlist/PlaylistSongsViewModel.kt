package com.hepimusic.main.playlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.hepimusic.main.songs.SongsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistSongsViewModel @Inject constructor(application: Application, playlistDatabase: PlaylistDatabase, val playlistItemsDatabase: PlaylistItemsDatabase): SongsViewModel(application) {

    override val parentId: String
        get() = "[allSongsID]"

    lateinit var playlist: Playlist

    val playlistItemsRepository: PlaylistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)
    val playlistRepository: PlaylistRepository = PlaylistRepository(playlistDatabase.dao)

    lateinit var playlistItems: LiveData<List<PlaylistItem>>
    lateinit var playlists: LiveData<List<Playlist>>

    init {
        viewModelScope.launch {
            playlists = playlistRepository.playlists
            playlistItems = playlistItemsRepository.playlistItems
        }

    }

    fun init(playlistId: String) {
        viewModelScope.launch {
            val tempRepo = PlaylistItemsRepository(playlistItemsDatabase.dao, playlistId)
            playlistItems = tempRepo.playlistItems
        }

    }
}