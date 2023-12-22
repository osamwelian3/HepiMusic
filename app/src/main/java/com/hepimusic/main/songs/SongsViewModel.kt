package com.hepimusic.main.songs

import android.app.Application
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class SongsViewModel @Inject constructor(val application: Application): BaseMediaStoreViewModel<Song>(application) {

    override val parentId: String
        get() = "[allSongsID]"

    override val repository: MediaStoreRepository<Song>
        get() = SongsRepository(application, liveBrowser)

}