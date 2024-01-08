package com.hepimusic.models.mappers

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.hepimusic.common.Constants
import com.hepimusic.datasource.local.entities.ArtistEntity
import com.hepimusic.main.artists.Artist
import com.hepimusic.models.Creator

fun Creator.toArtistEntity(): ArtistEntity {
    return ArtistEntity(
        key = key,
        desc = desc,
        facebook = facebook,
        instagram = instagram,
        name = name,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        twitter = twitter,
        youtube = youtube,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun com.amplifyframework.datastore.generated.model.Creator.toArtistEntity(): ArtistEntity {
    return ArtistEntity(
        key = key,
        desc = desc,
        facebook = facebook,
        instagram = instagram,
        name = name,
        thumbnail = thumbnail,
        thumbnailKey = thumbnailKey,
        twitter = twitter,
        youtube = youtube,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}

fun ArtistEntity.toCreator(): Creator {
    return Creator(
        key, desc, facebook, instagram, name, thumbnail, thumbnailKey, twitter, youtube, createdAt, updatedAt
    )
}

fun com.amplifyframework.datastore.generated.model.Creator.toArtist(): Artist {
    return Artist(
        id = "[artist]"+this.name,
        name = this.name,
        artist = this.name+" :: "+this.desc,
        albumArt = Uri.parse(Constants.BASE_URL+this.thumbnailKey),
        year = this.createdAt.toDate().year.toString(),
        tracks = null,
        albumsCount = null,
        key = this.key
    )
}

fun Artist.toMediaItem(): MediaItem {
    val metaData = MediaMetadata.Builder()
        .setTitle(this.name)
        .setDescription(this.artist.split("::")[1].trim())
        .setSubtitle(this.name)
        .setArtist(this.name)
        .setIsBrowsable(true)
        .setArtworkUri(this.albumArt)
        .build()

    return MediaItem.Builder()
        .setMediaMetadata(metaData)
        .setMediaId("[artist]"+this.name)
        .build()
}