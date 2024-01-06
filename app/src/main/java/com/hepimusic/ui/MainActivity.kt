package com.hepimusic.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.generated.model.Profile
import com.amplifyframework.geo.maplibre.view.AmplifyMapView
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.databinding.ActivityMainBinding
import com.hepimusic.main.admin.albums.AdminAlbumsViewModel
import com.hepimusic.main.admin.categories.AdminCategoriesViewModel
import com.hepimusic.main.admin.creators.AdminCreatorsViewModel
import com.hepimusic.main.admin.dashboard.AdminNavViewModel
import com.hepimusic.main.admin.songs.AdminSongsViewModel
import com.hepimusic.main.albums.AlbumSongsViewModel
import com.hepimusic.main.artists.ArtistAlbumsViewModel
import com.hepimusic.main.artists.ArtistsViewModel
import com.hepimusic.main.explore.ExploreViewModel
import com.hepimusic.main.playlist.AddSongsToPlaylistsViewModel
import com.hepimusic.main.playlist.PlaylistSongsEditorViewModel
import com.hepimusic.main.playlist.PlaylistSongsViewModel
import com.hepimusic.main.playlist.PlaylistViewModel
import com.hepimusic.main.playlist.WritePlaylistViewModel
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.profile.WriteProfileViewModel
import com.hepimusic.main.requests.RequestsViewModel
import com.hepimusic.main.requests.users.PlayersFragment
import com.hepimusic.main.search.SearchViewModel
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.playback.MusicService
import com.hepimusic.playback.PlaybackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var preferences: SharedPreferences

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
    lateinit var profileViewModel: ProfileViewModel
    lateinit var writeProfileViewModel: WriteProfileViewModel

    //admin
    lateinit var adminNavViewModel: AdminNavViewModel
    lateinit var songsAdminViewModel: AdminSongsViewModel
    lateinit var albumsAdminViewModel: AdminAlbumsViewModel
    lateinit var categoriesAdminViewModel: AdminCategoriesViewModel
    lateinit var creatorsAdminViewModel: AdminCreatorsViewModel

    // requests
    lateinit var requestsViewModel: RequestsViewModel

    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    var items = emptyList<Song>()
    private val backgroundScope = CoroutineScope(SupervisorJob()+Dispatchers.IO)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicService.MusicServiceBinder) {
                if (::playbackViewModel.isInitialized) {
                    playbackViewModel.exoPlayer = service.musicService.player
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    private val exceptionHandler = Thread.UncaughtExceptionHandler { t, e ->
        e.printStackTrace()
        Log.e("UNCAUGHT EXCEPTION HANDLER", e.message.toString())
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    val amplifyMapView by lazy {
        findViewById<AmplifyMapView>(R.id.mapView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler)
        preferences = this.getSharedPreferences("main", MODE_PRIVATE)
        preferences.edit().putString(Constants.SESSION_ID, UUID.randomUUID().toString()).apply()

//        Firebase.messaging.isAutoInitEnabled = true

        CoroutineScope(Dispatchers.IO).launch {
            playbackViewModel = ViewModelProvider(this@MainActivity)[PlaybackViewModel::class.java]
            exploreViewModel = ViewModelProvider(this@MainActivity)[ExploreViewModel::class.java]
            songsViewModel = ViewModelProvider(this@MainActivity)[SongsViewModel::class.java]
            albumSongsViewModel = ViewModelProvider(this@MainActivity)[AlbumSongsViewModel::class.java]
            playlistViewModel = ViewModelProvider(this@MainActivity)[PlaylistViewModel::class.java]
            writePlaylistViewModel = ViewModelProvider(this@MainActivity)[WritePlaylistViewModel::class.java]
            playlistSongsViewModel = ViewModelProvider(this@MainActivity)[PlaylistSongsViewModel::class.java]
            addSongsToPlaylistViewModel =
                ViewModelProvider(this@MainActivity)[AddSongsToPlaylistsViewModel::class.java]
            playlistSongsEditorViewModel =
                ViewModelProvider(this@MainActivity)[PlaylistSongsEditorViewModel::class.java]
            searchViewModel = ViewModelProvider(this@MainActivity)[SearchViewModel::class.java]
            artistsViewModel = ViewModelProvider(this@MainActivity)[ArtistsViewModel::class.java]
            artistAlbumsViewModel = ViewModelProvider(this@MainActivity)[ArtistAlbumsViewModel::class.java]
            profileViewModel = ViewModelProvider(this@MainActivity)[ProfileViewModel::class.java]
            writeProfileViewModel = ViewModelProvider(this@MainActivity)[WriteProfileViewModel::class.java]
            songsAdminViewModel = ViewModelProvider(this@MainActivity)[AdminSongsViewModel::class.java]
            albumsAdminViewModel = ViewModelProvider(this@MainActivity)[AdminAlbumsViewModel::class.java]
            categoriesAdminViewModel = ViewModelProvider(this@MainActivity)[AdminCategoriesViewModel::class.java]
            creatorsAdminViewModel = ViewModelProvider(this@MainActivity)[AdminCreatorsViewModel::class.java]
            adminNavViewModel = ViewModelProvider(this@MainActivity)[AdminNavViewModel::class.java]
            requestsViewModel = ViewModelProvider(this@MainActivity)[RequestsViewModel::class.java]
        }
        startMusicService()

        Amplify.Auth.getCurrentUser(
            { user ->
                if (preferences.getString(Constants.AUTH_USER, null).isNullOrEmpty()) {
                    preferences.edit().putString(Constants.AUTH_USER, Gson().toJson(user)).apply()
                }
                if (!preferences.getBoolean(Constants.AUTH_TYPE_SOCIAL, false)) {
                    Amplify.DataStore.query(
                        Profile::class.java,
                        Where.identifier(Profile::class.java, user.userId),
                        { profileMutableIterator ->
                            if (!profileMutableIterator.hasNext()) {
                                Amplify.DataStore.save(
                                    com.amplifyframework.datastore.generated.model.Profile.builder()
                                        .key(user.userId)
                                        .name(user.username)
                                        .email(if (preferences.getString(Constants.USERNAME, "")!!.contains("@")) preferences.getString(Constants.USERNAME, "") else "")
                                        .build(),
                                    {
                                        Log.e("INITIALIZE PROFILE", it.item().name)
                                    },
                                    {
                                        Log.e("INITIALIZE PROFILE", it.cause?.message.toString())
                                    }
                                )
                            }
                        },
                        {

                        }
                    )
                } else {
                    Amplify.DataStore.query(
                        Profile::class.java,
                        Where.identifier(Profile::class.java, user.userId),
                        { profileMutableIterator ->
                            if (!profileMutableIterator.hasNext()) {
                                Amplify.DataStore.save(
                                    com.amplifyframework.datastore.generated.model.Profile.builder()
                                        .key(user.userId)
                                        .email(preferences.getString(Constants.USERNAME, ""))
                                        .name(
                                            preferences.getString(Constants.USERNAME, "")?.split("@")
                                                ?.get(0)
                                                ?: "")
                                        .build(),
                                    {
                                        Log.e("INITIALIZE PROFILE", it.item().name)
                                    },
                                    {
                                        Log.e("INITIALIZE PROFILE", it.cause?.message.toString())
                                    }
                                )
                            }
                        },
                        {

                        }
                    )
                }
            },
            {
                try {
                    val user = Gson().fromJson(
                        preferences.getString(Constants.AUTH_USER, null),
                        AuthUser::class.java
                    )
                    if (user != null) {
                        if (!preferences.getBoolean(Constants.AUTH_TYPE_SOCIAL, false)) {
                            Amplify.DataStore.query(
                                Profile::class.java,
                                Where.identifier(Profile::class.java, user.userId),
                                { profileMutableIterator ->
                                    if (!profileMutableIterator.hasNext()) {
                                        Amplify.DataStore.save(
                                            Profile.builder()
                                                .key(user.userId)
                                                .name(user.username)
                                                .email(if (preferences.getString(Constants.USERNAME, "")!!.contains("@")) preferences.getString(Constants.USERNAME, "") else "")
                                                .build(),
                                            {
                                                Log.e("INITIALIZE PROFILE", it.item().name)
                                            },
                                            {
                                                Log.e("INITIALIZE PROFILE", it.cause?.message.toString())
                                            }
                                        )
                                    }
                                },
                                {

                                }
                            )
                        } else {
                            Amplify.DataStore.query(
                                Profile::class.java,
                                Where.identifier(Profile::class.java, user.userId),
                                { profileMutableIterator ->
                                    if (!profileMutableIterator.hasNext()) {
                                        Amplify.DataStore.save(
                                            com.amplifyframework.datastore.generated.model.Profile.builder()
                                                .key(user.userId)
                                                .email(preferences.getString(Constants.USERNAME, ""))
                                                .name(
                                                    preferences.getString(Constants.USERNAME, "")?.split("@")
                                                        ?.get(0)
                                                        ?: "")
                                                .build(),
                                            {
                                                Log.e("INITIALIZE PROFILE", it.item().name)
                                            },
                                            {
                                                Log.e("INITIALIZE PROFILE", it.cause?.message.toString())
                                            }
                                        )
                                    }
                                },
                                {

                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            application.applicationContext,
                            it.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

//        Amplify.DataStore.observeQuery(
//            com.amplifyframework.datastore.generated.model.Song::class.java,
//            ObserveQueryOptions(),
//            {
//                Log.e("Amplify Datastore Cancelable", it.toString())
//            },
//            {
//                Log.e("Amplify ITEM CHANGED", it.items.size.toString())
//            },
//            { Log.e("Amplify DataStore Exception", it.message.toString()) },
//            { Log.e("Amplify COMPLETED", "OBSERVATION COMPLETE") }
//        )

        /*songsViewModel.also {
            it.viewModelScope.launch {
                observeViewModel()
            }
        }*/

//        observeViewModel()

//        exploreViewModel.loadData()
        /*backgroundScope.launch {
            Log.d("MAINACTIVITY EV", exploreViewModel.toString())
            Log.d("MAINACTIVITY SV", songsViewModel.toString())
            Log.d("MAINACTIVITY PV", playbackViewModel.toString())
        }*/


//        playbackViewModel.init()
//        playbackViewModel.loadInitialData()
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        binding.mapView
//        navHostFragment = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
//        navController = navHostFragment.navController
//        navController.setGraph(R.navigation.navigation_graph)
        setContentView(binding.root)

    }

    private fun startMusicService() {
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun stopService() {
        unbindService(serviceConnection)
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

            viewModel.isBrowserConnected.observe(viewLifecycleOwner){ connected ->
                if (connected) {
//                    playbackViewModel.browser = viewModel.browser
                    viewModel.init()
                    viewModel.items.observe(viewLifecycleOwner) { list ->
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

            viewModel.isControllerConnected.observe(viewLifecycleOwner){ connected ->
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
        val fragmentManager = this.supportFragmentManager
        val fragmentsList = fragmentManager.fragments
        var fragment: Fragment? = null

        loop@ for (f in fragmentsList) {
            if (f != null && f.isVisible) {
                val childFragmentManager = f.childFragmentManager
                val childFragmentsList = childFragmentManager.fragments
                for (cf in childFragmentsList) {
                    if (cf is PlayersFragment) {
                        fragment = cf
                        break@loop
                    }
                }
                /*if (f.javaClass.isInstance(PlayersFragment::class.java)) {
                    fragment = f
                }*/
            }
        }


        fragment?.let {
            if (Constants.REQUEST_CHECK_SETTINGS == requestCode) {
                if(Activity.RESULT_OK == resultCode){
                    //user clicked OK, you can startUpdatingLocation(...);
                    (fragment as PlayersFragment).getLocation()
                }else{
                    //user clicked cancel: informUserImportanceOfLocationAndPresentRequestAgain();
                    Toast.makeText(this, "We need your device location to find Players near you. Please allow device location", Toast.LENGTH_LONG).show()
                    (fragment as PlayersFragment).createLocationRequest()
                }
            }
        }
    }

    /*fun observeViewModel() {
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
    }*/

    override fun onBackPressed() {
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.mainNavHostFragment)
        if (!(fragment?.javaClass?.isInstance(IOnBackPressed::class.java))!! || !((fragment as IOnBackPressed).onBackPressed())) {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
        this.applicationContext.getSharedPreferences("main", Context.MODE_PRIVATE).edit().putBoolean(
            Constants.INITIALIZATION_COMPLETE, false).apply()
    }

}

interface IOnBackPressed {
    fun onBackPressed(): Boolean
}


