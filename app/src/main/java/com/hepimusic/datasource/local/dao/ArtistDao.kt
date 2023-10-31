package com.hepimusic.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.hepimusic.datasource.local.entities.AlbumEntity
import com.hepimusic.datasource.local.entities.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Upsert
    fun upsertAllArtists(artists: List<ArtistEntity>)

    @Query("SELECT * FROM artistentity")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("DELETE FROM artistentity")
    fun clearAllArtists()
}