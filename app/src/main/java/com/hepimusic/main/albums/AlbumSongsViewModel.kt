package com.hepimusic.main.albums

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.hepimusic.main.songs.SongsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumSongsViewModel @Inject constructor(application: Application) : SongsViewModel(application) {

    var preferences: SharedPreferences
    init {
        preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)
    }
    override val parentId: String
        get() = "[allSongsID]"

    override fun init(vararg params: Any?) {
        super.init()
    }
}