package com.hepimusic.main.playlist

import android.app.Application
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.hepimusic.main.common.event.Event
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.models.mappers.toPlaylist
import com.hepimusic.models.mappers.toPlaylistItem
import com.hepimusic.models.mappers.toSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlaylistSongsEditorViewModel @Inject constructor(application: Application, playlistDatabase: PlaylistDatabase, playlistItemsDatabase: PlaylistItemsDatabase): SongsViewModel(application) {

//    private val playlistSongsRepository = PlaylistSongsRepository(application, browser)
    val playlistItemsRepository: PlaylistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)
    val playlistRepository: PlaylistRepository = PlaylistRepository(playlistDatabase.dao)
    /*private val playlistSongsProjection =
        listOf(*baseSongsProjection, MediaStore.Audio.Playlists.Members.AUDIO_ID).toTypedArray()*/
    /*private lateinit var playlistSongsUri: Uri*/
    private lateinit var initiallySelectedItems: List<Song>
    private val _playlistValue = MutableLiveData<Event<Boolean>>()
    val playlistValue: LiveData<Event<Boolean>> get() = _playlistValue
    lateinit var playlist: Playlist


    override fun init(vararg params: Any?) {
        /*playlistSongsUri = MediaStore.Audio.Playlists.Members.getContentUri("external", params[0] as Long)*/
        super.init()
    }

    fun reverseSelection(index: Int): Boolean {
        return data.value?.let {
            if (it.size > index) {
                it[index].selected = !it[index].selected
                true
            } else false
        } ?: false

    }


    /**
     *  Preset all songs on [items] that exists in the playlist and post the modified items
     *  @param items all songs on the device
     */

    override fun deliverResult(items: List<Song>) {
        // Find all the songs that belong to the playlist and select them
        /*MediaStore.Audio.Playlists.Members.AUDIO_ID*/
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val songsInPlaylist = mutableListOf<Song>()
                playlistItemsRepository.playlistItems.asFlow().collect { itemList ->
                    itemList.map {
                        if (::playlist.isInitialized && it.playlistId == playlist.id) {
                            val sng = it.toSong()
                            songsInPlaylist.add(sng)
                        }
                    }

                    items.forEach { song ->
                        // The tracks are the same if they have the same titleKey and the same file paths
                        /*val playlistSong =
                            songsInPlaylist.firstOrNull { it.titleKey == song.titleKey && it.path == song.path }*/
                        val playlistSong = songsInPlaylist.find { it.id == song.id && it.path == song.path }
                        if (playlistSong != null) {
                            song.selected = true
                            song.audioId = playlistSong.audioId
                        }
                    }
                    initiallySelectedItems = items.filter { it.selected }
                    if (data.value != items) data.postValue(items)
                }
//                val songsInPlaylist = playlistSongsRepository.loadData(parentId = "[allSongsID]")

            }
        }
    }

    fun updatePlaylist() {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                val selectedItems: List<Song> = items.value!!.filter { it.selected }
                // Items that weren't selected initially.
                // A better explanation: Items that doesn't exist in this playlist but are now selected
                val addableItems: List<Song> = selectedItems.minus(initiallySelectedItems)

                // Items that were initially selected but no longer selected
                val removableItems = initiallySelectedItems.minus(selectedItems)


                if (addableItems.isNotEmpty()) {
                    // Add songs to playlist
                    if (!addSongs(addableItems)) {
                        return@withContext false
                    }
                }

                if (!removableItems.isNullOrEmpty()) {
                    // Remove songs from playlist
                    if (!deleteSongs(removableItems)) {
                        return@withContext false
                    }

                }
                return@withContext true

            }
            _playlistValue.value = Event(success)
        }
    }

    @WorkerThread

    private fun deleteSongs(songs: List<Song>): Boolean {
        /*val string = songs.map { it.audioId }.joinToString(", ")
        val where = MediaStore.Audio.Playlists.Members.AUDIO_ID + " IN ($string)"
        val deletedRows = application.contentResolver?.delete(playlistSongsUri, where, null) ?: 0
        return deletedRows > 0*/
        return try {
            viewModelScope.launch(Dispatchers.IO) {
                songs.forEach {
                    val playlistItem: PlaylistItem = it.toPlaylistItem()
                    playlistItem.id = playlistItem.id+"-"+playlist.id
                    playlistItem.playlistId = playlist.id
                    playlistItemsRepository.deleteItem(playlistItem.id)
                }
            }
            true
        } catch (e: Exception) {
            Toast.makeText(application.applicationContext, e.message, Toast.LENGTH_LONG).show()
            false
        }


    }

    @WorkerThread

    private fun addSongs(songs: List<Song>): Boolean {
        /*val contentValues = Array(songs.size) { ContentValues() }
        songs.forEachIndexed { index, song ->
            val value = contentValues[index]
            value.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, initiallySelectedItems.size + 1)
            value.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, song.id)
        }
        val addedRows = getApplication<Application>().contentResolver?.bulkInsert(playlistSongsUri, contentValues) ?: 0
        return addedRows > 0*/
        return try {
            songs.forEach {
                viewModelScope.launch(Dispatchers.IO) {
                    val playlistItem: PlaylistItem = it.toPlaylistItem()
                    playlistItem.id = playlistItem.id+"-"+playlist.id
                    playlistItem.playlistId = playlist.id
                    playlistItemsRepository.insert(playlistItem)
                }
            }
            true
        } catch (e: Exception) {
            Toast.makeText(application.applicationContext, e.message, Toast.LENGTH_LONG).show()
            false
        }
    }

}
