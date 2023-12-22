package com.hepimusic.main.songs

import android.annotation.SuppressLint
import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.common.Constants
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.models.mappers.toSong

open class SongsRepository (application: Application, browser: LiveData<MediaBrowser>) : MediaStoreRepository<Song>(application, browser) {

    override fun transform(data: MediaItem): Song {
        return try {
            val castData = data.toSong()
            castData
        } catch (e: Exception){
            e.printStackTrace()
            Song("", "", "", "", null, null, "", 0, "", "", false, false)
        }

    }

    @SuppressLint("Range")
    @WorkerThread
    suspend fun fetchMatchingIds(
        /*uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null*/
    ): List<Long> {
        val results = mutableListOf<Long>()
        val cursor = query(/*uri, projection, selection, selectionArgs*/ Constants.SONGS_ROOT)
        var count: Long = 1;
        cursor?.map {
            results.add(count)
            count += 1
        }
        return results
    }
}