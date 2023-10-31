package com.hepimusic.main.common.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.google.common.util.concurrent.MoreExecutors
import com.hepimusic.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log

abstract class BaseMediaStoreRepository(private val application: Application, val browser: MediaBrowser) {

    val preferences: SharedPreferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)

    private val job = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + job)

    @WorkerThread
    suspend fun<T> loadData(parentId: String = "[albumID]", transform: (data: MediaItem) -> T): List<T> {
        val results = mutableListOf<T>()
        val data = query(parentId)/*if (Constants.LAST_PARENT_ID != "") Constants.LAST_PARENT_ID else Constants.SONGS_ROOT*/
        data.map {
            results.add(transform(it))
        }
        return results
    }

    suspend fun query(parentId: String = "[albumID]"): List<MediaItem> = suspendCoroutine { continuation ->
        val list = mutableListOf<MediaItem>()

        backgroundScope.launch{
            val children = withContext(Dispatchers.Main) {
                val children = browser.getChildren(parentId, 0, Int.MAX_VALUE, null)
                children
            }
            children.addListener({
                backgroundScope.launch {
                    val result = children.get()
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
            }, application.mainExecutor)
        }

//        return list
    }

}