package com.hepimusic.models.mappers

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hepimusic.datasource.local.entities.SongEntity
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.models.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.text.DecimalFormat
import java.util.ArrayList

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

fun com.amplifyframework.datastore.generated.model.Song.toSongEntity(): SongEntity {
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

fun com.amplifyframework.datastore.generated.model.Song.toMediaItem(): MediaItem{
    val albums = MediaItemTree.albums
    val albm = partOf.let {
        albums.find { album -> album.key == it  }?.name
    }
    val creator = selectedCreator?.let {
        Json.parseToJsonElement(it).jsonObject["name"]
    }
    val bundle = Bundle()
    bundle.putStringArrayList("trendingListens", this.trendingListens as ArrayList<String>?)
    bundle.putStringArrayList("listOfUidUpVotes", this.listOfUidUpVotes as ArrayList<String>?)
    bundle.putStringArrayList("listOfUidDownVotes", this.listOfUidDownVotes as ArrayList<String>?)
    val data = MediaMetadata.Builder()
        .setTitle(name)
        .setSubtitle(name)
        .setAlbumTitle(albm ?: "Unknown Album")
        .setArtist(creator?.toString()?.removeSurrounding("\"") ?: "Unknown Artist")
        .setArtworkUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$thumbnailKey"))
        .setExtras(bundle)
        .build()
    return MediaItem.Builder()
        .setMediaId(key)
        .setUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$fileKey"))
        .setMediaMetadata(data)
        .build()
}

fun com.amplifyframework.datastore.generated.model.RequestSong.toMediaItem(): MediaItem{
    val albums = MediaItemTree.albums
    val albm = partOf.let {
        albums.find { album -> album.key == it  }?.name
    }
    val creator = selectedCreator?.let {
        Json.parseToJsonElement(it).jsonObject["name"]
    }
    val bundle = Bundle()
    bundle.putStringArrayList("trendingListens", this.trendingListens as ArrayList<String>?)
    bundle.putStringArrayList("listOfUidUpVotes", this.listOfUidUpVotes as ArrayList<String>?)
    bundle.putStringArrayList("listOfUidDownVotes", this.listOfUidDownVotes as ArrayList<String>?)
    val data = MediaMetadata.Builder()
        .setTitle(name)
        .setSubtitle(name)
        .setAlbumTitle(albm ?: "Unknown Album")
        .setArtist(creator?.toString()?.removeSurrounding("\"") ?: "Unknown Artist")
        .setArtworkUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$thumbnailKey"))
        .setExtras(bundle)
        .build()
    return MediaItem.Builder()
        .setMediaId(key)
        .setUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$fileKey"))
        .setMediaMetadata(data)
        .build()
}

fun com.amplifyframework.datastore.generated.model.Song.toSong(): com.hepimusic.main.songs.Song {
    return com.hepimusic.main.songs.Song(
        this.toMediaItem()
    )
}

fun Int.toStreamCount(): String {
    val number = this
    val decimalFormat = DecimalFormat("#.#")
    decimalFormat.maximumFractionDigits = 1
    return when {
        number < 1000 -> number.toString()
        number in 1000 .. 999_999 -> {
            val decimalNumber: Double = number.toDouble() / 1000
            decimalFormat.format(decimalNumber) + "K"
        }
        else -> decimalFormat.format(number.toDouble() / 1_000_000) + "M"
    }
}
