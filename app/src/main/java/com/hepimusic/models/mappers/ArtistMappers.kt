package com.hepimusic.models.mappers

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