package com.hepimusic.main.search

import android.app.Application
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.main.albums.Album
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.common.data.BaseMediaStoreRepository
import com.hepimusic.main.genres.Genre
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.playlist.PlaylistDatabase
import com.hepimusic.main.playlist.PlaylistRepository
import com.hepimusic.main.songs.Song
import com.hepimusic.models.mappers.toAlbum
import com.hepimusic.models.mappers.toArtist
import com.hepimusic.models.mappers.toGenre
import com.hepimusic.models.mappers.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SearchRepository(application: Application, browser: LiveData<MediaBrowser>, val playlistsDatabase: PlaylistDatabase) : BaseMediaStoreRepository(application, browser) {

    @WorkerThread
    suspend fun querySongs(query: String, ascend: Boolean): List<Song> {
        /*val selection = "$basicSongsSelection AND ${MediaStore.Audio.Media.TITLE} LIKE ?"
        val selectionArgs = arrayOf(basicSongsSelectionArg, "%$query%")
        val order = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(baseSongUri, baseSongsProjection, selection, selectionArgs, order, ::Song)*/
        fun transform(data: MediaItem): Song {
            return try {
                val castData = data.toSong()
                castData
            } catch (e: Exception){
                e.printStackTrace()
                Song("", "", "", "", null, null, "", 0, "", "", false, false)
            }

        }
        return if (ascend) loadData(parentId = "[allSongsID]") { transform(it) }.filter { it.title.contains(query, true) }.sortedBy { it.title } else loadData(parentId = "[allSongsID]") { transform(it) }.filter { it.title.contains(query, true) }.sortedBy { it.title }.reversed()
    }

    @WorkerThread
    suspend fun queryAlbums(query: String, ascend: Boolean): List<Album> {
        /*val selection = "${MediaStore.Audio.Media.ALBUM} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val order = "${MediaStore.Audio.Media.ALBUM} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(baseAlbumUri, baseAlbumProjection, selection, selectionArgs, order, ::Album)*/
        fun transform(data: MediaItem): Album {
            return try {
                val castData = data.toAlbum()
                castData
            } catch (e: Exception){
                e.printStackTrace()
                Album(null, "", "", Uri.parse(""), null, null, "")
            }

        }
        return if (ascend) loadData(parentId = "[albumID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name } else loadData(parentId = "[albumID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name }.reversed()
    }

    @WorkerThread
    suspend fun queryArtists(query: String, ascend: Boolean): List<Artist> {
        /*val selection = "${MediaStore.Audio.Media.ARTIST} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val order = "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(baseArtistUri, baseArtistProjection, selection, selectionArgs, order, ::Artist)*/
        fun transform(data: MediaItem): Artist {
            return try {
                val castData = data.toArtist()
                castData
            } catch (e: Exception){
                e.printStackTrace()
                Artist(null, "", "", Uri.parse(""), null, null, null, "")
            }

        }
        return if (ascend) loadData(parentId = "[artistID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name } else loadData(parentId = "[artistID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name }.reversed()
    }

    @WorkerThread
    suspend fun queryCategories(query: String, ascend: Boolean): List<Genre> {
        /*val selection = "${MediaStore.Audio.Media.ARTIST} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val order = "${MediaStore.Audio.Media.ARTIST} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(baseArtistUri, baseArtistProjection, selection, selectionArgs, order, ::Artist)*/
        fun transform(data: MediaItem): Genre {
            return try {
                val castData = data.toGenre()
                castData
            } catch (e: Exception){
                e.printStackTrace()
                Genre("", "", "")
            }

        }
        return if (ascend) loadData(parentId = "[categoryID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name } else loadData(parentId = "[categoryID]") { transform(it) }.filter { it.name.contains(query, true) }.sortedBy { it.name }.reversed()
    }

    @WorkerThread
    fun queryGenres(query: String, ascend: Boolean): List<Genre> {
        /*val selection = "${MediaStore.Audio.Genres.NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val order = "${MediaStore.Audio.Genres.NAME} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(baseGenreUri, baseGenreProjection, selection, selectionArgs, order, ::Genre)*/
        return emptyList()
    }


    @WorkerThread
    suspend fun queryPlaylists(query: String, ascend: Boolean): List<Playlist> = suspendCoroutine { continuation ->
        /*val selection = "${MediaStore.Audio.Playlists.NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
        val order = "${MediaStore.Audio.Playlists.NAME} COLLATE NOCASE ${if (ascend) "ASC" else "DESC"}"
        return loadData(basePlaylistUri, basePlaylistProjection, selection, selectionArgs, order, ::Playlist)*/
        val playlistRepository = PlaylistRepository(playlistsDatabase.dao)

        playlistRepository.playlists.observeForever { playlists ->
            if (playlists.isNotEmpty()) continuation.resume(if (ascend) playlists.filter { it.name.contains(query, true) }.sortedBy { it.name } else playlists.filter { it.name.contains(query, true) }.sortedBy { it.name }.reversed()) else continuation.resume(emptyList<Playlist>())
        }
    }

}
