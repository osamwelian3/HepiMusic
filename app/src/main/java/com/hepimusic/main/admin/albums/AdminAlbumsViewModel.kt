package com.hepimusic.main.admin.albums

import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.generated.model.Album
import com.amplifyframework.datastore.generated.model.Song
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.operation.StorageUploadFileOperation
import com.amplifyframework.storage.options.StorageRemoveOptions
import com.amplifyframework.storage.s3.options.AWSS3StorageUploadFileOptions
import com.google.gson.Gson
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.main.admin.common.BaseAdminViewModel
import com.hepimusic.main.common.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val _imageUri = MutableLiveData<Uri?>()
        val imageUri: LiveData<Uri?> = _imageUri
        var imageUploadProgress = MutableLiveData<String>()
        var imageUploaded = false

        val _deleteOldImage = MutableLiveData<Boolean>()
        val deleteOldImage: LiveData<Boolean> = _deleteOldImage

        val _albumToEdit = MutableLiveData<Album?>()
        val albumToEdit: LiveData<Album?> = _albumToEdit
        val _editClicked = MutableLiveData<Boolean>()
        val editClicked : LiveData<Boolean> = _editClicked

        // album fields
        val _key = MutableLiveData<String>()

        @Bindable
        var alname = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.alname)
            }

        @Bindable
        val _thumbnail = MutableLiveData<String>()

        @Bindable
        val _thumbnailKey = MutableLiveData<String>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = alname
        val thumbnail: LiveData<String> = _thumbnail
        val thumbnailKey: LiveData<String> = _thumbnailKey

        private fun uploadImage(uuid: String, editing: Boolean = false, completionCallback: ((thumbnailKey: String?) -> Unit)? = null) {
            var key = if (!name.value.isNullOrEmpty()) {
                "images/${name.value}"+if (editing) " v"+UUID.randomUUID().toString().substring(0, 4) else ""
            } else {
                "images/${uuid}"+if (editing) " v"+UUID.randomUUID().toString().substring(0, 4) else ""
            }

            val options = AWSS3StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
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

            val keys = fileKeys.value ?: emptyList()
            if (keys.contains("$key.$ext")) {
                Log.e("EXISTS", "Image with key $key.$ext exists in storage")
                key = key+" v"+ UUID.randomUUID().toString().substring(0, 4)
            }

            Amplify.Storage.uploadFile(
                "$key.$ext",
                file,
                options,
                {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                    if (it.currentBytes == it.totalBytes) {
                        imageUploadProgress.postValue("Image Upload Complete")
                    }
                    Log.e("IMAGE PROGRESS", "Fraction completed: "+it.fractionCompleted.toString()+" currentBytes: "+it.currentBytes.toString()+" totalBytes: "+it.totalBytes.toString())
                },
                {
                    imageStream.close()
                    imageUploaded = true
                    completionCallback?.invoke(it.key)
                },
                {
                    imageStream.close()
                    it.printStackTrace()
                    Log.e("UPLOAD IMAGE ERROR", it.message.toString())
                    if (it.message.toString().contains("Something went wrong with your AWS S3 Storage upload file operation")) {
                        uploadImage(uuid, editing) { imgKey ->
                            completionCallback?.invoke(imgKey)
                        }
                    } else {
                        _data.postValue(
                            WriteResult(
                                false,
                                R.string.sth_went_wrong
                            )
                        )
                    }
                }
            )
        }

        private fun deleteImage(key: String) {
            Amplify.Storage.remove(
                key,
                StorageRemoveOptions.builder()
                    .accessLevel(StorageAccessLevel.PUBLIC)
                    .build(),
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
                    CoroutineScope(Dispatchers.Main).launch {
                        imageUploadProgress.postValue("")
                        imageUploaded = false
                        _data.postValue(
                            WriteResult(
                                true,
                                R.string.album_saved
                            )
                        )
                    }
                },
                {
                    _data.postValue(
                        WriteResult(
                            false,
                            R.string.sth_went_wrong
                        )
                    )
                }
            )
        }

        fun createAlbum() {
            val uuid = UUID.randomUUID().toString()

            if (imageUri.value != null) {
                uploadImage(uuid) { imageKey ->
                    imageUploadProgress.postValue("Upload Complete. Saving...")
                    val album = Album.Builder()
                        .key(uuid)
                        .name(name.value)
                        .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                        .thumbnailKey(
                            imageKey!!.replace(
                                "public/",
                                ""
                            )
                        )
                        .build()
                    saveAlbumToDb(album = album)
                }
            } else {
                _data.postValue(
                    WriteResult(
                        false,
                        R.string.sth_went_wrong
                    )
                )
            }

        }

        fun editAlbum() {
            if (imageUri.value != null) {
                uploadImage(albumToEdit.value!!.key, true) { imageKey ->
                    deleteImage(albumToEdit.value!!.thumbnailKey)
                    imageUploadProgress.postValue("Upload Complete. Saving changes...")
                    Amplify.DataStore.query(
                        Album::class.java,
                        Where.identifier(Album::class.java, albumToEdit.value!!.key.replace("[album]", "")),
                        {
                            if (it.hasNext()) {
                                val original = it.next()
                                val album = original.copyOfBuilder()
                                    .name(name.value)
                                    .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                                    .thumbnailKey(
                                        imageKey!!.replace(
                                            "public/",
                                            ""
                                        )
                                    )
                                    .build()
                                saveAlbumToDb(album = album)
                            }
                        },
                        {
                            Log.e("ALBUM TO EDIT ERROR", it.message.toString())
                        }
                    )
                }
            } else {
                imageUploadProgress.postValue("Saving changes...")
                Amplify.DataStore.query(
                    Album::class.java,
                    Where.identifier(Album::class.java, albumToEdit.value!!.key.replace("[album]", "")),
                    {
                        if (it.hasNext()) {
                            val original = it.next()
                            val album = original.copyOfBuilder()
                                .name(name.value)
                                .build()
                            saveAlbumToDb(album = album)
                        }
                    },
                    {
                        Log.e("ALBUM TO EDIT ERROR", it.message.toString())
                    }
                )
            }

        }

        fun deleteAlbum() {
            val delOptions = StorageRemoveOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build()
            Amplify.Storage.remove(
                albumToEdit.value?.thumbnailKey!!,
                delOptions,
                {
                    Log.e("STORAGEREMOVERESULT", it.key)
                },
                {
                    Log.e("STORAGEREMOVEEXCEPTION", it.message.toString())
                }
            )
            Amplify.DataStore.delete(
                albumToEdit.value!!,
                {
                    Log.e("ALBUM DELETE", it.item().name)
                    _data.postValue(WriteResult(true))
                },
                {
                    Log.e("ALBUM DELETE EXCEPTION", it.message.toString())
                    _data.postValue(
                        WriteResult(
                            false,
                            R.string.sth_went_wrong
                        )
                    )
                }
            )
        }

        fun clearResult(@StringRes success: Int) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)