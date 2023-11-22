package com.hepimusic.main.songs

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import aws.smithy.kotlin.runtime.util.asyncLazy
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.generated.model.Creator
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.data.Model
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Serializable
class Song(
    override val id: String,
    val title: String,
    val titleKey: String,
    val artist: String,
    @Contextual
    val album: Any?,
    @Contextual
    val artWork: Uri?,
    val path: String,
    val duration: Long,
    val number: String?,
    val artPath: String,
    var isCurrent: Boolean = false,
    var selected: Boolean = false,
    var audioId: Long? = null,
): Model(), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Album::class.java.classLoader),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    constructor(data: MediaItem) : this(
        id = data.mediaId,
        title = data.mediaMetadata.title.toString(),
        titleKey = data.mediaMetadata.subtitle.toString(),
        artist = if (data.mediaMetadata.artist.toString() != "" || data.mediaMetadata.artist != null) data.mediaMetadata.artist.toString() else data.mediaMetadata.subtitle.toString(),
        album = data.mediaMetadata.albumTitle.toString(),
        artWork = data.mediaMetadata.artworkUri,
        path = data.localConfiguration?.uri.toString(),
        duration = data.mediaMetadata.extras?.getLong("duration") ?: 0L,
        number = null,
        artPath = data.mediaMetadata.artworkUri.toString(),
        isCurrent = false,
        selected = false,
        audioId = null
    )

    /*init {
        CoroutineScope(Job()+Dispatchers.IO).launch {
            song = getAmplifyModelSong()
        }
    }*/

    constructor(data: MediaItem, audioId: Long?) : this(
        id = data.mediaId,
        title = data.mediaMetadata.title.toString(),
        titleKey = data.mediaMetadata.subtitle.toString(),
        artist = data.mediaMetadata.artist.toString(),
        album = data.mediaMetadata.albumTitle.toString(),
        artWork = data.mediaMetadata.artworkUri,
        path = data.localConfiguration?.uri.toString(),
        duration = data.mediaMetadata.extras?.getLong("duration") ?: 0L,
        artPath = data.mediaMetadata.artworkUri.toString(),
        number = null,
        audioId = audioId
    ) 

    /*suspend fun getAmplifyModelSong(): com.amplifyframework.datastore.generated.model.Song {
        Log.e("Amplify Song from Song", this.toSong().name)
        return this.toSong()
    }*/

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(titleKey)
        parcel.writeString(artist)
        parcel.writeParcelable(artWork, flags)
        parcel.writeString(path)
        parcel.writeLong(duration)
        parcel.writeString(number)
        parcel.writeString(artPath)
        parcel.writeByte(if (isCurrent) 1 else 0)
        parcel.writeByte(if (selected) 1 else 0)
        parcel.writeValue(audioId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}

/*
fun com.amplifyframework.datastore.generated.model.Song.toSong(): Song {
    val subtitleConfigurations: MutableList<MediaItem.SubtitleConfiguration> = mutableListOf()
    val albums = mutableListOf<com.amplifyframework.datastore.generated.model.Album>()
    val artists = mutableListOf<Creator>()
    Amplify.DataStore.query(
        com.amplifyframework.datastore.generated.model.Album::class.java,
        {
            while (it.hasNext()){
                albums.add(it.next())
            }
        },
        {
            Log.e("Amplify Exception", "Error Querying Albums")
        }
    )
    Amplify.DataStore.query(
        Creator::class.java,
        {
            while (it.hasNext()){
                artists.add(it.next())
            }
        },
        {
            Log.e("Amplify Exception", "Error Querying Creators")
        }
    )
    val album = this.partOf.let {
        albums.find { album -> album.key == it  }?.name
    } ?: "Unknown Album"
    val artist = this.selectedCreator?.let {
        Json.parseToJsonElement(it).jsonObject["name"]
    }?.toString()?.removeSurrounding("\"") ?: "Unknown Artist"
    return Song(
        MediaItemTree.buildMediaItem(
            title = name,
            mediaId = "[item]${key}",
            isPlayable = true,
            isBrowsable = false,
            mediaType = MediaMetadata.MEDIA_TYPE_MUSIC,
            subtitleConfigurations,
            album = album,
            artist = artist,
            genre = selectedCategory,
            sourceUri = Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/${fileKey}"),
            imageUri = Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/${thumbnailKey}"),
            year = createdAt?.toDate()?.year,
            month = createdAt?.toDate()?.month,
            day = createdAt?.toDate()?.day,
            trackCount = null
        )
    )
}

suspend fun Song.toSong(): com.amplifyframework.datastore.generated.model.Song {
    Log.e("Amplify Song Get Song ID", this.id+" cleaned ID: "+this.id.replace("[item]", ""))
    val condition = Where.matches(com.amplifyframework.datastore.generated.model.Song.KEY.eq(this.id.replace("[item]", "")))

    return withContext(Dispatchers.IO) {
        var item: com.amplifyframework.datastore.generated.model.Song =
            com.amplifyframework.datastore.generated.model.Song.builder()
                .key("")
                .fileUrl("")
                .fileKey("")
                .name("")
                .selectedCategory("")
                .thumbnail("")
                .thumbnailKey("")
                .build()

        Amplify.DataStore.query(
            com.amplifyframework.datastore.generated.model.Song::class.java,
            condition,
            { item = if (it.hasNext()) it.next() else it.next() },
            { })

        item
    }
}*/
