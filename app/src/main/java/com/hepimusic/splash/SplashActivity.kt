package com.hepimusic.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.amplifyframework.core.Amplify
import com.hepimusic.R
import com.hepimusic.auth.LoginActivity
import com.hepimusic.common.BaseActivity
import com.hepimusic.common.Constants
import com.hepimusic.common.Constants.INITIALIZATION_COMPLETE
import com.hepimusic.getStarted.GetStartedActivity
import com.hepimusic.main.albums.AlbumSongsViewModel
import com.hepimusic.main.artists.ArtistAlbumsViewModel
import com.hepimusic.main.artists.ArtistsViewModel
import com.hepimusic.main.explore.ExploreViewModel
import com.hepimusic.main.playlist.AddSongsToPlaylistsViewModel
import com.hepimusic.main.playlist.PlaylistSongsEditorViewModel
import com.hepimusic.main.playlist.PlaylistSongsViewModel
import com.hepimusic.main.playlist.PlaylistViewModel
import com.hepimusic.main.playlist.WritePlaylistViewModel
import com.hepimusic.main.search.SearchViewModel
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.onBoarding.OnBoardingActivity
import com.hepimusic.playback.PlaybackViewModel
import com.hepimusic.ui.MainActivity
import com.hepimusic.viewmodels.SongViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    /*private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    private val browser2: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    @Inject lateinit var browser: MediaBrowser*/

    private lateinit var viewModel: SongViewModel

    private lateinit var preferences: SharedPreferences

    var conditionOne: Boolean = false

    var conditionTwo: Boolean = false

    var conditionThree: Boolean = false

    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.e("CHANGED KEY", key!!)
            when (key) {
                Constants.INITIALIZATION_COMPLETE -> {
                    Log.e("PREFERENCE CHANGED", sharedPreferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false).toString())
                    goToNextScreen()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Amplify.Auth.getCurrentUser(
            {
                Log.e("AUTH USER", it.username)
            },
            {
                Log.e("AUTH USER Exception", it.stackTraceToString())
                Log.e("AUTH USER Exception", it.message.toString())

                if (it.message.toString().contains("You are currently signed out.", true)) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        )

//        val browserInstance = BrowserInstance(this.applicationContext, this)
//        browserInstance.run()

//        MediaBrowserManager.initialize(this.applicationContext)
//        browser = MediaBrowserManager.getMediaBrowser() // browserInstance.getBrowserInstance()

        preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)


        // conditions
        conditionOne = preferences.getBoolean(OnBoardingActivity.HAS_SEEN_ON_BOARDING, false)
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        conditionTwo = (isPermissionGranted(
            android.Manifest.permission.READ_MEDIA_AUDIO
        ) && isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES))
        conditionThree = isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        /*if (preferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false) && (conditionTwo || conditionThree)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            viewModel = ViewModelProvider(this).get(SongViewModel::class.java)
            viewModel.mediaItemList.observe(this) { mediaItemsList ->
                mediaItemsList.map {
                    Log.e("MEDIA ITEM: ", it.mediaMetadata.title.toString())
                }
            }
        }*/
        viewModel = ViewModelProvider(this).get(SongViewModel::class.java)
        viewModel.mediaItemList.observe(this) { mediaItemsList ->
            mediaItemsList.map {
                Log.e("MEDIA ITEM: ", it.mediaMetadata.title.toString())
            }
            if (mediaItemsList.isNotEmpty()) {
                goToNextScreen()
            }
        }
        preferences.edit().putBoolean(INITIALIZATION_COMPLETE, false).apply()
//        initializeBrowser()
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)



        // Do some network activity

        /*fun run(){
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                *//*Log.e("BROWSER WORKING", "Device Volume: "+browser.deviceVolume.toString())
                if (browser != null){
                    goToNextScreen()
                } else {
                    run()
                }*//*
                if (!preferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false)) {
                    Log.e("INITIALIZATION COMPLETE", preferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false).toString())
                    run()
                } else {
                    goToNextScreen()
                }
//                                                                    goToNextScreen()
            }, 3000)
        }
        run()*/


    }

    override fun onStart() {
        super.onStart()
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
    }

    private fun goToNextScreen() {
        val nextActivity =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                if (!conditionOne /*preferences.getBoolean(OnBoardingActivity.HAS_SEEN_ON_BOARDING, false)*/) {
                    OnBoardingActivity::class.java
                } else if (!conditionTwo /*(isPermissionGranted(
                        android.Manifest.permission.READ_MEDIA_AUDIO
                    ) && isPermissionGranted(android.Manifest.permission.READ_MEDIA_IMAGES))*/
                ) {
                    GetStartedActivity::class.java
                } else {
                    MainActivity::class.java
                }
            } else {
                if (!conditionOne /*preferences.getBoolean(OnBoardingActivity.HAS_SEEN_ON_BOARDING, false)*/) {
                    OnBoardingActivity::class.java
                } else if (!conditionThree /*isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)*/) {
                    GetStartedActivity::class.java
                } else {
                    MainActivity::class.java
                }
            }
        val intent = Intent(this, nextActivity)
        startActivity(intent)
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.fade_in, R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onResume() {
        if (preferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false)){
            goToNextScreen()
            finish()
        }
        super.onResume()
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
    }

    override fun onPause() {
        super.onPause()
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
    }


}
