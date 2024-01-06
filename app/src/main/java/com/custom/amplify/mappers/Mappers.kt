package com.custom.amplify.mappers

import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Song
import com.custom.amplify.data.SongEntity
import com.hepimusic.datasource.repositories.MediaItemTree

fun Song.toSongEntity(): SongEntity {
    return SongEntity(
        key, fileUrl, fileKey, listens, trendingListens, listOfUidDownVotes, listOfUidUpVotes, name, partOf, selectedCategory, selectedCreator, thumbnail, thumbnailKey, owner, createdAt, updatedAt
    )
}

fun SongEntity.toSong(): Song {
    val song = Song.builder()
        .key(key)
        .fileUrl(fileUrl)
        .fileKey(fileKey)
        .name(name)
        .selectedCategory(selectedCategory)
        .thumbnail(thumbnail)
        .thumbnailKey(thumbnailKey)
        .listens(listens)
        .trendingListens(trendingListens)
        .listOfUidUpVotes(listOfUidUpVotes)
        .listOfUidDownVotes(listOfUidDownVotes)
        .partOf(partOf)
        .selectedCreator(selectedCreator)
        .owner(owner)
        .build()

    return song
}

fun Song.createdAt(): Temporal.DateTime? {
    return MediaItemTree.songs.find { it.key == this.key }?.createdAt
}

fun Song.updatedAt(songEntity: SongEntity): Temporal.DateTime {
    return songEntity.updatedAt
}