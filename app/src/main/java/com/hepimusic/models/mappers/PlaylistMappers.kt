package com.hepimusic.models.mappers

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.playlist.PlaylistItem
import com.hepimusic.main.songs.Song

fun PlaylistItem.toSong(): Song {
    val rid = if ("-[playlist]" in id) {
        id.split("-[playlist]")[0]
    } else id
    return Song(
        id = rid,
        title = title,
        titleKey = title,
        artist = artist,
        album = album,
        artWork = Uri.parse(artWorkUri),
        path = mediaUri,
        duration = duration,
        number = null,
        artPath = artWorkUri,
        isCurrent = false,
        selected = false,
        audioId = null
    )
}

fun PlaylistItem.toPlaylist(): Playlist {
    return Playlist(
        id = id,
        name = title,
        modified = entryDate,
        songsCount = 0,
        selected = false,
        playlistImage = artWorkUri
    )
}

fun Song.toPlaylistItem(): PlaylistItem {
    return PlaylistItem(this.toMediaItem())
}

fun Song.toPlaylist(): Playlist {
    return Playlist(this.toMediaItem())
}
