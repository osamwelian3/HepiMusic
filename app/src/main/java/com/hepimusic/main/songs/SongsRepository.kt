package com.hepimusic.main.songs

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.temporal.Temporal
import com.hepimusic.common.Constants
import com.hepimusic.datasource.remote.AmplifyMusicSource
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.models.mappers.toAlbum
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong
import com.hepimusic.models.mappers.toSongEntity
import java.util.Date
import javax.inject.Inject

open class SongsRepository (application: Application, browser: MediaBrowser) : MediaStoreRepository<Song>(application, browser) {

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