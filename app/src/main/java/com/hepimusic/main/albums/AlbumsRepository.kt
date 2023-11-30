package com.hepimusic.main.albums

import android.app.Application
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.models.mappers.toAlbum

class AlbumsRepository(application: Application, browser: MediaBrowser): MediaStoreRepository<Album>(application, browser) {

    override fun transform(data: MediaItem): Album {
        return try {
            val castData = data.toAlbum()
            castData
        } catch (e: Exception){
            e.printStackTrace()
            Album(null, "", "", Uri.parse(""), null, null, "")
        }

    }
}