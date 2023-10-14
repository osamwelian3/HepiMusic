package com.hepimusic.main.common.view

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.playback.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MyBaseViewModel(
    application: Application
): ViewModel() {

    private var application: Application

    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()
    private var subItemMediaList: MutableList<MediaItem> = mutableListOf()
    var mediaItemList: MutableLiveData<List<MediaItem>> = MutableLiveData()

    lateinit var browserFuture: ListenableFuture<MediaBrowser>
    lateinit var controllerFuture: ListenableFuture<MediaController>

    private val browser2: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    private val controller2: MediaController?
        get() = if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    lateinit var globalBrowser: MediaBrowser
    lateinit var globalController: MediaController

    val _isBrowserConnected = MutableLiveData<Boolean>()

    val isBrowserConnected: LiveData<Boolean> = _isBrowserConnected.apply { value = false }

    val _isControllerConnected = MutableLiveData<Boolean>()

    val isControllerConnected: LiveData<Boolean> = _isControllerConnected.apply { value = false }

    init {
        this.application = application
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                initializeBrowser(application)
                initializeController(application)
            }
        }
    }

    fun isBrowserInitialized(): Boolean {
        return ::globalBrowser.isInitialized
    }

    suspend fun initializeBrowser(context: Context) {
        browserFuture =
            MediaBrowser.Builder(
                context,
                SessionToken(context, ComponentName(context, MusicService::class.java))
            )
                .buildAsync()
        /*while (!browserFuture.isDone) {
            delay(2000)
        }
        delay(2000)
        globalBrowser = browserFuture.get()
        //        getRoot()
        suspend fun initBrowser() {
            if (browser2 != null) {
                Log.e("BaseMediaStoreViewModel browser", browser2!!.deviceVolume.toString())
                globalBrowser = withContext(Dispatchers.IO) {
                    browserFuture.get()
                }
                _isBrowserConnected.postValue(::globalBrowser.isInitialized)
            } else {
                delay(2000)
                initBrowser()
                // displayChildrenList(libraryRoot)
            }
        }
        initBrowser()*/
        browserFuture.addListener({
            if (browserFuture.isDone && !browserFuture.isCancelled) {
                if (browser2 != null) {
                    globalBrowser = browser2!!
                    _isBrowserConnected.postValue(true)

                }
            }
                                  }, ContextCompat.getMainExecutor(application))
    }

    suspend fun initializeController(context: Context) {
        controllerFuture =
            MediaController.Builder(
                context,
                SessionToken(context, ComponentName(context, MusicService::class.java))
            )
                .buildAsync()
        /*while (!controllerFuture.isDone) {
            delay(2000)
        }
        delay(2000)
        //        getRoot()
        suspend fun initController() {
            if (browser2 != null) {
                Log.e("BaseMediaStoreViewModel controller", controller2!!.deviceVolume.toString())
                globalController = withContext(Dispatchers.IO) {
                    controllerFuture.get()
                }
                _isBrowserConnected.postValue(::globalController.isInitialized)
            } else {
                delay(2000)
                initController()
                // displayChildrenList(libraryRoot)
            }
        }
        initController()*/

        Log.e("CONTROLLER VOL", "CONTROLLER VOL: "+controller2?.deviceVolume.toString())
        controllerFuture.addListener({
            if (controllerFuture.isDone && !controllerFuture.isCancelled) {
                if (controller2 != null) {
                    globalController = controller2!!
                    Log.e("CONTROLLER VOL", "CONTROLLER VOL: "+controller2?.deviceVolume.toString())
                    _isControllerConnected.postValue(true)
                }
            }
                                     }, MoreExecutors.directExecutor())
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture)
    }

    private fun pushPathStack(mediaItem: MediaItem) {
        treePathStack.addLast(mediaItem)
        displayChildrenList(treePathStack.last())
    }

    private fun popPathStack(context: Context) {
        treePathStack.removeLast()
        if (treePathStack.size == 0) {
            return
        }

        displayChildrenList(treePathStack.last())
    }

    private fun getRoot() {
        // browser can be initialized many times
        // only push root at the first initialization
        if (!treePathStack.isEmpty()) {
            return
        }
        val browser = this.globalBrowser ?: return
        val rootFuture = browser.getLibraryRoot(/* params= */ null)

        rootFuture.addListener(
            {
                val result: LibraryResult<MediaItem> = rootFuture.get()!!
                val root: MediaItem = result.value!!
                pushPathStack(root)
                Log.e("BaseMediaStoreViewModel", root.mediaMetadata.title.toString())
            },
            ContextCompat.getMainExecutor(application)
        )

    }

    private fun displayChildrenList(mediaItem: MediaItem): MutableLiveData<List<MediaItem>> {
        val browser = this.globalBrowser ?: return MutableLiveData()

        val childrenFuture =
            browser.getChildren(
                mediaItem.mediaId,
                /* page= */ 0,
                /* pageSize= */ Int.MAX_VALUE,
                /* params= */ null
            )

        subItemMediaList.clear()
        childrenFuture.addListener(
            {
                val result = childrenFuture.get()!!
                val children = result.value!!

                subItemMediaList.addAll(children)
                mediaItemList.postValue(subItemMediaList)
//                continuation.resume(mediaItemList)
            },
            ContextCompat.getMainExecutor(application)
        )
        return mediaItemList

    }

    override fun onCleared() {
        super.onCleared()
        MediaController.releaseFuture(controllerFuture)
        MediaBrowser.releaseFuture(browserFuture)
        browser2?.release()
        controller2?.release()
    }

//    private val observer: ContentObserver = object : ContentObserver(null) {
//        override fun onChange(selfChange: Boolean) {
//            loadData()
//        }
//    }
}