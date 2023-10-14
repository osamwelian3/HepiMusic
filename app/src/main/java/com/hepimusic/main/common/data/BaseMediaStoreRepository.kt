package com.hepimusic.main.common.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
        val children = browser.getChildren(parentId, 0, Int.MAX_VALUE, null)

        backgroundScope.launch{
            children.addListener({
                val result = children.get()
                result.value?.map {
                    Log.e("BASEMEDIASTOREREPOSITORY", "query: mediaItem key: "+ it.mediaId +" title: "+it.mediaMetadata.title.toString(), )
                    list.add(it)
                }
//            preferences.edit().putString(Constants.LAST_PARENT_ID, parentId).apply()
                continuation.resume(list)
            }, application.mainExecutor)
        }

//        return list
    }

}