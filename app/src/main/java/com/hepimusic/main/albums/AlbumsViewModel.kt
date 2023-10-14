package com.hepimusic.main.albums

import android.app.Application
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.datasource.repositories.SongRepository
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
open class AlbumsViewModel @Inject constructor(
    val application: Application
    /*val browser: MediaBrowser*/
): BaseMediaStoreViewModel<Album>(application) {

    override val parentId: String
        get() = "[albumID]"

    final override val repository: MediaStoreRepository<Album>
        get() = AlbumsRepository(application, browser)

}