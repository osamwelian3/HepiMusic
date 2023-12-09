package com.hepimusic.main.admin.albums

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
import com.amplifyframework.datastore.generated.model.Album
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
class AdminAlbumsViewModel @Inject constructor(
    val application: Application
): BaseAdminViewModel(application) {

    private val observable = AdminAlbumsObservable()

    fun getObservable(): AdminAlbumsViewModel.AdminAlbumsObservable {
        return observable
    }

    inner class AdminAlbumsObservable: BaseObservable() {
        private val _data = MutableLiveData<WriteResult>()
        internal val data: LiveData<WriteResult> get() = _data

        // image
        val _imageUri = MutableLiveData<Uri>()
        val imageUri: LiveData<Uri> = _imageUri
        var imageUploadProgress = MutableLiveData<String>()

        val _deleteOldImage = MutableLiveData<Boolean>()
        val deleteOldImage: LiveData<Boolean> = _deleteOldImage

        val _albumToEdit = MutableLiveData<Album>()
        val albumToEdit: LiveData<Album> = _albumToEdit

        val _key = MutableLiveData<String>()
        @Bindable
        val _name = MutableLiveData<String>()
        @Bindable
        val _thumbnail = MutableLiveData<String>()
        @Bindable
        val _thumbnailKey = MutableLiveData<String>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = _name
        val thumbnail: LiveData<String> = _thumbnail
        val thumbnailKey: LiveData<String> = _thumbnailKey

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
                    _data.postValue(
                        WriteResult(
                            false,
                            R.string.something_went_wrong
                        )
                    )
                }
            )
        }

        fun deleteImage(key: String) {
            Amplify.Storage.remove(
                key,
                {
                    this@AdminAlbumsViewModel.viewModelScope.launch {
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

        private fun saveAlbumToDb(album: Album) {
            Amplify.DataStore.save(
                album,
                {
                    _data.postValue(
                        WriteResult(
                            true,
                            R.string.album_saved
                        )
                    )
                },
                {
                    _data.postValue(WriteResult(true))
                }
            )
        }

        fun createAlbum() {
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
                    val album = Album.Builder()
                        .key(uuid)
                        .name(name.value)
                        .thumbnail(thumbnail.value)
                        .thumbnailKey(thumbnailKey.value)
                        .build()
                    saveAlbumToDb(album = album)
                }
            }

        }

        fun editAlbum() {
            if (imageUri.value != null) {
                val imageUpload = uploadImage(albumToEdit.value!!.key, true)
                imageUpload.start()
                imageUpload.setOnProgress {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                }

                imageUpload.setOnSuccess { storageUploadFileResult ->
                    _thumbnailKey.postValue(storageUploadFileResult.key.replace("public/", ""))
                    _thumbnail.postValue("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                    val album = albumToEdit.value!!.copyOfBuilder()
                        .name(name.value)
                        .thumbnail(thumbnail.value)
                        .thumbnailKey(thumbnailKey.value)
                        .build()
                    saveAlbumToDb(album = album)
                }
            } else {
                val album = albumToEdit.value!!.copyOfBuilder()
                    .name(name.value)
                    .build()
                saveAlbumToDb(album = album)
            }

        }

        fun clearResult(@StringRes success: Int) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)