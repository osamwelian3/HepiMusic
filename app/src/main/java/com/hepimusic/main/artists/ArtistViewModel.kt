package com.hepimusic.main.artists

import android.app.Application
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.common.view.BaseMediaStoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor( val application: Application) : BaseMediaStoreViewModel<Artist>(application) {
    override val repository: MediaStoreRepository<Artist>
        get() = ArtistsRepository(application, browser)

    override val parentId: String
        get() = "[artistID]"


    /*override var projection: Array<String>? = baseArtistProjection*/
}


/*
val baseArtistProjection = arrayOf(
    MediaStore.Audio.Artists.ARTIST,
    MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
    MediaStore.Audio.Artists._ID
)

val baseArtistUri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI*/
