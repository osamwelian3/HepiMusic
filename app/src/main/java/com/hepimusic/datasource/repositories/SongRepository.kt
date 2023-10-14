package com.hepimusic.datasource.repositories

import androidx.room.withTransaction
import com.hepimusic.common.networkBoundResource
import com.hepimusic.datasource.local.databases.AlbumDatabase
import com.hepimusic.datasource.local.databases.SongDatabase
import com.hepimusic.datasource.remote.CloudMusicDatabase
import com.hepimusic.models.mappers.toAlbumEntity
import com.hepimusic.models.mappers.toSongEntity
import kotlinx.coroutines.delay

class SongRepository(
    musicDatabase: CloudMusicDatabase,
    songDatabase: SongDatabase,
    albumDatabase: AlbumDatabase
) {

    private val songDao = songDatabase.dao
    private val api = musicDatabase
    private val db = songDatabase
    private val albumDb = albumDatabase
    private val albumDao = albumDb.dao

    fun getSongs() = networkBoundResource(
        query = {
            songDao.getAllSongs()
        },
        fetch = {
            delay(2000)
            api.getAllSongs()
        },
        saveFetchResult = {
            db.withTransaction {
                songDao.clearAll()
                songDao.upsertAll(it.map { it.toSongEntity() })
            }
        },
        shouldFetch = {
            it.isEmpty()
        }
    )

    fun getAlbums() = networkBoundResource(
        query = {
            albumDao.getAllAlbums()
        },
        fetch = {
            delay(2000)
            api.getAllAlbums()
        },
        saveFetchResult = {
            albumDb.withTransaction {
                albumDao.clearAllAlbums()
                albumDao.upsertAllAlbums(it.map { it.toAlbumEntity() })
            }
        },
        shouldFetch = {
            it.isEmpty()
        }
    )
}