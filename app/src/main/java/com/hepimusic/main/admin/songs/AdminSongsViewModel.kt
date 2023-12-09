package com.hepimusic.main.admin.songs

import android.app.Application
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Song
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.operation.StorageUploadFileOperation
import com.amplifyframework.storage.s3.options.AWSS3StorageUploadFileOptions
import com.hepimusic.R
import com.hepimusic.main.admin.common.BaseAdminViewModel
import com.hepimusic.main.common.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AdminSongsViewModel @Inject constructor(
    val application: Application
): BaseAdminViewModel(application) {

    private val observable = AdminSongsObservable()

    fun getObservable(): AdminSongsObservable {
        return observable
    }

    inner class AdminSongsObservable: BaseObservable() {
        private val _data = MutableLiveData<WriteResult>()
        internal val data: LiveData<WriteResult> get() = _data

        // image
        val _imageUri = MutableLiveData<Uri>()
        val imageUri: LiveData<Uri> = _imageUri
        var imageUploadProgress = MutableLiveData<String>()

        // file
        val _fileUri = MutableLiveData<Uri>()
        val fileUri: LiveData<Uri> = _fileUri
        var fileUploadProgress = MutableLiveData<String>()

        val _deleteOldImage = MutableLiveData<Boolean>()
        val deleteOldImage: LiveData<Boolean> = _deleteOldImage

        val _songToEdit = MutableLiveData<Song>()
        val songToEdit: LiveData<Song> = _songToEdit

        // song fields
        val _key = MutableLiveData<String>()

        @Bindable
        val _name = MutableLiveData<String>()

        @Bindable
        val _fileUrl = MutableLiveData<String>()

        @Bindable
        val _fileKey = MutableLiveData<String>()

        @Bindable
        val _thumbnail = MutableLiveData<String>()

        @Bindable
        val _thumbnailKey = MutableLiveData<String>()

        @Bindable
        val _partOf = MutableLiveData<String>()

        @Bindable
        val _selectedCategory = MutableLiveData<String>()

        @Bindable
        val _selectedCreator = MutableLiveData<String>()

        @Bindable
        val _listens = MutableLiveData<List<String>>()

        @Bindable
        val _trendingListens = MutableLiveData<List<String>>()

        @Bindable
        val _listOfUidUpVotes = MutableLiveData<List<String>>()

        @Bindable
        val _listOfUidDownVotes = MutableLiveData<List<String>>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = _name
        val fileUrl: LiveData<String> = _fileUrl
        val fileKey: LiveData<String> = _fileKey
        val thumbnail: LiveData<String> = _thumbnail
        val thumbnailKey: LiveData<String> = _thumbnailKey
        val partOf: LiveData<String> = _partOf
        val selectedCategory: LiveData<String> = _selectedCategory
        val selectedCreator: LiveData<String> = _selectedCreator
        val listens: LiveData<List<String>> = _listens
        val trendingListens: LiveData<List<String>> = _trendingListens
        val listOfUidUpVotes: LiveData<List<String>> = _listOfUidUpVotes
        val listOfUidDownVotes: LiveData<List<String>> = _listOfUidDownVotes

        private fun uploadImage(uuid: String, editing: Boolean = false): StorageUploadFileOperation<*> {
            val key = if (!name.value.isNullOrEmpty()) {
                "images/${name.value}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            } else {
                "images/${uuid}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            }

            val options = AWSS3StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .setUseAccelerateEndpoint(true)
                .build()

            val contentResolver = application.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            val ext =
                mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri.value!!))
            val imageStream: InputStream? = application.contentResolver.openInputStream(
                imageUri.value!!
            )
            val tempFile = File.createTempFile("image", ".image")
            ImageUtils.copyStreamToFile(imageStream!!, tempFile)

            val file = File(tempFile.absolutePath)

            return Amplify.Storage.uploadFile(
                "$key.$ext",
                file,
                options,
                {
                    imageStream.close()
                },
                {
                    imageStream.close()
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                }
            )
        }

        private fun uploadSongFile(uuid: String, editing: Boolean = false): StorageUploadFileOperation<*> {
            val key = if (!name.value.isNullOrEmpty()) {
                "files/${name.value}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            } else {
                "files/${uuid}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            }

            val options = AWSS3StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .setUseAccelerateEndpoint(true)
                .build()

            val contentResolver = application.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            val ext = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(fileUri.value!!))
            val fileStream: InputStream? = application.contentResolver.openInputStream(
                fileUri.value!!
            )
            val tempFile = File.createTempFile("song", ".song")
            ImageUtils.copyStreamToFile(fileStream!!, tempFile)

            val file = File(tempFile.absolutePath)

            return Amplify.Storage.uploadFile(
                "$key.$ext",
                file,
                options,
                {
                    fileStream.close()
                },
                {
                    fileStream.close()
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                }
            )
        }

        fun deleteImage(key: String) {
            Amplify.Storage.remove(
                key,
                {
                    this@AdminSongsViewModel.viewModelScope.launch {
                        Toast.makeText(
                            application,
                            "Old Image Removed. Ready to begin upload.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                {
                    viewModelScope.launch {
                        Toast.makeText(application, it.cause?.message.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            )
        }

        fun deleteSongFile(key: String) {
            Amplify.Storage.remove(
                key,
                {
                    viewModelScope.launch {
                        Toast.makeText(
                            application,
                            "Song file deleted. Purging related items from database.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                {
                    viewModelScope.launch {
                        Toast.makeText(application, it.cause?.message.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            )
        }

        private fun saveSongToDb(song: Song) {
            Amplify.DataStore.save(
                song,
                {
                    _data.postValue(WriteResult(true, R.string.song_saved))
                },
                {
                    _data.postValue(WriteResult(true))
                }
            )
        }

        fun createSong() {
            val uuid = UUID.randomUUID().toString()

            if (imageUri.value != null) {
                val imageUpload = uploadImage(uuid)
                imageUpload.start()
                imageUpload.setOnProgress {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                }

                imageUpload.setOnSuccess { storageUploadFileResult ->
                    _thumbnailKey.postValue(storageUploadFileResult.key.replace("public/", ""))
                    _thumbnail.postValue("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                    val fileUpload = uploadSongFile(uuid)
                    fileUpload.start()
                    fileUpload.setOnProgress {
                        val p = (it.currentBytes / it.totalBytes) * 100
                        fileUploadProgress.postValue("$p%")
                    }

                    fileUpload.setOnSuccess {
                        _fileKey.postValue(it.key.replace("public/", ""))
                        _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                        val song = Song.Builder()
                            .key(uuid)
                            .fileUrl(fileUrl.value)
                            .fileKey(fileKey.value)
                            .name(name.value)
                            .selectedCategory(selectedCategory.value)
                            .thumbnail(thumbnail.value)
                            .thumbnailKey(thumbnailKey.value)
                            .selectedCreator(selectedCreator.value)
                            .build()
                        saveSongToDb(song = song)
                    }
                }
            }

        }

        fun editSong() {
            if (imageUri.value != null) {
                val imageUpload = uploadImage(songToEdit.value!!.key, true)
                imageUpload.start()
                imageUpload.setOnProgress {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                }

                imageUpload.setOnSuccess { storageUploadFileResult ->
                    _thumbnailKey.postValue(storageUploadFileResult.key.replace("public/", ""))
                    _thumbnail.postValue("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                    if (fileUri.value != null) {
                        val fileUpload = uploadSongFile(songToEdit.value!!.key, true)
                        fileUpload.start()
                        fileUpload.setOnProgress {
                            val p = (it.currentBytes / it.totalBytes) * 100
                            fileUploadProgress.postValue("$p%")
                        }

                        fileUpload.setOnSuccess {
                            _fileKey.postValue(it.key.replace("public/", ""))
                            _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                            val song = songToEdit.value!!.copyOfBuilder()
                                .fileUrl(fileUrl.value)
                                .fileKey(fileKey.value)
                                .name(name.value)
                                .selectedCategory(selectedCategory.value)
                                .thumbnail(thumbnail.value)
                                .thumbnailKey(thumbnailKey.value)
                                .selectedCreator(selectedCreator.value)
                                .build()
                            saveSongToDb(song = song)
                        }
                    } else {
                        val song = songToEdit.value!!.copyOfBuilder()
                            .name(name.value)
                            .selectedCategory(selectedCategory.value)
                            .thumbnail(thumbnail.value)
                            .thumbnailKey(thumbnailKey.value)
                            .selectedCreator(selectedCreator.value)
                            .build()
                        saveSongToDb(song = song)
                    }
                }
            } else {
                if (fileUri.value != null) {
                    val fileUpload = uploadSongFile(songToEdit.value!!.key, true)
                    fileUpload.start()
                    fileUpload.setOnProgress {
                        val p = (it.currentBytes / it.totalBytes) * 100
                        fileUploadProgress.postValue("$p%")
                    }

                    fileUpload.setOnSuccess {
                        _fileKey.postValue(it.key.replace("public/", ""))
                        _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                        val song = songToEdit.value!!.copyOfBuilder()
                            .fileUrl(fileUrl.value)
                            .fileKey(fileKey.value)
                            .name(name.value)
                            .selectedCategory(selectedCategory.value)
                            .selectedCreator(selectedCreator.value)
                            .build()
                        saveSongToDb(song = song)
                    }
                } else {
                    val song = songToEdit.value!!.copyOfBuilder()
                        .name(name.value)
                        .selectedCategory(selectedCategory.value)
                        .selectedCreator(selectedCreator.value)
                        .build()
                    saveSongToDb(song = song)
                }
            }

        }

        fun clearResult(@StringRes success: Int) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)