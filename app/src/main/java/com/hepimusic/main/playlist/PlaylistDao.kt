package com.hepimusic.main.playlist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun fetchAll(): LiveData<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlist: Playlist)

    @Query("DELETE FROM playlist WHERE id = :playlistId")
    fun deletePlaylist(playlistId: String)

    @Query("DELETE FROM playlist")
    fun delete()
}