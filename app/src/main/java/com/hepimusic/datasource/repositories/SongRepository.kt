package com.hepimusic.datasource.repositories

import androidx.room.withTransaction
import com.amplifyframework.datastore.generated.model.Album
import com.amplifyframework.datastore.generated.model.Creator
import com.amplifyframework.datastore.generated.model.Song
import com.hepimusic.common.Resource
import com.hepimusic.common.networkBoundResource
import com.hepimusic.datasource.local.databases.AlbumDatabase
import com.hepimusic.datasource.local.databases.ArtistDatabase
import com.hepimusic.datasource.local.databases.SongDatabase
import com.hepimusic.datasource.remote.CloudMusicDatabase
import com.hepimusic.models.mappers.toAlbumEntity
import com.hepimusic.models.mappers.toArtistEntity
import com.hepimusic.models.mappers.toSongEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class SongRepository(
    musicDatabase: CloudMusicDatabase,
    songDatabase: SongDatabase,
    albumDatabase: AlbumDatabase,
    artistDatabase: ArtistDatabase
) {

    private val songDao = songDatabase.dao
    private val api = musicDatabase
    private val db = songDatabase
    private val albumDb = albumDatabase
    private val albumDao = albumDb.dao
    private val artistDB = artistDatabase
    private val artistDao = artistDB.dao

    suspend fun getAllSongs(): Flow<Resource.Success<List<Song>>> {
        return flow {
            api.getAllSongs().collect {
                if (it.isNotEmpty()) {
                    emit(Resource.Success(it))
                }
            }
        }
        /*return flowOf(Resource.Success(api.getAllSongs()))*/
    }

    suspend fun getAllAlbums(): Flow<Resource.Success<List<Album>>> {
        return flow {
            api.getAllAlbums().collect {
                if (it.isNotEmpty()) {
                    emit(Resource.Success(it))
                }
            }
        }
        /*return flowOf(Resource.Success(api.getAllAlbums()))*/
    }
    suspend fun getAllArtists(): Flow<Resource.Success<List<Creator>>> {
        return flow {
            api.getAllArtists().collect {
                if (it.isNotEmpty()) {
                    emit(Resource.Success(it))
                }
            }
        }
        /*return flowOf(Resource.Success(api.getAllArtists()))*/
    }

    /*fun getSongs() = networkBoundResource(
        query = {
            songDao.getAllSongs()
        },
        fetch = {
//            delay(2000)
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
//            delay(2000)
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

    fun getArtists() = networkBoundResource(
        query = {
            artistDao.getAllArtists()
        },
        fetch = {
//            delay(2000)
            api.getAllArtists()
        },
        saveFetchResult = {
            artistDB.withTransaction {
                artistDao.clearAllArtists()
                artistDao.upsertAllArtists(it.map { it.toArtistEntity() })
            }
        },
        shouldFetch = {
            it.isEmpty()
        }
    )*/
}