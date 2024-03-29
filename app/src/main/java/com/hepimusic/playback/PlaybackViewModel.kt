package com.hepimusic.playback

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_GET_TIMELINE
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.room.withTransaction
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.options.AuthFetchSessionOptions
import com.amplifyframework.core.Action
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.Consumer
import com.amplifyframework.core.async.Cancelable
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.QuerySortBy
import com.amplifyframework.core.model.query.QuerySortOrder
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.DataStoreQuerySnapshot
import com.amplifyframework.datastore.generated.model.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.MoreExecutors
import com.hepimusic.common.Constants
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.view.MyBaseViewModel
import com.hepimusic.main.explore.RecentlyPlayed
import com.hepimusic.main.explore.RecentlyPlayedDatabase
import com.hepimusic.main.explore.RecentlyPlayedRepository
import com.hepimusic.main.playlist.PlaylistItemsDatabase
import com.hepimusic.main.playlist.PlaylistItemsRepository
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.models.mappers.toSong
import com.hepimusic.models.mappers.toStreamCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val application: Application,
    private val recentlyPlayedDatabase: RecentlyPlayedDatabase,
    val playlistItemsDatabase: PlaylistItemsDatabase
) : MyBaseViewModel(application) {

    lateinit var exoPlayer: ExoPlayer
    private var currentIndex = 0
    private var addedUpTo = 0

    var listensMap: MutableMap<String, ListenItemNode> = mutableMapOf()
    private var trendingListensMap: MutableMap<String, String> = mutableMapOf()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var browser2: MediaBrowser
    lateinit var controller: MediaController
    private val playlistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)
    private val preferences: SharedPreferences =
        application.getSharedPreferences("main", Context.MODE_PRIVATE)

    private lateinit var playedRepository: RecentlyPlayedRepository
    private val _mediaItems = MutableLiveData<List<MediaItem>>()
    private val _contentLength = MutableLiveData<Int>()
    private val _currentItem = MutableLiveData<MediaItem?>().apply { try { value = NOTHING_PLAYING } catch (e: Exception) { postValue(NOTHING_PLAYING) } }
    private val _isPlaying = MutableLiveData<Boolean>().apply { try { value = false } catch (e: Exception) { postValue(false) } }
    private val _playbackState = MutableLiveData<Int>().apply { try { value = Player.STATE_IDLE } catch (e: Exception) { postValue(Player.STATE_IDLE) } }
    private val _shuffleMode = MutableLiveData<Boolean>().apply {
        try {
            value = preferences.getBoolean(
                Constants.LAST_SHUFFLE_MODE,
                false
            )
        } catch (e: Exception) {
            postValue(
                preferences.getBoolean(
                    Constants.LAST_SHUFFLE_MODE,
                    false
                )
            )
        }
    }
    private val _repeatMode = MutableLiveData<Int>().apply {
        try {
            value = preferences.getInt(
                Constants.LAST_REPEAT_MODE,
                REPEAT_MODE_ALL
            )
        } catch (e: Exception) {
            postValue(
                preferences.getInt(
                    Constants.LAST_REPEAT_MODE,
                    REPEAT_MODE_ALL
                )
            )
        }
    }
    private val _mediaPosition =
        MutableLiveData<Int>().apply { try { value = preferences.getInt(Constants.LAST_POSITION, 0) } catch (e: Exception) { postValue(preferences.getInt(Constants.LAST_POSITION, 0)) } }
    private val _mediaBufferPosition = MutableLiveData<Int>().apply { postValue(0) }
    private var updatePosition = true
    private var updateListen = true
    private val handler = Handler(Looper.getMainLooper())
    private val timer = Timer()
    private var playMediaAfterLoad: String? = null

    val mediaItems: LiveData<List<MediaItem>> = _mediaItems
    val contentLength: LiveData<Int> = _contentLength
    val currentItem: LiveData<MediaItem?> = _currentItem
    val isPlaying: LiveData<Boolean> = _isPlaying
    val playbackState: LiveData<Int> = _playbackState
    val mediaPosition: LiveData<Int> = _mediaPosition
    val mediaBufferPosition: LiveData<Int> = _mediaBufferPosition
    val shuffleMode: LiveData<Boolean> = _shuffleMode
    val repeatMode: LiveData<Int> = _repeatMode

    val nowPlaying = MutableLiveData<MediaItem>().apply { postValue(NOTHING_PLAYING) }
    val networkFailure = MutableLiveData<Boolean>().apply { postValue(false) }
    lateinit var observer: Observer<List<RecentlyPlayed>>

    lateinit var browser: MediaBrowser

    val _liveBrowser = MutableLiveData<MediaBrowser>()
    val liveBrowser: LiveData<MediaBrowser> = _liveBrowser

    var currentUser: AuthUser? = null
    val session_id = preferences.getString(Constants.SESSION_ID, null)

    var job: Job? = null

    val _userHaslikedSong = MutableLiveData<Boolean>().apply { postValue(false) }
    val userHasLikedSong: LiveData<Boolean> = _userHaslikedSong
    val _upVotesCount = MutableLiveData<String>().apply { postValue(0.toStreamCount()) }
    val upVotesCount: LiveData<String> = _upVotesCount
    val _listensCount = MutableLiveData<String>().apply { postValue(0.toStreamCount()) }
    val listensCount: LiveData<String> = _listensCount

    val sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            Constants.SESSION_ID -> {
                if (!listensMap.containsKey(session_id)) {
                    listensMap[sharedPreferences.getString(key, null)!!] = ListenItemNode(session_id!!)
                }
            }
        }
    }

    init {
//        _isPlaying.postValue(false)
        CoroutineScope(Dispatchers.IO).launch {
            val recentlyPlayed =
                recentlyPlayedDatabase.dao // RecentlyPlayedRoomDatabase.getDatabase(application).recentDao()
            playedRepository = RecentlyPlayedRepository(recentlyPlayed)
            init()
            Amplify.Auth.getCurrentUser(
                {
                    currentUser = it
                },
                {
                    currentUser = null
                }
            )
            preferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
            if (session_id != null) {
                if (!listensMap.containsKey(session_id)) {
                    listensMap[session_id] = ListenItemNode(session_id)
                }
            }
        }
    }

    fun init() {
        CoroutineScope(Dispatchers.Main).launch {
            isBrowserConnected.observeForever { connected ->
                if (connected) {
                    browser2 = globalBrowser

                    browser = browser2
                    _liveBrowser.postValue(browser)
                    browser.addListener(playerListener)

                    browser.also { browser ->
                        val lastParentId = preferences.getString(
                            Constants.LAST_PARENT_ID,
                            ""
                        )!!
                        if (lastParentId.contains("[playlist]")) {
                            playlistItemsRepository.playlistItems.observeForever { _itemList ->
                                val itemList = _itemList.filter {
                                    it.playlistId == lastParentId
                                }
                                val songItems = itemList.map { it.toSong().toMediaItem() }
                                val lastMediaId = preferences.getString(Constants.LAST_ID, songItems.firstOrNull()?.mediaId ?: "")
                                val lastPosition = preferences.getInt(Constants.LAST_POSITION, 0)
                                val indexOfLastMediaId = songItems.indexOfFirst { mediaItem -> mediaItem.mediaId == lastMediaId }
                                val mediaItemstoSet = songItems.toTypedArray()
                                val startIndex = if (indexOfLastMediaId != -1) indexOfLastMediaId else 0
                                val currentItem = songItems.find { it.mediaId == lastMediaId }
                                _mediaItems.postValue(songItems)
                                _currentItem.postValue(currentItem)
                                updateLikeButton(currentItem)
                                _mediaPosition.postValue(lastPosition)

                                playAll(lastMediaId ?: songItems.first().mediaId, songItems, false, Integer.toUnsignedLong(lastPosition))
                                /*browser.setMediaItems(songItems, startIndex, Integer.toUnsignedLong(lastPosition))*/

                            }
                        } else if (lastParentId == "[recentlyPlayedId]") {
                            observer = Observer { playedList ->
                                val itemList = playedList.map { it.toMediaItem() }
                                val lastMediaId = preferences.getString(
                                    Constants.LAST_ID,
                                    itemList.firstOrNull()?.mediaId ?: ""
                                )
                                val lastPosition = preferences.getInt(Constants.LAST_POSITION, 0)
                                val indexOfLastMediaId =
                                    itemList.indexOfFirst { mediaItem -> mediaItem.mediaId == lastMediaId }
                                val mediaItemstoSet = itemList.toTypedArray()
                                val startIndex =
                                    if (indexOfLastMediaId != -1) indexOfLastMediaId else 0
                                val currentItem = itemList.find { it.mediaId == lastMediaId }
                                _mediaItems.postValue(itemList)
                                _currentItem.postValue(currentItem)
                                updateLikeButton(currentItem)
                                _mediaPosition.postValue(lastPosition)

                                playAll(lastMediaId ?: itemList.first().mediaId, itemList, false, Integer.toUnsignedLong(lastPosition))
                                /*browser.setMediaItems(
                                    itemList,
                                    startIndex,
                                    Integer.toUnsignedLong(lastPosition)
                                )*/

                                playedRepository.recentlyPlayed.removeObserver(observer)
                            }
                            playedRepository.recentlyPlayed.observeForever(observer)
                        } else {
                            Log.e(
                                "LAST_PARENT_ID",
                                preferences.getString(Constants.LAST_PARENT_ID, "NOT FOUND")!!
                            )
                            browser.getChildren(lastParendId, 0, Int.MAX_VALUE, null)
                                .also { libraryResultListenableFuture ->
                                    libraryResultListenableFuture.addListener({
                                        val result = libraryResultListenableFuture.get()!!

                                        if (result.value != null) {
                                            val children = result.value!!

                                            val lastMediaId = preferences.getString(
                                                Constants.LAST_ID,
                                                children.firstOrNull()?.mediaId ?: ""
                                            )
                                            val lastPosition =
                                                preferences.getInt(Constants.LAST_POSITION, 0)
                                            val indexOfLastMediaId =
                                                children.indexOfFirst { mediaItem -> mediaItem.mediaId == lastMediaId }
                                            val mediaItemstoSet = children.toTypedArray()
                                            val startIndex =
                                                if (indexOfLastMediaId != -1) indexOfLastMediaId else 0
                                            val currentItem =
                                                children.find { it.mediaId == lastMediaId }
                                            _mediaItems.postValue(children)
                                            _currentItem.postValue(currentItem)
                                            updateLikeButton(currentItem)
                                            _mediaPosition.postValue(lastPosition)

                                            playAll(lastMediaId ?: children.first().mediaId, children, false, Integer.toUnsignedLong(lastPosition))
                                            /*browser.setMediaItems(
                                                children,
                                                startIndex,
                                                Integer.toUnsignedLong(lastPosition)
                                            )*/
                                        }
                                    }, ContextCompat.getMainExecutor(application))
                                }
                        }
                        subscribe(lastParendId, null)
                        playbackState.observeForever(playbackStateObserver)
                        repeatMode.observeForever(repeatObserver)
                        shuffleMode.observeForever(shuffleObserver)
                    }
                    if (browser.playbackState == Player.STATE_IDLE) {
                        browser.prepare()
                    }
                }
            }

            isControllerConnected.observeForever { connected ->
                if (connected) {
                    controller = globalController

                    Log.e("CONTROLLER VOL", "CONTROLLER VOL: " + controller.deviceVolume.toString())

                    /*controller.addListener(playerListener)*/

                }
            }
        }

    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            updateLikeButton(mediaItem)
            val mediaList = mutableListOf<MediaItem>()
            updateListen = true
            for (i in 0 until browser.mediaItemCount) {
                mediaList.add(browser.getMediaItemAt(i))
            }
            /*_mediaItems.postValue(mediaList)*/
            nowPlaying.postValue(mediaItem)
            _currentItem.postValue(mediaItem)
            /*if (mediaItem != null) {
                addToRecentlyPlayed(mediaItem, browser.isPlaying)
            }*/
            persistPosition()
            try {
                _contentLength.postValue(browser.duration.toInt())
            } catch (_: Exception) {

            }
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                _contentLength.postValue(browser.duration.toInt())
            }
            if (mediaItems.value != null && browser.currentMediaItem != null) {
                currentIndex = mediaItems.value!!.indexOf(mediaItems.value!!.find { it.mediaId == browser.getMediaItemAt(0).mediaId })
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
            val item = nowPlaying.value?.let {
                MediaItem.Builder()
                    .setMediaId(it.mediaId)
                    .setUri(it.requestMetadata.mediaUri)
                    .setMediaMetadata(mediaMetadata)
                    .build()
            }
            nowPlaying.postValue(item)
//            _currentItem.value = item
            _currentItem.postValue(item)
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                _contentLength.postValue(browser.duration.toInt())
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            _playbackState.value = playbackState
            if (playWhenReady && browser.isPlaying) {
                updatePosition = true
                _isPlaying.postValue(true)
            } else {
                updatePosition = false
                _isPlaying.postValue(false)
            }
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                _contentLength.postValue(browser.duration.toInt())
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            _playbackState.postValue(playbackState)
            browser.currentMediaItem?.let { addToRecentlyPlayed(it, browser.isPlaying) }
            persistPosition()
            if (playbackState == STATE_READY /*|| playbackState == Player.STATE_BUFFERING*/ && browser.isPlaying) {
                _isPlaying.postValue(true)
                updatePosition = true
                updatePlaybackPosition()
            } else {
                updatePosition = false
                _isPlaying.postValue(false)
            }
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                _contentLength.postValue(browser.duration.toInt())
            }
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            _isPlaying.postValue(playWhenReady)
            try {
                /*_contentLength.value = browser.duration.toInt()*/
                _contentLength.postValue(browser.duration.toInt())
            } catch (_: Exception) {

            }
            if (playWhenReady) {
                updatePosition = true
                updatePlaybackPosition()
                /*browser.currentMediaItem?.let {
                    addToRecentlyPlayed(it, true)
                }*/
            } else {
                updatePosition = false
            }
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                /*_contentLength.value = browser.duration.toInt()*/
                _contentLength.postValue(browser.duration.toInt())

            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            _isPlaying.postValue(isPlaying)
            if (isPlaying) {
                updatePosition = true
                updatePlaybackPosition()
                browser.currentMediaItem?.let {
                    addToRecentlyPlayed(it, true)
                    updateLikeButton(it)
                }
            } else {
                updatePosition = false
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.postValue(repeatMode)
            preferences.edit().putInt(Constants.LAST_REPEAT_MODE, repeatMode).apply()
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING) {
                _contentLength.postValue(browser.duration.toInt())
            }
            super.onRepeatModeChanged(repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            super.onShuffleModeEnabledChanged(shuffleModeEnabled)
            /*_shuffleMode.value = shuffleModeEnabled*/
            _shuffleMode.postValue(shuffleModeEnabled)
        }

        override fun onPlayerError(error: PlaybackException) {
            if (error.message!!.contains("source error", true)) {
                networkFailure.postValue(true)
            }
            if (error.message!!.contains("source error", true)) {
                Toast.makeText(
                    application.applicationContext,
                    error.message + ": Ensure you have an internet connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
            super.onPlayerError(error)
        }

        override fun onPlayerErrorChanged(error: PlaybackException?) {
            super.onPlayerErrorChanged(error)
            if (error?.message != null && error.message!!.contains("source error", true)) {
                networkFailure.postValue(true)
                Toast.makeText(
                    application.applicationContext,
                    error.message + ": Ensure you have an internet connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    /*fun loadInitialData() {
        viewModelScope.launch {
            globalBrowser.getChildren(lastParendId, 0, Int.MAX_VALUE, null).let {
                it.addListener({
                    _mediaItems.postValue(it.get().value)
                }, ContextCompat.getMainExecutor(application.applicationContext))
            }
        }
    }*/

    fun updateLikeButton(mediaItem: MediaItem?) {
        Log.e("CURRENT ITEM LIKE BUTTON", mediaItem?.mediaMetadata?.title.toString())
        job?.cancel()
        mediaItem?.let { item ->
            val key = item.mediaId.replace("[item]", "")
            val tag = "ViewModel Observe Current Item Query"
            val onQuerySnapshot: Consumer<DataStoreQuerySnapshot<Song>> = Consumer<DataStoreQuerySnapshot<Song>> { value ->
                Log.d(tag, "success on snapshot")
                Log.d(tag, "number of songs: " + value.items.size)
                Log.d(tag, "sync status: " + value.isSynced)
                val song: Song? = value.items.find { song -> song.key == key }
                if (song != null && song.key == (nowPlaying.value?.mediaId?.replace("[item]", "")
                        ?: "")
                ) {
                    _upVotesCount.postValue((song.listOfUidUpVotes?.size ?: 0).toStreamCount())
                    _listensCount.postValue((song.listens?.size ?: 0).toStreamCount())

                    currentUser?.let { authUser ->
                        val exists = song.listOfUidUpVotes?.find { upVoteKey -> upVoteKey == authUser.userId }
                        if (exists != null) {
                            _userHaslikedSong.postValue(true)
                        } else {
                            _userHaslikedSong.postValue(false)
                        }
                    }
                }
            }
            val observationStarted = Consumer { _: Cancelable ->
                Log.d(tag, "Success on cancelable")
            }
            val onObservationError = Consumer { value: DataStoreException ->
                Log.d(tag, "error on snapshot $value")
            }
            val onObservationComplete = Action {
                Log.d(tag, "complete")
            }
            val predicate: QueryPredicate = Where.identifier(Song::class.java, key).queryPredicate // Song.KEY.contains(key)
            val querySortBy = QuerySortBy("song", "listofuidupvotes", QuerySortOrder.ASCENDING)
            val options = ObserveQueryOptions(predicate, listOf(querySortBy))

            job = CoroutineScope(Dispatchers.Main + Job()).launch {
                Amplify.DataStore.observeQuery(
                    Song::class.java,
                    options,
                    observationStarted,
                    onQuerySnapshot,
                    onObservationError,
                    onObservationComplete
                )
            }
            job?.start()
        }
    }

    fun playPause() {
        if (browser.isPlaying || browser.playWhenReady) {
            browser.pause()
        } else {
            if (browser.playbackState == STATE_READY || browser.playbackState == Player.STATE_BUFFERING && !browser.playWhenReady) browser.play() else playMediaId(
                currentItem.value
            )
        }
    }

    fun playMediaId(mediaItem: MediaItem?) {
        if (mediaItem == null) return
        val nowPlaying = browser.currentMediaItem ?: browser.currentMediaItem
        val transportControls = browser

        val isPrepared = browser.playWhenReady
        if (isPrepared && mediaItem.mediaId == nowPlaying?.mediaId) {
            /*if (transportControls.playWhenReady && !transportControls.isPlaying) {
                transportControls.play()
            }*/
            transportControls.play()
        } else {
            try {
//                transportControls.play()
                val mediaList = mutableListOf<MediaItem>()
                for (i in 0 until transportControls.mediaItemCount) {
                    mediaList.add(transportControls.getMediaItemAt(i))
                }
                Log.e("INdex", mediaList.indexOf(mediaItem).toString())
                transportControls.seekToDefaultPosition(mediaList.indexOf(mediaList.find { it.mediaId == mediaItem.mediaId }))
                /*transportControls.setMediaItems(mediaList,
                    mediaList.indexOf(mediaItem),
                    0
                )*/ // .playFromMediaId(mediaId, null)
                transportControls.repeatMode = repeatMode.value!!
                transportControls.shuffleModeEnabled = shuffleMode.value!!
                transportControls.playWhenReady = true
            } catch (e: Exception) {
                e.printStackTrace()
                /*val mediaList = mutableListOf<MediaItem>()
                for (i in 0..transportControls.mediaItemCount) {
                    mediaList.add(transportControls.getMediaItemAt(i))
                }
                transportControls.seekTo(
                    mediaList.indexOf(mediaItem),
                    0
                ) // .playFromMediaId(mediaId, null)
                transportControls.repeatMode = repeatMode.value!!
                transportControls.shuffleModeEnabled = shuffleMode.value!!*/
            }
        }
    }

    fun playAlbum(album: Album, playId: String = Constants.PLAY_FIRST) {
        val parentId = lastParendId
        val list = mediaItems.value
        if (parentId == album.key && list != null) {
            playMediaId(getItemFrmPlayId(playId, list))
        } else {
            playMediaAfterLoad = playId
            browser.unsubscribe(parentId)
            subscribe(album.key, null)
        }
    }

    private fun asyncLoadPreviousPlaylistItems() {
        if (browser.currentMediaItem == null) return
        if (mediaItems.value == null) return
        if (browser.currentMediaItemIndex > 2) return
        Log.e("current item id", browser.currentMediaItem!!.mediaId)
        Log.e("media item id of first item", mediaItems.value!![0].mediaId)
        if (browser.currentMediaItem!!.mediaId != mediaItems.value!![0].mediaId) {
            if ((currentIndex - 3) >= 0) {
                browser.addMediaItems(0, mediaItems.value!!.subList(currentIndex - 3, currentIndex))
            } else if ((currentIndex - 2) >= 0) {
                browser.addMediaItems(0, mediaItems.value!!.subList(currentIndex - 2, currentIndex))
            } else {
                if (currentIndex > 0) {
                    browser.addMediaItem(
                        0,
                        mediaItems.value!![currentIndex-1]
                    )
                } else return

            }
        }
    }

    fun asyncLoadNextPlaylistItems() {
        if (browser.currentMediaItem == null) return
        if (mediaItems.value == null) return
        if ((browser.mediaItemCount - browser.currentMediaItemIndex) > 3) return

        if (browser.currentMediaItem!!.mediaId != mediaItems.value!![mediaItems.value!!.size-1].mediaId) {
            val lastPos = mediaItems.value!!.indexOf(mediaItems.value!!.find { it.mediaId == browser.getMediaItemAt(browser.mediaItemCount-1).mediaId })
            if (lastPos < mediaItems.value!!.size - 1) {
                browser.addMediaItem(mediaItems.value!![lastPos + 1])
            } else return
        }
    }

    fun playAll(playId: String = Constants.PLAY_RANDOM, list: List<MediaItem>? = mediaItems.value, playWhenReady: Boolean = true, lastPosition: Long = C.TIME_UNSET) {
        if (list == null) return
        _mediaItems.postValue(list!!)
        val chunkSize = 50
        val totalItemsCount = list.size
        var job: Job? = null

        job = CoroutineScope(Dispatchers.IO + Job()).launch {
            val pos = if (list.indexOf(list.find { it.mediaId == playId }) < 0) 0 else list.indexOf(list.find { it.mediaId == playId })
            currentIndex = pos
            withContext(Dispatchers.Main) {
                Log.e("PLAY ALL POS", pos.toString())
                Log.e("PLAY ALL START INDEX", (if (pos < 0) 0 else if (pos > 0) pos-1 else pos).toString())
                Log.e("PLAY ALL TO INDEX", (if (((list.size-1)-pos) > 3) pos+3 else list.size).toString())
                Log.e("PLAY ALL SEEK POSITION", (if (pos > 0 && pos != (list.size - 1)) 1 else 0).toString())
                browser.setMediaItems(list.subList(if (pos > 0 && pos != (list.size-1)) pos-1 else pos, if (((list.size-1)-pos) > 3) pos+3 else list.size), if (pos > 0 && pos != (list.size - 1)) 1 else 0, lastPosition)
                browser.prepare()
                browser.playWhenReady = playWhenReady
            }
            /*for (startIndex in 0 until totalItemsCount step chunkSize) {
                val endIndex = minOf(startIndex + chunkSize, totalItemsCount)
                val currentChunk = list.subList(startIndex, endIndex)
                withContext(Dispatchers.Main) {
                    browser.addMediaItems(currentChunk)
                }
            }
            withContext(Dispatchers.Main) {
                browser.removeMediaItem(pos+1)
                browser.moveMediaItem(0, pos)
            }*/
        }

        /*var job: Job? = null
        kotlin.run {
            Log.e("LAST_PARENT_ID", preferences.getString(Constants.LAST_PARENT_ID, "NOT FOUND")!!)

            if (browser.isCommandAvailable(COMMAND_GET_TIMELINE) && browser.mediaItemCount != 0 && browser.getMediaItemAt(
                    0
                ).mediaId == (list?.get(0)?.mediaId
                    ?: NOTHING_PLAYING.mediaId)
            ) {
                try {
                    browser.seekTo(list!!.indexOf(list.find { it.mediaId == playId }), 0)
                } catch (e: Exception) {
                    viewModelScope.launch {
                        *//*job = CoroutineScope(Dispatchers.Main + Job()).launch {
                            if (list!!.size > 5) {

                            }
                        }*//*
                        browser.setMediaItems(
                            list!!,
                            list.indexOf(list.find { it.mediaId == playId }),
                            C.TIME_UNSET
                        )
                    }
                }
            } else {
                viewModelScope.launch {
                    browser.setMediaItems(
                        list!!,
                        list.indexOf(list.find { it.mediaId == playId }),
                        C.TIME_UNSET
                    )
                }
            }

            if (browser.playbackState == Player.STATE_IDLE) {
                browser.prepare()
            }
            browser.play()
            playMediaAfterLoad = playId

            Log.e("CURRENT ITEM", browser.currentMediaItem?.mediaId ?: "None")

        }*/
    }

    fun seek(time: Long) {
        val transportControls = browser
        transportControls.seekTo(time)
        preferences.edit().putLong(Constants.LAST_POSITION, time).apply()
    }

    fun setShuffleMode() {
        browser.shuffleModeEnabled = !browser.shuffleModeEnabled
        preferences.edit().putBoolean(Constants.LAST_SHUFFLE_MODE, browser.shuffleModeEnabled)
            .apply()
    }

    fun setRepeatMode() {
        browser.repeatMode = when (browser.repeatMode) {
            REPEAT_MODE_OFF -> REPEAT_MODE_ONE
            REPEAT_MODE_ONE -> REPEAT_MODE_ALL
            else -> REPEAT_MODE_OFF
        }
        preferences.edit().putInt(Constants.LAST_REPEAT_MODE, browser.repeatMode).apply()
    }

    fun skipToNext() {
        asyncLoadNextPlaylistItems()
        if (browser.playbackState == STATE_READY) {
            browser.seekToNextMediaItem()
            /*browser.playWhenReady = true*/
        } else {
            _mediaItems.value?.let {
                val i = it.indexOf(currentItem.value)
                // Only skip to the next item if the current item is not the last item in the list
                if (i != (it.size - 1)) _currentItem.postValue(it[(i + 1)])
            }
        }
    }

    fun skipToPrevious() {
        asyncLoadPreviousPlaylistItems()
        if (browser.playbackState == STATE_READY) {
            browser.seekToPreviousMediaItem() //.skipToPrevious()
            /*browser.playWhenReady = true*/
        } else {
            _mediaItems.value?.let {
                val i = it.indexOf(currentItem.value)
                // Only skip to the previous item if the current item is not first item in the list
                if (i > 1) _currentItem.postValue(it[(i - 1)])
            }
        }
    }

    fun upVoteSong() {
        if (!userHasLikedSong.value!!) {
            _userHaslikedSong.postValue(true)
        } else {
            _userHaslikedSong.postValue(false)
        }
        if (currentUser != null) {
            val currentKey = browser.currentMediaItem?.mediaId?.replace("[item]", "")
            currentKey?.let { key ->
                Amplify.DataStore.query(
                    Song::class.java,
                    Where.identifier(Song::class.java, key),
                    {
                        if (it.hasNext()) {
                            val original = it.next()
                            val upVotes = original.listOfUidUpVotes
                            val vote = upVotes.find { voteKey -> voteKey == currentUser!!.userId }
                            if (vote == null) {
                                upVotes.add(currentUser!!.userId)
                            } else {
                                upVotes.remove(currentUser!!.userId)
                            }
                            val modifiedSong = original.copyOfBuilder()
                                .listOfUidUpVotes(upVotes)
                                .build()
                            Amplify.DataStore.save(
                                modifiedSong,
                                { updatedSong ->
                                    if (vote == null) {
                                        Log.e(
                                            "UPVOTE SUCCESS",
                                            "Successfully upVoted ${updatedSong.item().name}"
                                        )
                                    } else {
                                        Log.e(
                                            "REMOVE VOTE SUCCESS",
                                            "Successfully removed vote from ${updatedSong.item().name}"
                                        )
                                    }
                                },
                                { dataStoreException ->
                                    if (!userHasLikedSong.value!!) {
                                        _userHaslikedSong.postValue(false)
                                    } else {
                                        _userHaslikedSong.postValue(true)
                                    }
                                    Toast.makeText(
                                        application.applicationContext,
                                        dataStoreException.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    Log.e("UPVOTE EXCEPTION", dataStoreException.message.toString())
                                }
                            )

                        }
                    },
                    {
                        Toast.makeText(
                            application.applicationContext,
                            it.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        } else {
            Toast.makeText(application.applicationContext, "You need to be logged in to upvote a song", Toast.LENGTH_LONG).show()
        }
    }

    private val NOTHING_PLAYING: MediaItem = MediaItem.Builder()
        .setMediaId("")
        .setMediaMetadata(MediaMetadata.Builder().setAlbumTitle("Nothing playing yet").build())
        .build()

    // When the session's [PlaybackStateCompat] changes, the [mediaItems] needs to be updated
    private val playbackStateObserver = Observer<Int> {
        val state = it
        val metadata = browser.currentMediaItem ?: NOTHING_PLAYING
        /*_mediaItems.postValue(updateState(state, metadata))*/
    }

    // When the session's [MediaMetadataCompat] changes, the [mediaItems] needs to be updated
    /*private val mediaMetadataObserver = Observer<MediaItem> {
        val playbackState = controller.playbackState
        val metadata = it
        _mediaItems.postValue(updateState(playbackState, metadata))
    }*/

    private val shuffleObserver = Observer<Boolean>(_shuffleMode::postValue)

    private val repeatObserver = Observer<Int>(_repeatMode::postValue)

    private fun updateState(state: Int, metadata: MediaItem):
            List<MediaItem> {
        val items =
            (_mediaItems.value?.map { it.copy(isPlaying = it.mediaId == metadata.mediaId && state == controller.playbackState) }
                ?: emptyList())

        val currentItem = if (items.isEmpty()) {
            // Only update media item if playback has started
            if (state == STATE_READY || state == Player.STATE_BUFFERING) {
                MediaItem.Builder()
                    .setMediaId(metadata.mediaId)
                    .setUri(metadata.requestMetadata.mediaUri)
                    .setMediaMetadata(metadata.mediaMetadata)
                    .build()
                /*MediaItemData(metadata, controller.isPlaying, controller.isLoading)*/
            } else {
                null
            }
        } else {
            // Only update media item once we have duration available
            if (items.isNotEmpty()) {
                val matchingItem = items.firstOrNull { it.mediaId == metadata.mediaId }
                matchingItem?.apply {
                    mediaMetadata.extras!!.putBoolean("isPlaying", browser.isPlaying)
                    mediaMetadata.extras!!.putBoolean("isBuffering", browser.isPlaying)
                    mediaMetadata.extras!!.putBoolean("isPlaying", browser.isPlaying)
                    /*isPlaying = state.isPlaying
                    isBuffering = state.isBuffering
                    duration = metadata.duration*/
                }
            } else null
        }

        // Update synchronously so addToRecentlyPlayed can pick up a valid currentItem
        if (currentItem != null) _currentItem.postValue(currentItem)
        _playbackState.postValue(state)
        if (state == STATE_READY && browser.isPlaying) {
            updatePlaybackPosition()
        } else {
            updatePosition = false
        }
        return items
    }

    private fun subscribe(parentId: String, params: LibraryParams?) {
        browser.subscribe(parentId, params).addListener({
            browser.getChildren(parentId, 0, Int.MAX_VALUE, params)
        }, MoreExecutors.directExecutor())
        /*preferences.edit().putString(Constants.LAST_PARENT_ID, parentId).apply()*/
    }


    private fun getItemFrmPlayId(playId: String, items: List<MediaItem>): MediaItem? {
        return when (playId) {
            Constants.PLAY_FIRST -> items.firstOrNull()
            Constants.PLAY_RANDOM -> items.random()
            else -> items.firstOrNull { it.mediaId == playId }
        }
    }

    private fun isItemPlaying(mediaId: String): Boolean {
        val isActive = mediaId == controller.currentMediaItem?.mediaId
        val isPlaying = controller.isPlaying
        return isActive && isPlaying
    }

    private fun isItemBuffering(mediaId: String): Boolean {
        val isActive = mediaId == controller.currentMediaItem?.mediaId
        val isBuffering = controller.playbackState == Player.STATE_BUFFERING
        return isActive && isBuffering
    }

    private fun addToRecentlyPlayed(metadata: MediaItem, isPlaying: Boolean) {
        if (isPlaying) {
            serviceScope.launch {
                val played = RecentlyPlayed(metadata)
                recentlyPlayedDatabase.withTransaction {
                    playedRepository.insert(played)
                    playedRepository.trim()
                }
                preferences.edit().putString(Constants.LAST_ID, metadata.mediaId).apply()
            }

        }
    }

    private fun persistPosition() {
        if (browser.playbackState == STATE_READY) {
            preferences.edit().putInt(Constants.LAST_POSITION, mediaPosition.value ?: 0).apply()
        }
    }

    inner class ListenItemNode(var item: String) {
        private val children: MutableList<String> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(listensMap[childID]!!.item)
        }

        fun getChildren(): List<String> {
            return ImmutableList.copyOf(children)
        }

    }
    private fun updateListens() {
        val currentKey = browser.currentMediaItem?.mediaId?.replace("[item]", "")
        currentKey?.let { key ->
            Amplify.DataStore.query(
                Song::class.java,
                Where.identifier(Song::class.java, key),
                {
                    if (it.hasNext()) {
                        val original = it.next()
                        val listens = original.listens
                        val listen = currentUser?.userId ?: "Anonymous${UUID.randomUUID().toString().substring(0,3)}"
                        if (!listensMap.containsKey("listens")) {
                            listensMap["listens"] = ListenItemNode("listens")
                            listensMap[session_id]!!.addChild("listens")
                        }
//                        listensMap["listens"]!!.addChild(listen)
                        val listensList = listensMap[listensMap[session_id]!!.getChildren().find { l -> l == "listens" }]!!.getChildren()
                        if (listensList.contains(listen)) {
                            Log.e("ALREADY LISTENED", "ALREADY LISTENED")
                        } else {
                            Log.e("SHOULD LISTEN", "SHOULD LISTEN")
                        }
                        listens.add(listen)
                        val modifiedSong = original.copyOfBuilder()
                            .listens(listens)
                            .build()
                        Amplify.DataStore.save(
                            modifiedSong,
                            { updatedSong ->
                                Log.e(
                                    "LISTEN ADD SUCCESS",
                                    "Successfully added listen to ${updatedSong.item().name}"
                                )
                                updateListen = false
                            },
                            { dataStoreException ->
                                Toast.makeText(
                                    application.applicationContext,
                                    dataStoreException.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("LISTEN ADD EXCEPTION", dataStoreException.message.toString())
                                updateListen = false
                            }
                        )

                    }
                },
                {
                    Toast.makeText(
                        application.applicationContext,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun updatePlaybackPosition(): Boolean = handler.postDelayed({
        Log.e(
            "CONTROLLER POSITION",
            "CONTROLLER POSITION: " + browser.currentPosition + " MEDIA POSITION: " + mediaPosition.value + " SONG DURATION: " + browser.duration + " - " + browser.contentDuration
        )
        val currPosition =
            browser.currentPosition.toInt() // _playbackState.value?.currentPlayBackPosition
        _mediaBufferPosition.postValue(browser.bufferedPosition.toInt())
        if (_mediaPosition.value != currPosition) {
            _mediaPosition.postValue(currPosition)
//            _mediaPosition.postValue(currPosition)
        }
        if ((browser.duration - currPosition) < 1000) {
            asyncLoadNextPlaylistItems()
        }
        if (updateListen && currPosition > 3000) {
            updateListens()
            updateListen = false
        }
        if (updatePosition) updatePlaybackPosition() else Log.e(
            "CONTROLLER POSITION",
            "CONTROLLER POSITION STOPED"
        )
    }, POSITION_UPDATE_INTERVAL_MILLIS)

    /*timer.schedule(object : TimerTask() {
        override fun run() {
            viewModelScope.launch {
                val currPosition = controller.currentPosition.toInt() // _playbackState.value?.currentPlayBackPosition
                if (_mediaPosition.value != currPosition) {
                    _mediaPosition.value = currPosition
//            _mediaPosition.postValue(currPosition)
                }
                if (controller.duration.toInt() != contentLength.value) {
                    _contentLength.postValue(controller.duration.toInt())
                }
                *//*if (updatePosition)
                    updatePlaybackPosition()*//*
            }

        }

    }, 0, 10)*/

    override fun onCleared() {
        try {
            globalBrowser.release()
        } catch(e: Exception) {
            e.printStackTrace()
        }

        browser.release()
        controller.release()
        timer.cancel()
        preferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onCleared()
    }


    private val lastParendId: String
        get() = preferences.getString(
            Constants.LAST_PARENT_ID,
            "null"
        )!!
}

private fun MediaItem.copy(isPlaying: Boolean): MediaItem {
    val metaData = MediaMetadata.Builder()
        .setTitle(mediaMetadata.title)
        .setSubtitle(mediaMetadata.subtitle)
        .setArtist(mediaMetadata.artist)
        .setAlbumTitle(mediaMetadata.albumTitle)
        .setArtworkUri(mediaMetadata.artworkUri)
        .setExtras(Bundle().apply { putBoolean("isPlaying", isPlaying) })
        .build()
    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(requestMetadata.mediaUri)
        .setMediaMetadata(metaData)
        .build()
}

private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L

interface OnNowPlayingChangedListener {
    fun onNowPlayingChanged()
}
