package com.hepimusic.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.hepimusic.datasource.local.entities.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Upsert
    fun upsertAllAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albumentity")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("DELETE FROM albumentity")
    fun clearAllAlbums()
}