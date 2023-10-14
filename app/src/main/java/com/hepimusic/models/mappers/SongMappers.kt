package com.hepimusic.models.mappers

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hepimusic.datasource.local.entities.SongEntity
import com.hepimusic.models.Song

fun Song.toSongEntity(): SongEntity {
    return SongEntity(
        key = key,
        fileUrl = fileUrl,
        fileKey = fileKey,
        listens = listens,
        trendingListens = trendingListens,
        listOfUidDownVotes = listOfUidDownVotes,
        listOfUidUpVotes = listOfUidUpVotes,
        name = name,
        partOf = partOf,
        selectedCategory = selectedCategory,
        selectedCreator = selectedCreator,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SongEntity.toSong(): Song {
    return Song(
        key = key,
        fileUrl = fileUrl,
        fileKey = fileKey,
        listens = listens,
        trendingListens = trendingListens,
        listOfUidDownVotes = listOfUidDownVotes,
        listOfUidUpVotes = listOfUidUpVotes,
        name = name,
        partOf = partOf,
        selectedCategory = selectedCategory,
        selectedCreator = selectedCreator,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun com.hepimusic.main.songs.Song.toMediaItem(): MediaItem{
    val data = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(artWork)
        .build()
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(Uri.parse(path))
        .setMediaMetadata(data)
        .build()
}