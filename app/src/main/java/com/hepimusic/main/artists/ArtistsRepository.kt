package com.hepimusic.main.artists

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.main.common.data.MediaStoreRepository

class ArtistsRepository(application: Application, browser: LiveData<MediaBrowser>) : MediaStoreRepository<Artist>(application, browser) {
    override fun transform(data: MediaItem): Artist = Artist(data)
}
