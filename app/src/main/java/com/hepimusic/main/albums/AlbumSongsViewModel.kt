package com.hepimusic.main.albums

import android.app.Application
import com.hepimusic.main.songs.SongsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumSongsViewModel @Inject constructor(application: Application) : SongsViewModel(application) {

    override val parentId: String
        get() = super.parentId

    override fun init(vararg params: Any?) {
        super.init()
    }
}