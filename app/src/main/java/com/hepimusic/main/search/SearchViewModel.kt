package com.hepimusic.main.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaBrowser
import com.hepimusic.main.albums.Album
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.common.event.Event
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import com.hepimusic.main.genres.Genre
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.playlist.PlaylistDatabase
import com.hepimusic.main.playlist.PlaylistItemsDatabase
import com.hepimusic.main.playlist.PlaylistItemsRepository
import com.hepimusic.main.playlist.PlaylistRepository
import com.hepimusic.main.songs.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(val application: Application, val playlistDatabase: PlaylistDatabase, val playlistItemsDatabase: PlaylistItemsDatabase) : ViewModel() {

    private val _songs = MutableLiveData<List<Song>>()
    val songsResults: LiveData<List<Song>> get() = _songs

    private val _albums = MutableLiveData<List<Album>>()
    val albumsResults: LiveData<List<Album>> get() = _albums

    private val _artists = MutableLiveData<List<Artist>>()
    val artistsResults: LiveData<List<Artist>> get() = _artists

    private val _genres = MutableLiveData<List<Genre>>()
    val genresResults: LiveData<List<Genre>> get() = _genres

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlistResults: LiveData<List<Playlist>> get() = _playlists

    private val _resultSize = MutableLiveData<Int>()
    val resultSize: LiveData<Int> get() = _resultSize

    private lateinit var repository: SearchRepository

    private val playlistRepository = PlaylistRepository(playlistDatabase.dao)
    private val playlistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)

    fun init(browser: LiveData<MediaBrowser>) {
        repository = SearchRepository(application, browser, playlistDatabase)
    }


    fun query(query: String, ascend: Boolean) {
        viewModelScope.launch {
            val songs = async { repository.querySongs(query, ascend) }
            val albums = async { repository.queryAlbums(query, ascend) }
            val artists = async { repository.queryArtists(query, ascend) }
            val genres = async { repository.queryGenres(query, ascend) }
            val playlists = async {
                repository.queryPlaylists(query, ascend).apply {
                    this.forEach { it.songsCount = playlistItemsRepository.fetchSongCount(it.id)/*playlistRepository.fetchSongCount(it.id)*/ }
                }
            }

            _songs.value = songs.await()
            _albums.value = albums.await()
            _artists.value = artists.await()
            _genres.value = genres.await()
            _playlists.value = playlists.await()
            val totalSize = (_songs.value?.size ?: 0) + (_albums.value?.size ?: 0) + (_artists.value?.size
                ?: 0) + (_genres.value?.size ?: 0) + (_playlists.value?.size ?: 0)
            _resultSize.postValue(totalSize)
        }
    }


    private val _searchNavigation =
        MutableLiveData<Event<SearchNavigation>>()
    val searchNavigation: LiveData<Event<SearchNavigation>> get() = _searchNavigation

    fun navigateFrmSearchFragment(navigation: SearchNavigation) = _searchNavigation.postValue(Event(navigation))

}