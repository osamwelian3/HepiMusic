package com.hepimusic.main.playlist

import androidx.lifecycle.LiveData

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    val playlists: LiveData<List<Playlist>> = playlistDao.fetchAll()

    suspend fun insert(playlist: Playlist) = playlistDao.insert(playlist)

    suspend fun deletePlaylist(playlistId: String) = playlistDao.deletePlaylist(playlistId = playlistId)

    suspend fun delete() = playlistDao.delete()

}