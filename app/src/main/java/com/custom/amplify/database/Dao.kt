package com.custom.amplify.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.custom.amplify.data.AlbumEntity
import com.custom.amplify.data.CategoryEntity
import com.custom.amplify.data.CreatorEntity
import com.custom.amplify.data.ProfileEntity
import com.custom.amplify.data.RequestPlayerEntity
import com.custom.amplify.data.RequestPlaylistEntity
import com.custom.amplify.data.RequestSongEntity
import com.custom.amplify.data.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    // SongEntity
    @Upsert
    fun upsertAllSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addSong(song: SongEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSong(song: SongEntity)

    @Query("SELECT * FROM SongTable")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("DELETE FROM SongTable WHERE `key`=:songKey")
    fun deleteSong(songKey: String)

    @Query("DELETE FROM SongTable")
    fun clearAllSongs()

    // AlbumEntity
    @Upsert
    fun upsertAllAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addAlbum(album: AlbumEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlbum(album: AlbumEntity)

    @Query("SELECT * FROM AlbumTable")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("DELETE FROM AlbumTable WHERE `key`=:albumKey")
    fun deleteAlbum(albumKey: String)

    @Query("DELETE FROM AlbumTable")
    fun clearAllAlbums()

    // CreatorEntity
    @Upsert
    fun upsertAllCreators(creators: List<CreatorEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addCreator(creator: CreatorEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCreator(creator: CreatorEntity)

    @Query("SELECT * FROM CreatorTable")
    fun getAllCreators(): Flow<List<CreatorEntity>>

    @Query("DELETE FROM CreatorTable WHERE `key`=:creatorKey")
    fun deleteCreator(creatorKey: String)

    @Query("DELETE FROM CreatorTable")
    fun clearAllCreators()

    // CategoryEntity
    @Upsert
    fun upsertAllCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addCategory(category: CategoryEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCategory(category: CategoryEntity)

    @Query("SELECT * FROM CategoryTable")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("DELETE FROM CategoryTable WHERE `key`=:categoryKey")
    fun deleteCategory(categoryKey: String)

    @Query("DELETE FROM CategoryTable")
    fun clearAllCategories()

    // ProfileEntity
    @Upsert
    fun upsertAllProfiles(profiles: List<ProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addProfile(profile: ProfileEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM ProfileTable")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("DELETE FROM ProfileTable WHERE `key`=:profileKey")
    fun deleteProfile(profileKey: String)

    @Query("DELETE FROM ProfileTable")
    fun clearAllProfiles()

    // RequestSongEntity
    @Upsert
    fun upsertAllRequestSongs(songs: List<RequestSongEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addRequestSong(song: RequestSongEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateRequestSong(song: RequestSongEntity)

    @Query("SELECT * FROM RequestSongTable")
    fun getAllRequestSongs(): Flow<List<RequestSongEntity>>

    @Query("DELETE FROM RequestSongTable WHERE `key`=:requestSongKey")
    fun deleteRequestSong(requestSongKey: String)

    @Query("DELETE FROM RequestSongTable")
    fun clearAllRequestSongs()

    // RequestPlaylistEntity
    @Upsert
    fun upsertAllRequestPlaylists(playlists: List<RequestPlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addRequestPlaylist(playlist: RequestPlaylistEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateRequestPlaylist(playlist: RequestPlaylistEntity)

    @Query("SELECT * FROM RequestPlaylistTable")
    fun getAllRequestPlaylists(): Flow<List<RequestPlaylistEntity>>

    @Query("DELETE FROM RequestPlaylistTable WHERE `key`=:requestPlaylistKey")
    fun deleteRequestPlaylist(requestPlaylistKey: String)

    @Query("DELETE FROM RequestPlaylistTable")
    fun clearAllRequestPlaylists()

    // RequestPlayerEntity
    @Upsert
    fun upsertAllRequestPlayers(players: List<RequestPlayerEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addRequestPlayer(song: RequestPlayerEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateRequestPlayer(song: RequestPlayerEntity)

    @Query("SELECT * FROM RequestPlayerTable")
    fun getAllRequestPlayers(): Flow<List<RequestPlayerEntity>>

    @Query("DELETE FROM RequestPlayerTable WHERE `key`=:requestPlayerKey")
    fun deleteRequestPlayer(requestPlayerKey: String)

    @Query("DELETE FROM RequestPlayerTable")
    fun clearAllRequestPlayers()
}