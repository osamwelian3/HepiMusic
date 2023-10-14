package com.hepimusic.models.mappers

import android.net.Uri
import androidx.media3.common.MediaItem
import com.hepimusic.common.Constants
import com.hepimusic.datasource.local.entities.AlbumEntity
import com.hepimusic.main.songs.Song
import com.hepimusic.models.Album

fun Album.toAlbumEntity(): AlbumEntity {
    return AlbumEntity(
        key = key,
        name = name,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AlbumEntity.toAlbum(): Album {
    return Album(
        key = key,
        name = name,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AlbumEntity.toAlbumModel(): com.hepimusic.main.albums.Album {
    return com.hepimusic.main.albums.Album(
        id = key,
        name = name,
        artist = name,
        albumArt = Uri.parse(Constants.BASE_URL+thumbnailKey),
        tracks = null,
        year = createdAt?.toDate().toString(),
        key = key
    )
}

fun MediaItem.toAlbum(): com.hepimusic.main.albums.Album {
    return com.hepimusic.main.albums.Album(
        this
    )
}

fun MediaItem.toSong(): Song {
    return Song(this)
}