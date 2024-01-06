package com.hepimusic.main.requests.common

import android.content.Context
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlayerCopy
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestPlaylistCopy
import com.amplifyframework.datastore.generated.model.RequestSong
import com.amplifyframework.datastore.generated.model.Song
import com.google.auth.oauth2.GoogleCredentials
import com.hepimusic.R
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun RequestPlayer.toRequestPlayerCopy(playlists: List<RequestPlaylist>? = null): RequestPlayerCopy {
    return RequestPlayerCopy.builder()
        .key(this.key)
        .name(this.name)
        .desc(this.desc)
        .latitude(this.latitude)
        .longitude(this.longitude)
        .playlists(playlists)
        .followers(this.followers)
        .following(this.following)
        .owners(this.owners)
        .requestPlayer(this)
        .createdDate(createdDate)
        .updatedDate(updatedDate)
        .ownerData(ownerData)
        .build()
}

fun RequestPlayerCopy.toRequestPlayer(): RequestPlayer {
    return this.requestPlayer
}

fun RequestPlaylist.toRequestPlaylistCopy(songs: List<RequestSong>? = null): RequestPlaylistCopy {
    return RequestPlaylistCopy.builder()
        .key(this.key)
        .player(this.player)
        .songs(songs)
        .owners(this.owners)
        .name(this.name)
        .desc(this.desc)
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .requestPlaylist(this)
        .createdDate(createdDate)
        .updatedDate(updatedDate)
        .ownerData(ownerData)
        .build()
}

fun RequestPlaylistCopy.toRequestPlaylist(): RequestPlaylist {
    return this.playlist
}

fun Song.toRequestSong(playlist: RequestPlaylist? = null, created: Temporal.DateTime, updated: Temporal.DateTime, ownerData: String?): RequestSong {
    return RequestSong.builder()
        .key(this.key)
        .fileUrl(this.fileUrl)
        .fileKey(this.fileKey)
        .name(this.name)
        .selectedCategory(this.selectedCategory)
        .thumbnail(this.thumbnail)
        .thumbnailKey(this.thumbnailKey)
        .playlist(playlist)
        .createdDate(created)
        .updatedDate(updated)
        .ownerData(ownerData)
        .build()
}
