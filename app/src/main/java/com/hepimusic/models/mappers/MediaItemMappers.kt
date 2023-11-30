package com.hepimusic.models.mappers

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hepimusic.main.explore.RecentlyPlayed
import com.hepimusic.main.playlist.PlaylistItem
import com.hepimusic.playback.MediaItemData

fun RecentlyPlayed.toMediaItem(): MediaItem {
    val data = MediaMetadata.Builder()
        .setTitle(title)
        .setSubtitle(title)
        .setAlbumTitle(album)
        .setArtist(artist)
        .setAlbumArtist(artist)
        .setReleaseYear(entryDate.toInt())
        .setArtworkUri(Uri.parse(artWorkUri))
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(mediaUri)
        .setMediaMetadata(data)
        .build()
}

fun PlaylistItem.toMediaItem(): MediaItem {
    val data = MediaMetadata.Builder()
        .setTitle(title)
        .setSubtitle(title)
        .setAlbumTitle(album)
        .setArtist(artist)
        .setAlbumArtist(artist)
        .setReleaseYear(entryDate.toInt())
        .setArtworkUri(Uri.parse(artWorkUri))
        .setExtras(Bundle().apply { putString("playlistId", playlistId) })
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(mediaUri)
        .setMediaMetadata(data)
        .build()
}

fun MediaItem.toMediaItemData(isPlaying: Boolean, isBuffering: Boolean): MediaItemData {
    return MediaItemData(this, isPlaying, isBuffering)
}

fun MediaItemData.toMediaItem(): MediaItem {
    val data = MediaMetadata.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(description)
        .setAlbumTitle(description)
        .setArtist(subtitle)
        .setAlbumArtist(subtitle)
        .setArtworkUri(albumArtUri)
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setMediaMetadata(data)
        .build()
}