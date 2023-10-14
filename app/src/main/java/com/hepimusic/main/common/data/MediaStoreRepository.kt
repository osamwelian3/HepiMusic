package com.hepimusic.main.common.data

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.datasource.repositories.SongRepository

abstract class MediaStoreRepository<T>(application: Application, browser: MediaBrowser): BaseMediaStoreRepository(application, browser) {

    @WorkerThread
    suspend fun loadData(parentId: String = "[albumID]"): List<T> {
        return loadData(parentId) { transform(it) }
    }

    abstract fun transform(data: MediaItem): T
}