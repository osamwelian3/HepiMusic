//package com.hepimusic.main.explore
//
//import android.app.Application
//import android.content.ComponentName
//import android.content.Context
//import android.content.SharedPreferences
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import androidx.media3.common.MediaItem
//import androidx.media3.session.LibraryResult
//import androidx.media3.session.MediaBrowser
//import androidx.media3.session.SessionToken
//import com.google.common.util.concurrent.ListenableFuture
//import com.hepimusic.common.Constants
//import com.hepimusic.main.albums.Album
//import com.hepimusic.models.mappers.toAlbum
//import com.hepimusic.playback.MusicService
//import com.hepimusic.ui.MainActivity
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.suspendCancellableCoroutine
//import kotlinx.coroutines.withContext
//import javax.inject.Inject
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//@HiltViewModel
//open class ExploreViewModelTest @Inject constructor(
//    application: Application,
//    recentlyPlayedDatabase: RecentlyPlayedDatabase
//) : AndroidViewModel(application) {
//
//    private val preferences: SharedPreferences
//
//    private val recentlyPlayedRepository: RecentlyPlayedRepository
//    val recentlyPlayed: LiveData<List<RecentlyPlayed>>
//
//    protected val data = MutableLiveData<List<Album>>()
//    val items: LiveData<List<Album>> get() = data
//
//    private var subItemMediaList: MutableList<MediaItem> = mutableListOf()
//    var mediaItemList: MutableLiveData<List<MediaItem>> = MutableLiveData()
//    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()
//
//    private lateinit var libraryRoot: MediaItem
//
//    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
//
//    private val browser: MediaBrowser?
//        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null
//
//    private val preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener =
//        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
//            Log.e("CHANGED KEY", key!!)
//            when (key) {
//                Constants.INITIALIZATION_COMPLETE -> {
//                    Log.e("PREFERENCE CHANGED", sharedPreferences.getBoolean(Constants.INITIALIZATION_COMPLETE, false).toString())
//                    displayChildrenList(treePathStack.last())
//                }
//            }
//        }
//    init {
//        preferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)
//        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
//        val recentDao = recentlyPlayedDatabase.dao
//        recentlyPlayedRepository = RecentlyPlayedRepository(recentDao)
//        recentlyPlayed = recentlyPlayedRepository.recentlyPlayed
//
//        viewModelScope.launch {
//            initializeBrowser(application.applicationContext)
//        }
//    }
//
//    private suspend fun initializeBrowser(context: Context) {
//        withContext(Dispatchers.Default) {
//            browserFuture =
//                MediaBrowser.Builder(
//                    context,
//                    SessionToken(context, ComponentName(context, MusicService::class.java))
//                )
//                    .buildAsync()
//        }
////        delay(2000)
//        browserFuture.addListener({ getRoot() }, ContextCompat.getMainExecutor(getApplication()))
////        getRoot()
//        if (browser != null) Log.e("browser", browser!!.deviceVolume.toString()) // displayChildrenList(libraryRoot)
//    }
//
//    private fun releaseBrowser() {
//        MediaBrowser.releaseFuture(browserFuture)
//    }
//
//    private fun pushPathStack(mediaItem: MediaItem) {
//        Log.e("ROOT MEDIAITEM", "ROOT MEDIAITEM: key: "+ mediaItem.mediaId +" title: "+ mediaItem.mediaMetadata.title.toString())
//        treePathStack.addLast(mediaItem)
//        preferences.registerOnSharedPreferenceChangeListener(preferencesListener)
//        displayChildrenList(treePathStack.last())
//    }
//
//    private fun popPathStack(context: Context) {
//        treePathStack.removeLast()
//        if (treePathStack.size == 0) {
//            return
//        }
//
//        displayChildrenList(treePathStack.last())
//    }
//
//    private fun getRoot() {
//        // browser can be initialized many timesactivity
//        // only push root at the first initialization
//        if (!treePathStack.isEmpty()) {
//            return
//        }
//        val browser = this.browser ?: return
//        val rootFuture = browser.getLibraryRoot(/* params= */ null)
//
//        rootFuture.addListener(
//            {
//                val result: LibraryResult<MediaItem> = rootFuture.get()!!
//                val root: MediaItem = result.value!!
//                pushPathStack(root)
//                Log.e("SongViewModel", root.mediaMetadata.title.toString())
//            },
//            ContextCompat.getMainExecutor(getApplication())
//        )
//
//    }
//
//    private fun displayChildrenList(mediaItem: MediaItem): MutableLiveData<List<MediaItem>> {
//        val browser = this.browser ?: return MutableLiveData()
//
//        val childrenFuture =
//            browser.getChildren(
//                "[albumID]",
//                 /*page=*/  0,
//                 /*pageSize=*/  Int.MAX_VALUE,
//                 /*params=*/  null
//            )
//        val itemFuture = browser.getItem("[albumID]")
//
//        itemFuture.addListener(
//            {
//                val result = itemFuture.get()
//                Log.e("ITEM FOLDER NAME: ", result.value!!.mediaMetadata.title.toString())
//            },
//            ContextCompat.getMainExecutor(getApplication())
//        )
//
//        subItemMediaList.clear()
//        childrenFuture.addListener(
//            {
//                val result = childrenFuture.get()!!
//                val children = result.value!!
//
//                subItemMediaList.addAll(children)
//                mediaItemList.postValue(subItemMediaList)
//                data.postValue(subItemMediaList.map { it.toAlbum() })
////                continuation.resume(mediaItemList)
//
//                if (!childrenFuture.isDone) {
//                    displayChildrenList(mediaItem)
//                }
//                viewModelScope.launch {
//                    do {
//                        if (children.size == 0 || children.size > subItemMediaList.size){
//                            subItemMediaList.clear()
//                            subItemMediaList.addAll(children.reverse())
////                        playableMediaListAdapter.notifyDataSetChanged()
//                            displayChildrenList(mediaItem)
//                            delay(3000)
//                        }
//                    } while (children.size > subItemMediaList.size)
//                }
//
//            },
//            ContextCompat.getMainExecutor(getApplication())
//        )
//        return mediaItemList
//
//    }
//
//    fun overrideCurrentItems(items: List<Album>) {
//        data.value = items
//    }
//
//    override fun onCleared() {
//        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
//        releaseBrowser()
//        super.onCleared()
//    }
//}