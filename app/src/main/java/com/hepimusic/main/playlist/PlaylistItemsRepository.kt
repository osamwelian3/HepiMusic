package com.hepimusic.main.playlist

import androidx.lifecycle.LiveData

class PlaylistItemsRepository(private val playlistItemsDao: PlaylistItemsDao, val playlistId: String? = "") {

    val playlistItems: LiveData<List<PlaylistItem>> = if (playlistId.isNullOrEmpty()) playlistItemsDao.fetchAll() else playlistItemsDao.fetchAllFromId(playlistId ?: "")

    suspend fun insert(playlistItem: PlaylistItem) = playlistItemsDao.insert(playlistItem)

    suspend fun deleteItem(id: String) = playlistItemsDao.deleteItem(id)

    suspend fun fetchFirst(): PlaylistItem? = playlistItemsDao.fetchFirst()

    suspend fun deleteAll() = playlistItemsDao.deleteAll()

    fun fetchSongCount(playlistId: String): Int {
        return playlistItems.value?.filter { it.playlistId == playlistId }?.size ?: 0
    }
}