package com.hepimusic.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.hepimusic.playback.MusicService
import com.hepimusic.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongViewModel(application: Application) : AndroidViewModel(application) {


    private var subItemMediaList: MutableList<MediaItem> = mutableListOf()
    var mediaItemList: MutableLiveData<List<MediaItem>> = MutableLiveData()
    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()

    private lateinit var libraryRoot: MediaItem

    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    init {
        viewModelScope.launch {
            initializeBrowser(application.applicationContext)
        }
    }

    private suspend fun initializeBrowser(context: Context) {
        withContext(Dispatchers.Default) {
            browserFuture =
                MediaBrowser.Builder(
                    context,
                    SessionToken(context, ComponentName(context, MusicService::class.java))
                )
                    .buildAsync()
        }
//        delay(2000)
        browserFuture.addListener({ getRoot() }, ContextCompat.getMainExecutor(getApplication()))
//        getRoot()
        if (browser != null) Log.e("browser", browser!!.deviceVolume.toString()) // displayChildrenList(libraryRoot)
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture)
    }

    private fun pushPathStack(mediaItem: MediaItem) {
        Log.e("ROOT MEDIAITEM", "ROOT MEDIAITEM: key: "+ mediaItem.mediaId +" title: "+ mediaItem.mediaMetadata.title.toString())
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
        // browser can be initialized many timesactivity
        // only push root at the first initialization
        if (!treePathStack.isEmpty()) {
            return
        }
        val browser = this.browser ?: return
        val rootFuture = browser.getLibraryRoot(/* params= */ null)

        rootFuture.addListener(
            {
                val result: LibraryResult<MediaItem> = rootFuture.get()!!
                if (result.value != null) {
                    val root: MediaItem = result.value!!
                    pushPathStack(root)
                    Log.e("SongViewModel", root.mediaMetadata.title.toString())
                }
            },
            ContextCompat.getMainExecutor(getApplication())
        )

    }

    private fun displayChildrenList(mediaItem: MediaItem): MutableLiveData<List<MediaItem>> {
        val browser = this.browser ?: return MutableLiveData()

        val childrenFuture =
            browser.getChildren(
                "[albumID]",
                /* page= */ 0,
                /* pageSize= */ Int.MAX_VALUE,
                /* params= */ null
            )

        subItemMediaList.clear()
        childrenFuture.addListener(
            {
                val result = childrenFuture.get()!!
                result?.let {
                    val children = result.value!!

                    subItemMediaList.addAll(children)
                    mediaItemList.postValue(subItemMediaList)
//                continuation.resume(mediaItemList)
                }
            },
            ContextCompat.getMainExecutor(getApplication())
        )
        return mediaItemList

    }

    override fun onCleared() {
        releaseBrowser()
        super.onCleared()
    }
}