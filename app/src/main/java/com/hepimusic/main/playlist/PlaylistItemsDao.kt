package com.hepimusic.main.playlist

import com.hepimusic.main.explore.RecentlyPlayed
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hepimusic.common.Constants

@Dao
interface PlaylistItemsDao {

    @Query("SELECT * FROM playlist_items_table ORDER BY entryDate DESC")
    fun fetchAll(): LiveData<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items_table WHERE playlistId = :playlistId ORDER BY entryDate DESC")
    fun fetchAllFromId(playlistId: String): LiveData<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items_table ORDER BY entryDate DESC LIMIT 1")
    fun fetchFirst(): PlaylistItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlistItem: PlaylistItem)

    @Query("DELETE FROM playlist_items_table where id = :id")
    fun deleteItem(id: String)

    @Query("DELETE FROM playlist_items_table")
    fun deleteAll()
}