package com.hepimusic.main.artists

import android.app.Application
import com.hepimusic.main.albums.Album
import com.hepimusic.main.albums.AlbumsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArtistAlbumsViewModel @Inject constructor(application: Application) : AlbumsViewModel(application) {

    lateinit var artist: Artist
    override fun init(vararg params: Any?) {
        /*uri = MediaStore.Audio.Artists.Albums.getContentUri("external", params[0] as Long)*/
        artist = params[1] as Artist
        super.init()
    }

    /*override fun deliverResult(items: List<Album>) {
        if (::artist.isInitialized) {
            val newItems = items.filter {
                it.artist.trim() == artist.name.trim()
            }
            super.deliverResult(newItems)
        }
    }*/



}