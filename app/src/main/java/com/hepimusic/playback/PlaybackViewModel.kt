package com.hepimusic.playback

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService.LibraryParams
import com.bumptech.glide.RequestManager
import com.google.common.util.concurrent.MoreExecutors
import com.hepimusic.common.Constants
import com.hepimusic.main.albums.Album
import com.hepimusic.main.common.view.MyBaseViewModel
import com.hepimusic.main.explore.RecentlyPlayedDatabase
import com.hepimusic.main.explore.RecentlyPlayedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val application: Application,
    val recentlyPlayedDatabase: RecentlyPlayedDatabase,
    val glide: RequestManager
): MyBaseViewModel(application) {

    private val job = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.Main + job)

    lateinit var browser2: MediaBrowser
    lateinit var controller: MediaController
    private val _isControllerInitialized = MutableLiveData<Boolean>().apply { value = false }
    val isControllerInitialized: LiveData<Boolean> = _isControllerInitialized
    private val preferences: SharedPreferences = application.getSharedPreferences("main", Context.MODE_PRIVATE)

    private val playedRepository: RecentlyPlayedRepository
    private val _mediaItems = MutableLiveData<List<MediaItem>>()
    private val _contentLength = MutableLiveData<Int>()
    private val _currentItem = MutableLiveData<MediaItem?>().apply { value = NOTHING_PLAYING }
    private val _isPlaying = MutableLiveData<Boolean>().apply { value = false }
    private val _playbackState = MutableLiveData<Int>().apply { value = Player.STATE_IDLE }
    private val _shuffleMode = MutableLiveData<Boolean>().apply {
        value = preferences.getBoolean(
            Constants.LAST_SHUFFLE_MODE,
            false
        )
    }
    private val _repeatMode = MutableLiveData<Int>().apply {
        value = preferences.getInt(
            Constants.LAST_REPEAT_MODE,
            Player.REPEAT_MODE_ALL
        )
    }
    private val _mediaPosition =
        MutableLiveData<Int>().apply { value = preferences.getInt(Constants.LAST_POSITION, 0) }
    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())
    private val timer = Timer()
    private var playMediaAfterLoad: String? = null

    val mediaItems: LiveData<List<MediaItem>> = _mediaItems
    val contentLength: LiveData<Int> = _contentLength
    val currentItem: LiveData<MediaItem?> = _currentItem
    val isPlaying: LiveData<Boolean> = _isPlaying
    val playbackState: LiveData<Int> = _playbackState
    val mediaPosition: LiveData<Int> = _mediaPosition
    val shuffleMode: LiveData<Boolean> = _shuffleMode
    val repeatMode: LiveData<Int> = _repeatMode

    val nowPlaying = MutableLiveData<MediaItem>().apply { postValue(NOTHING_PLAYING) }
    val networkFailure = MutableLiveData<Boolean>().apply { postValue(false) }

    lateinit var browser: MediaBrowser

    init {
        _isPlaying.postValue(false)
        val recentlyPlayed = recentlyPlayedDatabase.dao // RecentlyPlayedRoomDatabase.getDatabase(application).recentDao()
        playedRepository = RecentlyPlayedRepository(recentlyPlayed)
    }

    fun init() {
        if (isBrowserConnected.value == true) {
            /*browserFuture.addListener({
                if (browserFuture.isDone && !browserFuture.isCancelled) browser = browserFuture.get()
            }, ContextCompat.getMainExecutor(application.applicationContext))

            controllerFuture.addListener({
                if (controllerFuture.isDone && !controllerFuture.isCancelled) {
                    controller = controllerFuture.get()
                    _isControllerInitialized.postValue(true)
                }
            }, MoreExecutors.directExecutor())*/

            isControllerConnected.observeForever { connected ->
                if (connected) {
                    controller = globalController

                    Log.e("CONTROLLER VOL", "CONTROLLER VOL: "+controller.deviceVolume.toString())

                    controller.addListener(object : Player.Listener {
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            nowPlaying.postValue(mediaItem)
                            try {
                                _contentLength.postValue(controller.duration.toInt())
                            } catch (_: Exception){

                            }
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                            super.onMediaItemTransition(mediaItem, reason)
                        }

                        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                            val item = nowPlaying.value?.let {
                                MediaItem.Builder()
                                    .setMediaId(it.mediaId)
                                    .setUri(it.requestMetadata.mediaUri)
                                    .setMediaMetadata(mediaMetadata)
                                    .build()
                            }
                            nowPlaying.postValue(item)
                            _currentItem.postValue(item)
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                            super.onMediaMetadataChanged(mediaMetadata)
                        }

                        override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
                            super.onPlaylistMetadataChanged(mediaMetadata)
                        }

                        override fun onIsLoadingChanged(isLoading: Boolean) {
                            super.onIsLoadingChanged(isLoading)
                        }

                        override fun onLoadingChanged(isLoading: Boolean) {
                            super.onLoadingChanged(isLoading)
                        }

                        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                            super.onPlayerStateChanged(playWhenReady, playbackState)
                            if (playWhenReady && controller.isPlaying) {
                                _isPlaying.postValue(true)
                            } else {
                                _isPlaying.postValue(false)
                            }
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            _playbackState.postValue(playbackState)
                            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_BUFFERING && controller.isPlaying) {
                                _isPlaying.postValue(true)
                            } else {
                                _isPlaying.postValue(false)
                            }
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                            super.onPlaybackStateChanged(playbackState)
                        }

                        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                            _isPlaying.value = playWhenReady
                            try {
                                _contentLength.postValue(controller.duration.toInt())
                            } catch (_: Exception){

                            }
                            updatePlaybackPosition()
                            super.onPlayWhenReadyChanged(playWhenReady, reason)
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            super.onIsPlayingChanged(isPlaying)
                        }

                        override fun onRepeatModeChanged(repeatMode: Int) {
                            _repeatMode.postValue(repeatMode)
                            preferences.edit().putInt(Constants.LAST_REPEAT_MODE, repeatMode).apply()
                            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING) {
                                _contentLength.postValue(controller.duration.toInt())
                            }
                            super.onRepeatModeChanged(repeatMode)
                        }

                        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                            super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            if (error.message!!.contains("network", true)) {
                                networkFailure.postValue(true)
                            }
                            super.onPlayerError(error)
                        }

                        override fun onEvents(player: Player, events: Player.Events) {

                            super.onEvents(player, events)
                        }

                        override fun onPlayerErrorChanged(error: PlaybackException?) {
                            super.onPlayerErrorChanged(error)
                        }

                        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                            super.onPlaybackParametersChanged(playbackParameters)
                        }

                        override fun onMetadata(metadata: Metadata) {
                            super.onMetadata(metadata)
                        }
                    })

                }
            }

            isBrowserConnected.observeForever { connected ->
                if (connected) {
                    browser2 = globalBrowser

                    browser = browser2

                    browser.also { browser ->
                        browser.getChildren(lastParendId, 0, kotlin.Int.MAX_VALUE, null).also {
                            it.addListener({
                                val result = it.get()!!
                                val children = result.value!!
                                _mediaItems.postValue(children)
                            }, ContextCompat.getMainExecutor(application))
                        }
                        subscribe(lastParendId, null)
                        playbackState.observeForever(playbackStateObserver)
                        repeatMode.observeForever(repeatObserver)
                        shuffleMode.observeForever(shuffleObserver)
                    }
                }
            }

        }
    }

    fun loadInitialData() {
        backgroundScope.launch {
            globalBrowser.getChildren(lastParendId, 0, Int.MAX_VALUE, null).let {
                it.addListener({
                    _mediaItems.postValue(it.get().value)
                }, ContextCompat.getMainExecutor(application.applicationContext))
            }
        }
    }

    fun playPause() {
        if (controller.isPlaying || controller.playWhenReady) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_READY || controller.playbackState == Player.STATE_BUFFERING && !controller.playWhenReady) controller.play() else playMediaId(currentItem.value)
        }
    }

    fun playMediaId(mediaItem: MediaItem?) {
        if (mediaItem == null) return
        val nowPlaying = controller.currentMediaItem ?: browser.currentMediaItem
        val transportControls = controller

        val isPrepared = browser.playWhenReady
        if (isPrepared && mediaItem.mediaId == nowPlaying?.mediaId) {
            if (transportControls.playWhenReady && !transportControls.isPlaying) {
                transportControls.play()
            }
        } else {
            transportControls.seekTo(mediaItems.value?.indexOf(mediaItem)!!, 0) // .playFromMediaId(mediaId, null)
            transportControls.repeatMode = repeatMode.value!!
            transportControls.shuffleModeEnabled = shuffleMode.value!!
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

    fun playAll(playId: String = Constants.PLAY_RANDOM, list: List<MediaItem>? = mediaItems.value) {
        val parentId = lastParendId
//        val list = mediaItems.value
        if (controller.mediaItemCount > 0) {
            try {
                controller.seekTo(list!!.indexOf(list.find { it.mediaId == playId }), 0)
            } catch (e: Exception) {
                controller.setMediaItems(
                    list!!,
                    list.indexOf(list.find { it.mediaId == playId }),
                    0
                )
            }
        } else {
            controller.setMediaItems(
                list!!,
                list.indexOf(list.find { it.mediaId == playId }),
                0
            )
        }
//        browser.seekTo(0, 0)
        controller.prepare()
        controller.setPlaybackSpeed(1f)
        controller.playWhenReady = true
//        browser.play()
        Log.e("CURRENT ITEM", controller.currentMediaItem?.mediaId ?: "None")
        /*if (parentId == Constants.SONGS_ROOT && list != null) {
            playMediaId(getItemFrmPlayId(playId, list))
        } else {
            playMediaAfterLoad = playId
            browser.unsubscribe(parentId)
            subscribe(Constants.SONGS_ROOT, null)
        }*/
    }

    fun seek(time: Long) {
        val transportControls = controller
        transportControls.seekTo(time)
        preferences.edit().putLong(Constants.LAST_POSITION, time).apply()
    }

    fun setShuffleMode() {
        browser.shuffleModeEnabled = !browser.shuffleModeEnabled
        preferences.edit().putBoolean(Constants.LAST_SHUFFLE_MODE, browser.shuffleModeEnabled).apply()
    }

    fun setRepeatMode() {
        browser.repeatMode = when (browser.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        preferences.edit().putInt(Constants.LAST_REPEAT_MODE, browser.repeatMode).apply()
    }

    fun skipToNext() {
        if (browser.playbackState == Player.STATE_READY) {
            controller.seekToNextMediaItem()
            controller.playWhenReady = true
        } else {
            _mediaItems.value?.let {
                val i = it.indexOf(currentItem.value)
                // Only skip to the next item if the current item is not the last item in the list
                if (i != (it.size - 1)) _currentItem.postValue(it[(i + 1)])
            }
        }
    }

    fun skipToPrevious() {
        if (browser.playbackState == Player.STATE_READY) {
            controller.seekToNextMediaItem() //.skipToPrevious()
            controller.playWhenReady = true
        } else {
            _mediaItems.value?.let {
                val i = it.indexOf(currentItem.value)
                // Only skip to the previous item if the current item is not first item in the list
                if (i > 1) _currentItem.postValue(it[(i - 1)])
            }
        }
    }

    val NOTHING_PLAYING: MediaItem = MediaItem.Builder()
        .setMediaId("")
        .setMediaMetadata(MediaMetadata.Builder().setAlbumTitle("Nothing playing yet").build())
        .build()

    // When the session's [PlaybackStateCompat] changes, the [mediaItems] needs to be updated
    private val playbackStateObserver = Observer<Int> {
        val state = it ?: Player.STATE_IDLE
        val metadata = controller.currentMediaItem ?: NOTHING_PLAYING
        if (metadata.mediaId != null) {
            _mediaItems.postValue(updateState(state, metadata))
        }
    }

    // When the session's [MediaMetadataCompat] changes, the [mediaItems] needs to be updated
    private val mediaMetadataObserver = Observer<MediaItem> {
        val playbackState = controller.playbackState ?: Player.STATE_IDLE
        val metadata = it ?: NOTHING_PLAYING
        if (metadata.mediaId != null) {
            _mediaItems.postValue(updateState(playbackState, metadata))
        }
    }

    private val shuffleObserver = Observer<Boolean>(_shuffleMode::postValue)

    private val repeatObserver = Observer<Int>(_repeatMode::postValue)

    private fun updateState(state: Int, metadata: MediaItem):
            List<MediaItem> {
        val items = (_mediaItems.value?.map { it.copy(isPlaying = it.mediaId == metadata.mediaId && state == controller.playbackState) } ?: emptyList())

        val currentItem = if (items.isEmpty()) {
            // Only update media item if playback has started
            if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
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
                    mediaMetadata.extras!!.putBoolean("isBu", browser.isPlaying)
                    mediaMetadata.extras!!.putBoolean("isPlaying", browser.isPlaying)
                    /*isPlaying = state.isPlaying
                    isBuffering = state.isBuffering
                    duration = metadata.duration*/
                }
            } else null
        }

        // Update synchronously so addToRecentlyPlayed can pick up a valid currentItem
        if (currentItem != null) _currentItem.value = currentItem
        _playbackState.postValue(state)
        if (state == Player.STATE_READY) updatePlaybackPosition()
        return items
    }

    private fun subscribe(parentId: String, params: LibraryParams?) {
        browser.subscribe(parentId, params).addListener({
            browser.getChildren(parentId, 0, Int.MAX_VALUE, params)
        }, MoreExecutors.directExecutor())
        preferences.edit().putString(Constants.LAST_PARENT_ID, parentId).apply()
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

    private fun updatePlaybackPosition(): Boolean = handler.postDelayed({
//        Log.e("CONTROLLER POSITION", "CONTROLLER POSITION: "+controller.currentPosition+" MEDIA POSITION: "+mediaPosition.value+" SONG DURATION: "+controller.duration+" - "+controller.contentDuration)
        val currPosition = controller.currentPosition.toInt() // _playbackState.value?.currentPlayBackPosition
        if (_mediaPosition.value != currPosition) {
            _mediaPosition.value = currPosition
//            _mediaPosition.postValue(currPosition)
        }
        if (updatePosition)
            updatePlaybackPosition()
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
        globalBrowser.release()
        browser.release()
        controller.release()
        timer.cancel()
        super.onCleared()
    }


    private val lastParendId: String get() = preferences.getString(Constants.LAST_PARENT_ID, Constants.SONGS_ROOT)!!
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

private const val POSITION_UPDATE_INTERVAL_MILLIS = 1L

