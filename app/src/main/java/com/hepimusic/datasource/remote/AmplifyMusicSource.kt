package com.hepimusic.datasource.remote

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.amplifyframework.datastore.generated.model.Album
import com.amplifyframework.datastore.generated.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.hepimusic.datasource.remote.State.*
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class AmplifyMusicSource {

    private var cloudMusicDatabase: CloudMusicDatabase = CloudMusicDatabase()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if(value == STATE_INITIALIZED ||  value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }

    private var songs = emptyList<MediaMetadataCompat>()
    var albums = emptyList<Album>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val allSongs: MutableList<Song> = mutableListOf()
        val allAlbums: MutableList<Album> = mutableListOf()
        cloudMusicDatabase.getAllAlbums().collect {
            it.map {
                allAlbums.add(it)
            }
        }
        cloudMusicDatabase.getAllSongs().collect {
            it.map {
                allSongs.add(it)
            }
        }
        albums = allAlbums
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.selectedCreator?.let { Json.parseToJsonElement(it).jsonObject["name"] }?.toString()?.removeSurrounding("\"") ?: "Unknown Artist")
                .putString(METADATA_KEY_MEDIA_ID, song.key)
                .putString(METADATA_KEY_TITLE, song.name)
                .putString(METADATA_KEY_ALBUM, allAlbums.find { it.key == song.partOf }?.name ?: "Unknown Album")
                .putString(METADATA_KEY_DISPLAY_TITLE, song.name)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, "https://dn1i8z7909ivj.cloudfront.net/public/"+song.thumbnailKey)
                .putString(METADATA_KEY_MEDIA_URI, "https://dn1i8z7909ivj.cloudfront.net/public/"+song.fileKey)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.thumbnailKey)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.name)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.name)
                .build()
        }
        state = STATE_INITIALIZED
    }

    fun asMediaItemList() = songs.map { song ->
        val metadata = MediaMetadata.Builder()
            .setTitle(song.description.title)
            .setArtist(song.getString(METADATA_KEY_ARTIST))
            .setSubtitle(song.description.subtitle)
            .setArtworkUri(song.description.iconUri)
            .setAlbumTitle(song.getString(METADATA_KEY_ALBUM))
            .build()
        MediaItem.Builder()
            .setMediaId(song.description.mediaId!!)
            .setUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setMediaMetadata(metadata)
            .build()
    }

}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}