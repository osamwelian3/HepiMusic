package com.hepimusic.main.common.view

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hepimusic.main.common.data.MediaStoreRepository
import com.hepimusic.main.songs.Song
import com.hepimusic.playback.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseMediaStoreViewModel<T> (
    application: Application
): ViewModel() {
    protected val data = MutableLiveData<List<T>>()
    protected val filteredData = MutableLiveData<List<T>>()
    val items: LiveData<List<T>> get() = data
    val filteredItems: LiveData<List<T>> = filteredData
    abstract val repository: MediaStoreRepository<T>
    abstract val parentId: String
    var albumId: String? = null
    open var projection: Array<String>? = null
    open var selection: String? = null
    open var selectionArgs: Array<String>? = null
    open var sortOrder: String? = null

    private var application: Application

    private val treePathStack: ArrayDeque<MediaItem> = ArrayDeque()
    private var subItemMediaList: MutableList<MediaItem> = mutableListOf()
    var mediaItemList: MutableLiveData<List<MediaItem>> = MutableLiveData()

    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    private val browser2: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    lateinit var browser: MediaBrowser

    val _liveBrowser = MutableLiveData<MediaBrowser>()
    val liveBrowser : LiveData<MediaBrowser> = _liveBrowser

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val controller2: MediaController?
        get() = if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    lateinit var controller: MediaController

    val _isBrowserConnected = MutableLiveData<Boolean>()

    val isBrowserConnected: LiveData<Boolean> = _isBrowserConnected

    val _isControllerConnected = MutableLiveData<Boolean>()

    val isControllerConnected: LiveData<Boolean> = _isControllerConnected

    init {
        this.application = application
        viewModelScope.launch {
            initializeBrowser(application)
            initializeController(application)
        }
    }

    fun isBrowserInitialized(): Boolean {
        return ::browser.isInitialized
    }

    private suspend fun initializeBrowser(context: Context) {
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
//        getRoot()
        suspend fun initBrowser() {
            if (browser2 != null) {
                Log.e("BaseMediaStoreViewModel browser", browser2!!.deviceVolume.toString())
                browser = withContext(Dispatchers.IO) {
                    browserFuture.get()
                }
                _isBrowserConnected.postValue(::browser.isInitialized)
                loadData()
            } else {
                delay(2000)
                initBrowser()
            // displayChildrenList(libraryRoot)
            }
        }
        initBrowser()*/
        browserFuture.addListener({
            viewModelScope.launch {
                if (browser2 != null) {
                    browser = withContext(Dispatchers.IO) {
                        browserFuture.get()
                    }
                    _isBrowserConnected.postValue(::browser.isInitialized)
                    _liveBrowser.postValue(browser)
                    loadData(parentId)
//                    getRoot()
                }
            }
                                  }, ContextCompat.getMainExecutor(application))
    }

    private suspend fun initializeController(context: Context) {
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
            if (controller2 != null) {
                Log.e("BaseMediaStoreViewModel browser", controller2!!.deviceVolume.toString())
                controller = withContext(Dispatchers.IO) {
                    controllerFuture.get()
                }
                _isControllerConnected.postValue(::controller.isInitialized)
                loadData()
            } else {
                delay(2000)
                initController()
                // displayChildrenList(libraryRoot)
            }
        }
        initController()*/
        controllerFuture.addListener({
            viewModelScope.launch {
                if (controller2 != null) {
                    controller = withContext(Dispatchers.IO) {
                        controllerFuture.get()
                    }
                    _isControllerConnected.postValue(::controller.isInitialized)
//                    loadData(parentId)
                }
            }

        }, MoreExecutors.directExecutor())
    }

    /*private fun releaseBrowser() {
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
        val browser = this.browser ?: return
        val rootFuture = browser.getLibraryRoot(*//* params= *//* null)

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
        val browser = this.browser ?: return MutableLiveData()

        val childrenFuture =
            browser.getChildren(
                mediaItem.mediaId,
                *//* page= *//* 0,
                *//* pageSize= *//* Int.MAX_VALUE,
                *//* params= *//* null
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

    }*/

//    private val observer: ContentObserver = object : ContentObserver(null) {
//        override fun onChange(selfChange: Boolean) {
//            loadData()
//        }
//    }

    /**
     *  Fetch data from the [MediaStore] and watch it for changes to the data at [uri]]
     */
    @CallSuper
    open fun init(vararg params: Any?) {
//        observer.onChange(false)
//        getApplication<Application>().contentResolver.registerContentObserver(uri, true, observer)
        if (items.value == null) return
        if (items.value?.isEmpty()!!) {
            viewModelScope.launch {
                repository.loadData(parentId)

            }
        }
    }


    fun loadData(parentId2: String = "") {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                //repository.loadData(uri, projection, selection, selectionArgs, sortOrder)
                withContext(Dispatchers.IO){
                    repository.loadData(parentId)
                }

            }
            deliverResult(result)

        }
    }

    override fun onCleared() {
        super.onCleared()
//        getApplication<Application>().contentResolver.unregisterContentObserver(observer)
    }

    fun getAlbumSongs() {
        if (albumId != null){
            val tempList = mutableListOf<T>()
            items.value?.map {
                val item = it as Song
                if ("[album]"+item.album as String == albumId) {
                    tempList.add(it)
                }
            }
            filteredData.value = tempList
        }
    }

    // Give child classes the opportunity to intercept and modify result
    open fun deliverResult(items: List<T>) {
        data.postValue(items)
        if (data.value != items) data.value = items
    }

    fun overrideCurrentItems(items: List<T>) {
        data.postValue(items)
        data.value = items
    }
}