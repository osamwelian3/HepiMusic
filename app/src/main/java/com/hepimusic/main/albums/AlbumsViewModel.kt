package com.hepimusic.main.albums

import android.app.Application
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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