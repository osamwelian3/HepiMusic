package com.hepimusic.ui

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.databinding.ActivityMainBinding
import com.hepimusic.main.albums.AlbumSongsViewModel
import com.hepimusic.main.artists.ArtistAlbumsViewModel
import com.hepimusic.main.artists.ArtistsViewModel
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.explore.ExploreViewModel
import com.hepimusic.main.playlist.AddSongsToPlaylistsViewModel
import com.hepimusic.main.playlist.PlaylistSongsEditorViewModel
import com.hepimusic.main.playlist.PlaylistSongsViewModel
import com.hepimusic.main.playlist.PlaylistViewModel
import com.hepimusic.main.playlist.WritePlaylistViewModel
import com.hepimusic.main.search.SearchViewModel
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.playback.PlaybackViewModel
import com.hepimusic.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var songViewModel: SongViewModel

    lateinit var playbackViewModel: PlaybackViewModel // by viewModels()
    lateinit var songsViewModel: SongsViewModel
    lateinit var exploreViewModel: ExploreViewModel // by viewModels()
    lateinit var albumSongsViewModel: AlbumSongsViewModel
    lateinit var playlistViewModel: PlaylistViewModel
    lateinit var writePlaylistViewModel: WritePlaylistViewModel
    lateinit var playlistSongsViewModel: PlaylistSongsViewModel
    lateinit var addSongsToPlaylistViewModel: AddSongsToPlaylistsViewModel
    lateinit var playlistSongsEditorViewModel: PlaylistSongsEditorViewModel
    lateinit var searchViewModel: SearchViewModel
    lateinit var artistsViewModel: ArtistsViewModel
    lateinit var artistAlbumsViewModel: ArtistAlbumsViewModel
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    var items = emptyList<Song>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playbackViewModel = ViewModelProvider(this)[PlaybackViewModel::class.java]
        songsViewModel = ViewModelProvider(this)[SongsViewModel::class.java]
        exploreViewModel = ViewModelProvider(this)[ExploreViewModel::class.java]
        songViewModel = ViewModelProvider(this)[SongViewModel::class.java]
        albumSongsViewModel = ViewModelProvider(this)[AlbumSongsViewModel::class.java]
        playlistViewModel = ViewModelProvider(this)[PlaylistViewModel::class.java]
        writePlaylistViewModel = ViewModelProvider(this)[WritePlaylistViewModel::class.java]
        playlistSongsViewModel = ViewModelProvider(this)[PlaylistSongsViewModel::class.java]
        addSongsToPlaylistViewModel = ViewModelProvider(this)[AddSongsToPlaylistsViewModel::class.java]
        playlistSongsEditorViewModel = ViewModelProvider(this)[PlaylistSongsEditorViewModel::class.java]
        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        artistsViewModel = ViewModelProvider(this)[ArtistsViewModel::class.java]
        artistAlbumsViewModel = ViewModelProvider(this)[ArtistAlbumsViewModel::class.java]

        /*songsViewModel.also {
            it.viewModelScope.launch {
                observeViewModel()
            }
        }*/

//        observeViewModel()

//        exploreViewModel.loadData()
        Log.d("MAINACTIVITY EV", exploreViewModel.toString())
        Log.d("MAINACTIVITY SV", songsViewModel.toString())
        Log.d("MAINACTIVITY PV", playbackViewModel.toString())


//        playbackViewModel.init()
//        playbackViewModel.loadInitialData()
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
//        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
//        navController = navHostFragment.navController
//        navController.setGraph(R.navigation.navigation_graph)
        setContentView(binding.root)

    }

    override fun onStart() {
        super.onStart()
        // Just now
        /*playbackViewModel.isBrowserConnected.observe(this){ connected ->
            if (connected){
                playbackViewModel.init()private fun observeViewModel() {
        if (items.isEmpty()) {
            if ((viewModel.items.value?.size ?: 0) == 0) {
                suspend fun load() {
                    if (viewModel.isBrowserInitialized()) {
                        viewModel.loadData(parentId)
                    } else {
                        delay(2000)
                        Log.e("RETRY", "RETRY")
                        load()
                    }
                }
                viewModel.also {
                    it.viewModelScope.launch {
                        load()
                    }
                }
            }

            viewModel.isBrowserConnected.observe(requireActivity()){ connected ->
                if (connected) {
//                    playbackViewModel.browser = viewModel.browser
                    viewModel.init()
                    viewModel.items.observe(requireActivity()) { list ->
                        if (list.isNotEmpty()) {
                            viewModel.items.value?.first()?.id?.let {
                                if (it.toString().contains("[album]")) {
                                    Log.e("ID", it.toString())
                                    viewModel.loadData(parentId)
                                }
                            }
                        }
                    }
                    viewModel.items.observe(viewLifecycleOwner, Observer(::updateViews))
                }
            }

            viewModel.isControllerConnected.observe(requireActivity()){ connected ->
                if (connected) {
                    playbackViewModel.controller = viewModel.controller
                }
            }

        } else {
            viewModel.overrideCurrentItems(items)
        }
    }
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
        }*/
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

    fun observeViewModel() {
        Log.d("MAINACTIVITY STARTED", "STARTED OBSERVE VIEWMODEL")
        if (items.isEmpty()) {
            if ((songsViewModel.items.value?.size ?: 0) == 0) {
                suspend fun load() {
                    if (songsViewModel.isBrowserInitialized()) {
                        songsViewModel.loadData()
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

            songsViewModel.isBrowserConnected.observe(this){ connected ->
                if (connected) {
//                    playbackViewModel.browser = viewModel.browser
                    songsViewModel.init()
                    songsViewModel.items.observe(this) { list ->
                        if (list.isNotEmpty()) {
                            songsViewModel.items.value?.first()?.id?.let {
                                Log.e("ID", it.toString())
                                if (it.toString().contains("[album]")) {
                                    Log.e("ID", it.toString())
                                    songsViewModel.loadData()
                                }
                            }
                        }
                    }
                    songsViewModel.items.observe(this, Observer(::updateViews))
                }
            }

            songsViewModel.isControllerConnected.observe(this) { connected ->
                if (connected) {
                    playbackViewModel.controller = songsViewModel.controller
                }
            }

        } else {
            songsViewModel.overrideCurrentItems(items)
        }
    }

    private fun updateViews(items: List<Song>) {
        this.items = items
        Log.d("MAINACTIVITY UPDATE VIEWS", this.items.size.toString())
    }

    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)
        if (!(fragment?.javaClass?.isInstance(IOnBackPressed::class.java))!! || !((fragment as IOnBackPressed).onBackPressed())) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.applicationContext.getSharedPreferences("main", Context.MODE_PRIVATE).edit().putBoolean(
            Constants.INITIALIZATION_COMPLETE, false).apply()
    }

}

interface IOnBackPressed {
    fun onBackPressed(): Boolean
}


