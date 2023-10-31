package com.hepimusic.main.playlist

import android.app.Application
import android.content.ContentProviderOperation
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.hepimusic.R
import com.hepimusic.main.common.event.Event
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsRepository
import com.hepimusic.models.mappers.toPlaylist
import com.hepimusic.models.mappers.toPlaylistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddSongsToPlaylistsViewModel @Inject constructor(application: Application, playlistDatabase: PlaylistDatabase, playlistItemsDatabase: PlaylistItemsDatabase): PlaylistViewModel(application, playlistDatabase, playlistItemsDatabase) {

    private val insertionData = MutableLiveData<Event<InsertionResult>>()
    val mediatorItems = MediatorLiveData<Any>()
    private var matchingSongsIds = emptyList<String>()
    /*private lateinit var songsUri: Uri
    private lateinit var songsSelection: String
    private lateinit var songsSelectionArgs: Array<String>*/

    @Suppress("UNCHECKED_CAST")
    override fun init(
        vararg params: Any?
    ) {
        /*this.songsUri = params[0] as Uri? ?: baseSongUri
        this.songsSelection = params[1] as String? ?: basicSongsSelection
        this.songsSelectionArgs = params[2] as Array<String>? ?: basicSongsSelectionArgs*/
        viewModelScope.launch {
            mediatorItems.addSource(playlists) { mediatorItems.value = it }
            mediatorItems.addSource(insertionData) { mediatorItems.value = it }
            super.init()
            loadSongs()
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            val ids = withContext(Dispatchers.IO) {
                val tempIds = mutableListOf<String>()
                items.value?.map { tempIds.add(it.id) }
                return@withContext tempIds
            }
            matchingSongsIds = ids
        }
    }

    fun addToPlaylist(song: Song?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val selected = playlists.value?.filter { it.selected }
                if (selected == null || selected.isEmpty()) {
                    insertionData.postValue(Event(InsertionResult()))
                    return@withContext
                }

                val songIds = matchingSongsIds
                if (songIds.isEmpty()) {
                    insertionData.postValue(Event(InsertionResult(R.string.sth_went_wrong, false)))
                    return@withContext
                }
                val operations = arrayListOf<ContentProviderOperation>()

                try {
                    selected.forEach { playlist ->
                        if (song != null) {
                            val playlistItem: PlaylistItem = song.toPlaylistItem()
                            playlistItem.id = playlistItem.id+"-"+playlist.id
                            playlistItem.playlistId = playlist.id
                            playlistItemsRepository.insert(playlistItem)

                            viewModelScope.launch {

                                playlistItemsRepository.playlistItems.asFlow().collect { itList ->
                                    if (itList.isNotEmpty()) {
                                        Log.e("PLAYLIST SONG COUNT", itList.count { it.playlistId == playlist.id }.toString())
                                    }
                                }
                            }

                            playlistRepository.insert(playlist.apply {
                                songsCount =
                                    playlistItemsRepository.playlistItems.value?.count { it.playlistId == id }
                                    ?: 0
                            })
                        }
                    }

                    insertionData.postValue(Event(InsertionResult(R.string.success, true)))

                } catch (e: Exception){
                    insertionData.postValue(Event(InsertionResult(R.string.sth_went_wrong, false)))
                }
                /*selected.forEach { playlist ->
                    playlistItemsRepository.insert(playlist)
                    songIds.forEach { id ->
                        *//*val values = ContentValues()
                        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, playlist.songsCount + 1)
                        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, id)
                        val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist.id)
                        val cpo = ContentProviderOperation.newInsert(uri).withValues(values).build()
                        operations.add(cpo)*//*
                    }
                }
                val result = getApplication<Application>().contentResolver.applyBatch(MediaStore.AUTHORITY, operations)*/

                /*if (result.size == operations.size) {
                    insertionData.postValue(Event(InsertionResult(R.string.success, true)))
                } else {
                    insertionData.postValue(Event(InsertionResult(R.string.sth_went_wrong, false)))
                }*/
            }
        }
    }

    fun removeSource() {
        mediatorItems.removeSource(playlists)
        mediatorItems.removeSource(insertionData)
    }

}

internal data class InsertionResult(@StringRes val message: Int? = null, val success: Boolean? = null)