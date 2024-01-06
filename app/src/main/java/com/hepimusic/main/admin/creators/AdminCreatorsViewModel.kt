package com.hepimusic.main.admin.creators

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
import com.amplifyframework.datastore.generated.model.Creator
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageRemoveOptions
import com.amplifyframework.storage.s3.options.AWSS3StorageUploadFileOptions
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
class AdminCreatorsViewModel @Inject constructor(
    val application: Application
): BaseAdminViewModel(application) {

    private val observable = AdminCreatorsObservable()

    fun getObservable(): AdminCreatorsViewModel.AdminCreatorsObservable {
        return observable
    }

    inner class AdminCreatorsObservable: BaseObservable() {
        private val _data = MutableLiveData<WriteResult>()
        internal val data: LiveData<WriteResult> get() = _data

        // image
        val _imageUri = MutableLiveData<Uri?>()
        val imageUri: LiveData<Uri?> = _imageUri
        var imageUploadProgress = MutableLiveData<String>()
        var imageUploaded = false

        val _deleteOldImage = MutableLiveData<Boolean>()
        val deleteOldImage: LiveData<Boolean> = _deleteOldImage

        val _creatorToEdit = MutableLiveData<Creator?>()
        val creatorToEdit: LiveData<Creator?> = _creatorToEdit
        val _editClicked = MutableLiveData<Boolean>()
        val editClicked : LiveData<Boolean> = _editClicked

        // creator fields
        val _key = MutableLiveData<String>()

        @Bindable
        var crname = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.crname)
            }

        @Bindable
        var ddesc = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.ddesc)
            }

        @Bindable
        var ffacebook = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.ffacebook)
            }

        @Bindable
        var iinstagram = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.iinstagram)
            }

        @Bindable
        var ttwitter = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.ttwitter)
            }

        @Bindable
        var yyoutube = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.yyoutube)
            }

        @Bindable
        val _thumbnail = MutableLiveData<String>()

        @Bindable
        val _thumbnailKey = MutableLiveData<String>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = crname
        val thumbnail: LiveData<String> = _thumbnail
        val thumbnailKey: LiveData<String> = _thumbnailKey
        val desc: LiveData<String> = ddesc
        val facebook: LiveData<String> = ffacebook
        val instagram: LiveData<String> = iinstagram
        val twitter: LiveData<String> = ttwitter
        val youtube: LiveData<String> = yyoutube

        private fun uploadImage(uuid: String, editing: Boolean = false, completionCallback: ((thumbnailKey: String?) -> Unit)? = null) {
            var key = if (!name.value.isNullOrEmpty()) {
                "images/${name.value}"+if (editing) " v"+ UUID.randomUUID().toString().substring(0, 4) else ""
            } else {
                "images/${uuid}"+if (editing) " v"+ UUID.randomUUID().toString().substring(0, 4) else ""
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
                    this@AdminCreatorsViewModel.viewModelScope.launch {
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

        private fun saveCreatorToDb(creator: Creator) {
            Amplify.DataStore.save(
                creator,
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        imageUploadProgress.postValue("")
                        imageUploaded = false
                        _data.postValue(
                            WriteResult(
                                true,
                                R.string.creator_saved
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

        fun createCreator() {
            val uuid = UUID.randomUUID().toString()

            if (imageUri.value != null) {
                uploadImage(uuid) { imageKey ->
                    imageUploadProgress.postValue("Upload Complete. Saving...")
                    val creator = Creator.Builder()
                        .key(uuid)
                        .name(name.value)
                        .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                        .thumbnailKey(
                            imageKey!!.replace(
                                "public/",
                                ""
                            )
                        )
                        .desc(desc.value)
                        .facebook(facebook.value)
                        .instagram(instagram.value)
                        .twitter(twitter.value)
                        .youtube(youtube.value)
                        .build()
                    saveCreatorToDb(creator = creator)
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

        fun editCreator() {
            if (imageUri.value != null) {
                uploadImage(creatorToEdit.value!!.key, true) { imageKey ->
                    deleteImage(creatorToEdit.value!!.thumbnailKey)
                    imageUploadProgress.postValue("Upload Complete. Saving changes...")
                    Amplify.DataStore.query(
                        Creator::class.java,
                        Where.identifier(Creator::class.java, creatorToEdit.value!!.key.replace("[artist]", "")),
                        {
                            if (it.hasNext()) {
                                val original = it.next()
                                val creator = original.copyOfBuilder()
                                    .name(name.value)
                                    .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                                    .thumbnailKey(
                                        imageKey!!.replace(
                                            "public/",
                                            ""
                                        )
                                    )
                                    .desc(desc.value)
                                    .facebook(facebook.value)
                                    .instagram(instagram.value)
                                    .twitter(twitter.value)
                                    .youtube(youtube.value)
                                    .build()
                                saveCreatorToDb(creator = creator)
                            }
                        },
                        {
                            Log.e("CREATOR TO EDIT ERROR", it.message.toString())
                        }
                    )
                }
            } else {
                imageUploadProgress.postValue("Saving changes...")
                Amplify.DataStore.query(
                    Creator::class.java,
                    Where.identifier(Creator::class.java, creatorToEdit.value!!.key.replace("[artist]", "")),
                    {
                        if (it.hasNext()) {
                            val original = it.next()
                            val creator = original.copyOfBuilder()
                                .name(name.value)
                                .desc(desc.value)
                                .facebook(facebook.value)
                                .instagram(instagram.value)
                                .twitter(twitter.value)
                                .youtube(youtube.value)
                                .build()
                            saveCreatorToDb(creator = creator)
                        }
                    },
                    {
                        Log.e("CREATOR TO EDIT ERROR", it.message.toString())
                    }
                )
            }

        }

        fun deleteCreator() {
            val delOptions = StorageRemoveOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build()
            Amplify.Storage.remove(
                creatorToEdit.value?.thumbnailKey!!,
                delOptions,
                {
                    Log.e("STORAGEREMOVERESULT", it.key)
                },
                {
                    Log.e("STORAGEREMOVEEXCEPTION", it.message.toString())
                }
            )
            Amplify.DataStore.delete(
                creatorToEdit.value!!,
                {
                    Log.e("CREATOR DELETE", it.item().name)
                    _data.postValue(WriteResult(true))
                },
                {
                    Log.e("CREATOR DELETE EXCEPTION", it.message.toString())
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