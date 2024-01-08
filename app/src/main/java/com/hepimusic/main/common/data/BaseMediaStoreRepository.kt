package com.hepimusic.main.common.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaBrowser
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.generated.model.Album
import com.amplifyframework.datastore.generated.model.Category
import com.amplifyframework.datastore.generated.model.Creator
import com.amplifyframework.datastore.generated.model.Song
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.models.mappers.toArtist
import com.hepimusic.models.mappers.toGenre
import com.hepimusic.models.mappers.toLAlbum
import com.hepimusic.models.mappers.toMediaItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class BaseMediaStoreRepository(private val application: Application, val liveBrowser: LiveData<MediaBrowser>) {

    val preferences: SharedPreferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)

    private val job = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + job)
    private val foregroundScope = CoroutineScope(Dispatchers.Main + job)
    lateinit var browser: MediaBrowser

    @WorkerThread
    suspend fun<T> loadData(parentId: String = "[albumID]", transform: (data: MediaItem) -> T): List<T> = suspendCoroutine { continuation ->
        /*if (liveBrowser.value != null) {
            browser = liveBrowser.value!!

            backgroundScope.launch {
                val results = mutableListOf<T>()
                val data = query(parentId)*//*if (Constants.LAST_PARENT_ID != "") Constants.LAST_PARENT_ID else Constants.SONGS_ROOT*//*
                data.map {
                    results.add(transform(it))
                }
                continuation.resume(results)
            }

        } else {
            foregroundScope.launch {
                liveBrowser.observeForever {
                    if (it.isConnected) {
                        browser = it

                        backgroundScope.launch {
                            val results = mutableListOf<T>()
                            val data = query(parentId)*//*if (Constants.LAST_PARENT_ID != "") Constants.LAST_PARENT_ID else Constants.SONGS_ROOT*//*
                            data.map {
                                results.add(transform(it))
                            }
                            continuation.resume(results)
                        }
                    }
                }
            }
        }*/

        if (parentId.contains("[allSongsID]")) {
            getSongs {
                backgroundScope.launch {
                    val results = mutableListOf<T>()
                    val data = it
                    data.map {
                        results.add(transform(it))
                    }
                    continuation.resume(results)
                }
            }
        }
        if (parentId.contains("[item]")) {
            getSong(parentId.replace("[item]", "")) {
                continuation.resume(listOf(transform(it)))
            }
        }
        if (parentId.contains("[albumID]")) {
            getAlbums {
                backgroundScope.launch {
                    val results = mutableListOf<T>()
                    val data = it
                    data.map {
                        results.add(transform(it))
                    }
                    continuation.resume(results)
                }
            }
        }
        if (parentId.contains("[album]")) {
            getAlbum(parentId.replace("[album]", "")) {
                continuation.resume(listOf(transform(it)))
            }
        }
        if (parentId.contains("[artistID]")) {
            getArtists {
                backgroundScope.launch {
                    val results = mutableListOf<T>()
                    val data = it
                    data.map {
                        results.add(transform(it))
                    }
                    continuation.resume(results)
                }
            }
        }
        if (parentId.contains("[artist]")) {
            getArtist(parentId.replace("[artist]", "")) {
                continuation.resume(listOf(transform(it)))
            }
        }
        if (parentId.contains("[categoryID]")) {
            getCategories {
                backgroundScope.launch {
                    val results = mutableListOf<T>()
                    val data = it
                    data.map {
                        results.add(transform(it))
                    }
                    continuation.resume(results)
                }
            }
        }
        if (parentId.contains("[category]")) {
            getCategory(parentId.replace("[category]", "")) {
                continuation.resume(listOf(transform(it)))
            }
        }

    }

    /*suspend fun query(parentId: String = "[albumID]"): List<MediaItem> = suspendCoroutine { continuation ->
        val list = mutableListOf<MediaItem>()

        backgroundScope.launch{
            val children = *//*browser.getChildren(parentId, 0, Int.MAX_VALUE, null)*//*withContext(Dispatchers.Main) {
                val children = browser.getChildren(parentId, 0, Int.MAX_VALUE, null)
                children
            }

            children.get()

            children.addListener({
                backgroundScope.launch {
                    val result = children.get()!! *//*withContext(Dispatchers.IO) {
                        children.get()
                    }*//*
                    result.value?.map {
                        Log.e("BASEMEDIASTOREREPOSITORY", "query: mediaItem key: "+ it.mediaId +" title: "+it.mediaMetadata.title.toString()+" DiscCount: "+it.mediaMetadata.totalDiscCount+" tracks: "+it.mediaMetadata.totalTrackCount )
                        if (it.mediaId.contains("[album]")) {
                            val itCount = withContext(Dispatchers.Main) {
                                val itCount = browser.getChildren(it.mediaId, 0, Int.MAX_VALUE, null)
                                itCount
                            }
                            val result2 = itCount.get()
                            var artist: CharSequence? = null
                            if (result2.value != null) {
                                for (item in result2.value!!) {
                                    if (item.mediaMetadata.artist != null) {
                                        artist = item.mediaMetadata.artist
                                    }
                                }
                            }
                            val data = it.mediaMetadata.buildUpon()
                                .setTotalTrackCount(result2.value?.size)
                                .setArtist(artist)
                                .build()
                            val item = it.buildUpon()
                                .setMediaMetadata(data)
                                .build()
                            list.add(item)
                        } else if (it.mediaId.contains("[artist]")) {
                            val itCount = withContext(Dispatchers.Main) {
                                val itCount = browser.getChildren(it.mediaId, 0, Int.MAX_VALUE, null)
                                itCount
                            }
                            val result2 = itCount.get()
                            val discCount = result2.value?.distinctBy { mediaItem -> mediaItem.mediaMetadata.albumTitle }
                                ?.count()

                            val data = it.mediaMetadata.buildUpon()
                                .setTotalTrackCount(result2.value?.size)
                                .setTotalDiscCount(discCount)
                                .build()
                            val item = it.buildUpon()
                                .setMediaMetadata(data)
                                .build()
                            list.add(item)
                        } else {
                            list.add(it)
                        }
                    }
                    continuation.resume(list)
                }
//            preferences.edit().putString(Constants.LAST_PARENT_ID, parentId).apply()
            }, *//*ContextCompat.getMainExecutor(application)*//*application.mainExecutor)
        }

//        return list
    }*/

    suspend fun query(parentId: String = "[albumID]"): List<MediaItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItem>()
        val albums = MediaItemTree.albums
        val artists = MediaItemTree.artists
        val songs = MediaItemTree.songs

        // Fetch children on a background thread
        val children = withContext(Dispatchers.Main) {
            browser.getChildren(parentId, 0, Int.MAX_VALUE, null)
        }
        val childrenResult = withContext(Dispatchers.IO) {
            children.get()
        }

        // Process the result on the background thread
        childrenResult.value?.forEach { mediaItem ->
            when {
                mediaItem.mediaId.contains("[album]") -> {

                    val itCnt = withContext(Dispatchers.Main) {
                        browser.getChildren(mediaItem.mediaId, 0, Int.MAX_VALUE, null)
                    }
                    val itCount = withContext(Dispatchers.IO) {
                        itCnt.get()
                    }
                    val artist = itCount.value?.firstNotNullOfOrNull { it.mediaMetadata.artist }

                    val data = mediaItem.mediaMetadata.buildUpon()
                        .setTotalTrackCount(itCount.value?.size)
                        .setArtist(artist)
                        .build()

                    val uri = songs.find { it.key == mediaItem.mediaId.replace("[item]", "") }?.fileKey
                    val item = mediaItem.buildUpon()
                        .setUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$uri"))
                        .setMediaMetadata(data)
                        .build()

                    list.add(item)
                }
                mediaItem.mediaId.contains("[artist]") -> {
                    val itCnt = withContext(Dispatchers.Main) {
                        browser.getChildren(mediaItem.mediaId, 0, Int.MAX_VALUE, null)
                    }
                    val itCount = withContext(Dispatchers.IO) {
                        itCnt.get()
                    }
                    val discCount = itCount.value?.distinctBy { it.mediaMetadata.albumTitle }?.count()

                    val data = mediaItem.mediaMetadata.buildUpon()
                        .setTotalTrackCount(itCount.value?.size)
                        .setTotalDiscCount(discCount)
                        .build()

                    val uri = songs.find { it.key == mediaItem.mediaId.replace("[item]", "") }?.fileKey
                    val item = mediaItem.buildUpon()
                        .setUri(Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$uri"))
                        .setMediaMetadata(data)
                        .build()

                    list.add(item)
                }
                else -> {
                    val uri = songs.find { it.key == mediaItem.mediaId.replace("[item]", "") }?.fileKey
                    val item = MediaItemTree.buildMediaItem(
                        mediaItem.mediaMetadata.title as String,
                        mediaItem.mediaId,
                        mediaItem.mediaMetadata.isPlayable == true,
                        mediaItem.mediaMetadata.isBrowsable == true,
                        mediaItem.mediaMetadata.mediaType ?: MediaMetadata.MEDIA_TYPE_MUSIC,
                        mutableListOf(),
                        mediaItem.mediaMetadata.albumTitle as String?,
                        mediaItem.mediaMetadata.artist as String?,
                        mediaItem.mediaMetadata.genre as String?,
                        Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/$uri"),
                        mediaItem.mediaMetadata.artworkUri,
                        mediaItem.mediaMetadata.releaseYear,
                        mediaItem.mediaMetadata.releaseMonth,
                        mediaItem.mediaMetadata.releaseDay,
                        mediaItem.mediaMetadata.totalTrackCount,
                        mediaItem.mediaMetadata.totalDiscCount
                    )

//                    Log.e("URI", item.localConfiguration?.uri.toString())
                    list.add(item)
                }
            }
        }

        list
    }

    fun getSongs(onSuccess: (List<MediaItem>) -> Unit) {
        Amplify.DataStore.query(
            Song::class.java,
            {
                val temp = mutableListOf<MediaItem>()
                while (it.hasNext()) {
                    temp.add(it.next().toMediaItem())
                }
                onSuccess.invoke(temp)
            },
            {

            }
        )
    }

    fun getSong(key: String, onSuccess: (MediaItem) -> Unit) {
        Amplify.DataStore.query(
            Song::class.java,
            Where.identifier(Song::class.java, key),
            {
                while (it.hasNext()) {
                    onSuccess.invoke(it.next().toMediaItem())
                }
            },
            {

            }
        )
    }

    fun getAlbums(onSuccess: (List<MediaItem>) -> Unit) {
        Amplify.DataStore.query(
            Album::class.java,
            {
                val temp = mutableListOf<MediaItem>()
                while (it.hasNext()) {
                    temp.add(it.next().toLAlbum().toMediaItem())
                }
                onSuccess.invoke(temp)
            },
            {

            }
        )
    }

    fun getAlbum(key: String, onSuccess: (MediaItem) -> Unit) {
        Amplify.DataStore.query(
            Album::class.java,
            Where.matches(Album.NAME.contains(key.trim()).or(Album.NAME.eq(key)).or(Creator.KEY.eq(key))),
            {
                while (it.hasNext()) {
                    onSuccess.invoke(it.next().toLAlbum().toMediaItem())
                }
            },
            {

            }
        )
    }

    fun getArtists(onSuccess: (List<MediaItem>) -> Unit) {
        Amplify.DataStore.query(
            Creator::class.java,
            {
                val temp = mutableListOf<MediaItem>()
                while (it.hasNext()) {
                    temp.add(it.next().toArtist().toMediaItem())
                }
                onSuccess.invoke(temp)
            },
            {

            }
        )
    }

    fun getArtist(key: String, onSuccess: (MediaItem) -> Unit) {
        Amplify.DataStore.query(
            Creator::class.java,
            Where.matches(Creator.NAME.contains(key.trim()).or(Creator.NAME.eq(key).or(Creator.KEY.eq(key)))),
            {
                while (it.hasNext()) {
                    onSuccess.invoke(it.next().toArtist().toMediaItem())
                }
            },
            {

            }
        )
    }

    fun getCategories(onSuccess: (List<MediaItem>) -> Unit) {
        Amplify.DataStore.query(
            Category::class.java,
            {
                val temp = mutableListOf<MediaItem>()
                while (it.hasNext()) {
                    temp.add(it.next().toGenre().toMediaItem())
                }
                onSuccess.invoke(temp)
            },
            {

            }
        )
    }

    fun getCategory(key: String, onSuccess: (MediaItem) -> Unit) {
        Amplify.DataStore.query(
            Category::class.java,
            Where.matches(Category.NAME.contains(key.trim()).or(Category.NAME.eq(key).or(Category.KEY.eq(key)))),
            {
                while (it.hasNext()) {
                    onSuccess.invoke(it.next().toGenre().toMediaItem())
                }
            },
            {

            }
        )
    }

}