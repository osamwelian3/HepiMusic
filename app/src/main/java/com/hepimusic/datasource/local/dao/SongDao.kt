package com.hepimusic.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.hepimusic.datasource.local.entities.AlbumEntity
import com.hepimusic.datasource.local.entities.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Upsert
    fun upsertAll(songs: List<SongEntity>)

    @Query("SELECT * FROM songentity")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("DELETE FROM songentity")
    fun clearAll()

}