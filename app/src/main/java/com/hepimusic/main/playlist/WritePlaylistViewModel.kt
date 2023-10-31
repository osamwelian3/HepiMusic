package com.hepimusic.main.playlist

import android.app.Application
import android.app.appsearch.SearchSuggestionResult.Builder
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.hepimusic.HepiApplication
import com.hepimusic.R
import com.hepimusic.main.common.utils.ImageUtils
import com.hepimusic.main.common.utils.UriFileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class WritePlaylistViewModel @Inject constructor(val application: Application, playlistDatabase: PlaylistDatabase, playlistItemsDatabase: PlaylistItemsDatabase) : ViewModel() {

    val playlistRepository: PlaylistRepository = PlaylistRepository(playlistDatabase.dao)
    val playlistItemsRepository: PlaylistItemsRepository = PlaylistItemsRepository(playlistItemsDatabase.dao)

    private val _data = MutableLiveData<WriteResult>()
    internal val data: LiveData<WriteResult> get() = _data

    fun createPlaylist(playlistName: String, tempThumbUri: Uri?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                /*val uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
                val values = ContentValues(1)
                values.put(MediaStore.Audio.Playlists.NAME, playlistName)
                val playlistUri = getApplication<HepiApplication>().contentResolver.insert(uri, values)

                if (playlistUri?.toString().isNullOrEmpty()) {
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                    return@withContext
                }*/
                try {
                    val playlist = Playlist("[playlist]$playlistName", playlistName, System.currentTimeMillis())
                    playlistRepository.insert(playlist)
                    val bitmap = Glide.with(application)
                        .asBitmap()
                        .load(tempThumbUri)
                        .submit().get()
                    write(playlist, bitmap)
//                    writeImageFile(playlist, tempThumbUri)
                    _data.postValue(WriteResult(true))
                } catch (e: Exception) {
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                }
            }
        }

    }

    fun editPlaylist(name: String, playlist: Playlist, tempThumbUri: Uri?, deleteImageFile: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                /*val uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
                val values = ContentValues(2)
                val where = MediaStore.Audio.Playlists._ID + " =? "
                val whereVal = arrayOf(playlist.id.toString())
                values.put(MediaStore.Audio.Playlists.NAME, name)
                values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis())
                val rowsUpdated = getApplication<App>().contentResolver.update(uri, values, where, whereVal)
                if (rowsUpdated < 1) {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                    return@withContext
                }*/
                try {
                    val newPlaylist = Playlist(playlist.id, name, System.currentTimeMillis())
                    playlistRepository.insert(newPlaylist)

                    if (deleteImageFile) {
                        remove(playlist)
                        val bitmap = Glide.with(application)
                            .asBitmap()
                            .load(tempThumbUri)
                            .submit().get()
                        write(newPlaylist, bitmap)
                    }
//                    writeImageFile(playlist, tempThumbUri, deleteImageFile)
                    _data.postValue(WriteResult(true))
                } catch (e: Exception) {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                /*val where = MediaStore.Audio.Playlists._ID + "=?"
                val whereVal = arrayOf(playlist.id.toString())
                val rows = getApplication<Application>().contentResolver
                    .delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal)
                if (rows > 0) {
                    writeImageFile(playlist, deleteImageFile = true)
                    _data.postValue(WriteResult(true, R.string.sth_deleted))
                } else {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }*/
                try {
                    playlistRepository.deletePlaylist(playlist.id)
//                    writeImageFile(playlist, deleteImageFile = true)
                    remove(playlist)
                    _data.postValue(WriteResult(true, R.string.sth_deleted))
                } catch (e: Exception) {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            }
        }
    }


    @WorkerThread
    private fun writeImageFile(playlist: Playlist, tempThumbUri: Uri? = null, deleteImageFile: Boolean = false) {
        val app = application
        val resultPath = ImageUtils.getImagePathForModel(playlist, app)

        if (deleteImageFile) {
            val file = File(resultPath)
            if (file.exists()) file.delete()
        }

        if (tempThumbUri == null) return

        val path = UriFileUtils.getPathFromUri(app, tempThumbUri)
        if (path != null) {
            if (resultPath != null) {
                ImageUtils.resizeImageIfNeeded(path, 300.0, 300.0, 80, resultPath)
            }
        }
        return
    }

    @WorkerThread
    fun write(playlist: Playlist, bitmap: Bitmap) {
        val outputStream: FileOutputStream
        try {
            outputStream = application.openFileOutput(playlist.name, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_LONG).show()
        }
    }

    @WorkerThread
    fun remove(playlist: Playlist) {
        try {
            val file = File(application.applicationContext.filesDir, playlist.name)
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val resolver = application.contentResolver
                    val uri = FileProvider.getUriForFile(application.applicationContext, "${application.applicationContext.packageName}.provider", file)
                    /*val uri = MediaStore.Files.getContentUri("external")*/
                    val selection = MediaStore.Files.FileColumns.DATA + "=?"
                    val selectionArgs = arrayOf(file.absolutePath)
                    resolver.delete(uri, /*selection*/null, /*selectionArgs*/ null)
                    Log.e("DELETED", "PLAYLIST IMAGE DELETED")
                } else {
                    file.delete()
                    Log.e("DELETED", "PLAYLIST IMAGE DELETED")
                }
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(application.applicationContext, "No image file found for this playlist", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(application.applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clearResult(@StringRes success: Int) {
        _data.postValue(WriteResult(false, success))
    }
}



internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)