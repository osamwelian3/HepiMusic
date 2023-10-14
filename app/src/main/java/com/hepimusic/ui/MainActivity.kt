package com.hepimusic.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hepimusic.R
import com.hepimusic.databinding.ActivityMainBinding
import com.hepimusic.main.explore.ExploreViewModel
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.playback.PlaybackViewModel
import com.hepimusic.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var songViewModel: SongViewModel

    val playbackViewModel: PlaybackViewModel by viewModels()
    lateinit var songsViewModel: SongsViewModel
    val exploreViewModel: ExploreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsViewModel = ViewModelProvider(this).get(SongsViewModel::class.java)
        songViewModel = ViewModelProvider(this).get(SongViewModel::class.java)

        if ((songsViewModel.items.value?.size ?: 0) == 0) {
            if ((songsViewModel.items.value?.size ?: 0) == 0) {
                suspend fun load() {
                    if (songsViewModel.isBrowserInitialized()) {
                        songsViewModel.loadData("[allSongsID]")
                    } else {
                        delay(2000)
                        Log.e("RETRY", "RETRY")
                        load()
                    }
                }
                songsViewModel.also {
                    it.viewModelScope.launch {
                        load()
                    }
                }
            }
            songsViewModel.isBrowserConnected.observe(this) { connected ->
                if (connected) {
                    songsViewModel.init()
                    songsViewModel.items.observe(this) { list ->
                        if (list.isNotEmpty()) {

                        }
                    }
                }
            }
        }


//        exploreViewModel.loadData()

        playbackViewModel.init()
//        playbackViewModel.loadInitialData()
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()
        playbackViewModel.isBrowserConnected.observe(this){ connected ->
            if (connected){
                playbackViewModel.init()
                playbackViewModel.loadInitialData()
            }
        }
        songsViewModel.isBrowserConnected.observe(this){ connected ->
            if (connected) {
                songsViewModel.also {
                    it.viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            songsViewModel.loadData("[allSongsID]")
                        }
                    }
                }
            }
        }
        /*songViewModel.mediaItemList.observe(this) { mediaItemList ->
            Log.e("VIEWMODEL ", "mediaItemList count: " + mediaItemList.size)
            mediaItemList.map {
                Log.e(
                    "VIEWMODEL WORKING",
                    "key: " + it.mediaId + ", name: " + it.mediaMetadata.title + ", uri: " + it.requestMetadata.mediaUri
                )
            }

        }*/
    }

    override fun onResume() {
        super.onResume()
        /*playbackViewModel.isBrowserConnected.observe(this){ connected ->
            if (connected){
                playbackViewModel.init()
                playbackViewModel.loadInitialData()
            }
        }
        songsViewModel.isBrowserConnected.observe(this){ connected ->
            if (connected) {
                songsViewModel.loadData("[allSongsID]")
            }
        }*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


}


