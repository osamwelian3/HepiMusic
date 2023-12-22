package com.hepimusic.main.admin.songs

import android.app.Application
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter.OnTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.generated.model.Song
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.TransferState
import com.amplifyframework.storage.operation.StorageUploadFileOperation
import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.options.StoragePagedListOptions
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
        val _imageUri = MutableLiveData<Uri?>()
        val imageUri: LiveData<Uri?> = _imageUri
        var imageUploadProgress = MutableLiveData<String>()
        var imageUploaded = false

        // file
        val _fileUri = MutableLiveData<Uri?>()
        val fileUri: LiveData<Uri?> = _fileUri
        var fileUploadProgress = MutableLiveData<String>()
        var fileUploaded = false

        val _deleteOldImage = MutableLiveData<Boolean>()
        val deleteOldImage: LiveData<Boolean> = _deleteOldImage

        val _songToEdit = MutableLiveData<Song?>()
        val songToEdit: LiveData<Song?> = _songToEdit
        val _editClicked = MutableLiveData<Boolean>()
        val editClicked : LiveData<Boolean> = _editClicked

        val categoryExists = MutableLiveData<Boolean>().apply { try { postValue(false) } catch (_: Exception) { value = false } }
        val albumExists = MutableLiveData<Boolean>().apply { try { postValue(false) } catch (_: Exception) { value = false } }
        val creatorExists = MutableLiveData<Boolean>().apply { try { postValue(false) } catch (_: Exception) { value = false } }

        // song fields
        val _key = MutableLiveData<String>()

        @Bindable
        var nname = MutableLiveData<String>()
            set(value) {
                field = value
                notifyPropertyChanged(BR.nname)
            }

        @Bindable
        val _fileUrl = MutableLiveData<String>()

        @Bindable
        val _fileKey = MutableLiveData<String>()

        @Bindable
        val _thumbnail = MutableLiveData<String>()

        @Bindable
        val _thumbnailKey = MutableLiveData<String>()

        @Bindable
        var aalbumName = MutableLiveData<String?>()
            set(value) {
                value.value?.let { searchAlbum(it) }
                field = value
                notifyPropertyChanged(BR.aalbumName)
            }

        var albumName = ""
            set(value) {
                field = value
                searchAlbum(value)
            }

        var categoryName = ""
            set(value) {
                field = value
                searchCategory(value)
            }

        var creatorName = ""
            set(value) {
                field = value
                searchCreator(value)
            }

        val _partOf = MutableLiveData<String?>()

        @Bindable
        var sselectedCategory = MutableLiveData<String?>()
            set(value) {
                value.value?.let { searchCategory(it) }
                field = value
                notifyPropertyChanged(BR.sselectedCategory)
            }

        @Bindable
        var _selectedCreator = MutableLiveData<String?>()

        @Bindable
        val _listens = MutableLiveData<List<String>?>()

        @Bindable
        val _trendingListens = MutableLiveData<List<String>?>()

        @Bindable
        val _listOfUidUpVotes = MutableLiveData<List<String>?>()

        @Bindable
        val _listOfUidDownVotes = MutableLiveData<List<String>?>()

        val key: LiveData<String> = _key
        val name: LiveData<String> = nname
        val fileUrl: LiveData<String> = _fileUrl
        val fileKey: LiveData<String> = _fileKey
        val thumbnail: LiveData<String> = _thumbnail
        val thumbnailKey: LiveData<String> = _thumbnailKey
        val partOf: LiveData<String?> = _partOf
        val selectedCategory: LiveData<String?> = sselectedCategory
        val selectedCreator: LiveData<String?> = _selectedCreator
        val listens: LiveData<List<String>?> = _listens
        val trendingListens: LiveData<List<String>?> = _trendingListens
        val listOfUidUpVotes: LiveData<List<String>?> = _listOfUidUpVotes
        val listOfUidDownVotes: LiveData<List<String>?> = _listOfUidDownVotes

        private fun searchCategory(name: String) {
            val category = categories.value?.find { it.originalCategory.name.trim() == name.trim() }
            if (category != null) {
                sselectedCategory.postValue(category.originalCategory.name)
                categoryExists.postValue(true)
            } else {
                sselectedCategory.postValue(null)
                categoryExists.postValue(false)
            }
        }

        private fun searchAlbum(name: String) {
            val album = albums.value?.find { it.originalAlbum.name.trim() == name.trim() }
            if (album != null) {
                _partOf.postValue(album.originalAlbum.key)
                albumExists.postValue(true)
            } else {
                _partOf.postValue(null)
                albumExists.postValue(false)
            }
        }

        private fun searchCreator(name: String) {
            val creator = creators.value?.find { it.originalCreator.name.trim() == name.trim() }
            if (creator != null) {
                _selectedCreator.postValue(Gson().toJson(creator.originalCreator))
                creatorExists.postValue(true)
            } else {
                _selectedCreator.postValue(null)
                creatorExists.postValue(false)
            }
        }

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
                        _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                    }
                }
            )
        }

        private fun uploadSongFile(uuid: String, editing: Boolean = false, completionCallback: ((fileKey: String?) -> Unit)? = null) {
            val key = if (!name.value.isNullOrEmpty()) {
                "files/${name.value}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            } else {
                "files/${uuid}"+if (editing) UUID.randomUUID().toString().substring(0, 4) else ""
            }

            val options = AWSS3StorageUploadFileOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
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

            Amplify.Storage.uploadFile(
                "$key.$ext",
                file,
                options,
                {
                    imageUploadProgress.postValue("")
                    val p = (it.currentBytes / it.totalBytes) * 100
                    fileUploadProgress.postValue("$p%")
                    if (it.currentBytes == it.totalBytes) {
                        fileUploadProgress.postValue("File Upload Complete")
                    }
                    Log.e("FILE PROGRESS", "Fraction completed: "+it.fractionCompleted.toString()+" currentBytes: "+it.currentBytes.toString()+" totalBytes: "+it.totalBytes.toString())
                },
                {
                    fileStream.close()
                    fileUploaded = true
                    completionCallback?.invoke(it.key)
                },
                {
                    fileStream.close()
                    it.printStackTrace()
                    Log.e("UPLOAD FILE ERROR", it.message.toString())
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            )
        }

        fun deleteImage(key: String) {
            Amplify.Storage.remove(
                key,
                StorageRemoveOptions.builder()
                    .accessLevel(StorageAccessLevel.PUBLIC)
                    .build(),
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
                StorageRemoveOptions.builder()
                    .accessLevel(StorageAccessLevel.PUBLIC)
                    .build(),
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
                    CoroutineScope(Dispatchers.Main).launch {
                        fileUploadProgress.postValue("")
                        imageUploadProgress.postValue("")
                        imageUploaded = false
                        fileUploaded = false
                        _data.postValue(WriteResult(true, R.string.song_saved))
                    }
                },
                {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            )
        }

        fun createSong() {
            val uuid = UUID.randomUUID().toString()

            if (imageUri.value != null) {
                /*val imageUpload = uploadImage(uuid)
                imageUpload.start()
                imageUpload.setOnProgress {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                    if (it.currentBytes == it.totalBytes) {
                        imageUploadProgress.postValue("Image Upload Complete")
                    }
                    Log.e("PROGRESS", "Fraction completed: "+it.fractionCompleted.toString()+" currentBytes: "+it.currentBytes.toString()+" totalBytes: "+it.totalBytes.toString())
                }

                imageUpload.setOnError {
                    imageUpload.request
                    it.printStackTrace()
                    Log.e("UPLOAD IMAGE ERROR", it.message.toString())
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }*/

                uploadImage(uuid) { imageKey ->
                    uploadSongFile(uuid) { fileKey ->
                        fileUploadProgress.postValue("Upload Complete. Saving...")
                        val song = Song.Builder()
                            .key(uuid)
                            .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${fileKey}")
                            .fileKey(fileKey!!.replace("public/", ""))
                            .name(name.value)
                            .selectedCategory(selectedCategory.value)
                            .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                            .thumbnailKey(
                                imageKey!!.replace(
                                    "public/",
                                    ""
                                )
                            )
                            .selectedCreator(selectedCreator.value)
                            .partOf(partOf.value)
                            .listens(emptyList())
                            .trendingListens(emptyList())
                            .listOfUidUpVotes(emptyList())
                            .listOfUidDownVotes(emptyList())
                            .build()
                        saveSongToDb(song = song)
                    }
                }

                /*imageUpload.setOnSuccess { storageUploadFileResult ->
                    _thumbnailKey.postValue(storageUploadFileResult.key.replace("public/", ""))
                    _thumbnail.postValue("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                    if (*//*imageUpload.transferState == TransferState.COMPLETED || *//*imageUploadProgress.value!!.contains("Image Upload Complete")) {
                        imageUploadProgress.postValue("")
                        val fileUpload = uploadSongFile(uuid)
                        fileUpload.start()
                        fileUpload.setOnProgress {
                            val p = (it.currentBytes / it.totalBytes) * 100
                            fileUploadProgress.postValue("$p%")
                            if (it.currentBytes == it.totalBytes) {
                                fileUploadProgress.postValue("File Upload Complete")
                            }
                            Log.e("PROGRESS", "Fraction completed: "+it.fractionCompleted.toString()+" currentBytes: "+it.currentBytes.toString()+" totalBytes: "+it.totalBytes.toString())
                        }

                        fileUpload.setOnError {
                            _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                        }

                        fileUpload.setOnSuccess {
                            _fileKey.postValue(it.key.replace("public/", ""))
                            _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                            if (*//*fileUpload.transferState == TransferState.COMPLETED || *//*fileUploadProgress.value!!.contains("File Upload Complete")) {
                                fileUploadProgress.postValue("Upload Complete. Saving...")
                                val song = Song.Builder()
                                    .key(uuid)
                                    .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                                    .fileKey(it.key.replace("public/", ""))
                                    .name(name.value)
                                    .selectedCategory(selectedCategory.value)
                                    .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                                    .thumbnailKey(
                                        storageUploadFileResult.key.replace(
                                            "public/",
                                            ""
                                        )
                                    )
                                    .selectedCreator(selectedCreator.value)
                                    .partOf(partOf.value)
                                    .listens(emptyList())
                                    .trendingListens(emptyList())
                                    .listOfUidUpVotes(emptyList())
                                    .listOfUidDownVotes(emptyList())
                                    .build()
                                saveSongToDb(song = song)
                            }
                        }
                    }
                }*/
            } else {
                _data.postValue(WriteResult(false, R.string.sth_went_wrong))
            }

        }

        fun editSong() {
            if (imageUri.value != null) {

                uploadImage(songToEdit.value!!.key, true) { imageKey ->
                    deleteImage(songToEdit.value!!.thumbnailKey)
                    if (fileUri.value != null) {
                        uploadSongFile(songToEdit.value!!.key, true) { fileKey ->
                            deleteSongFile(songToEdit.value!!.fileKey)
                            fileUploadProgress.postValue("Upload Complete. Saving changes...")
                            Amplify.DataStore.query(
                                Song::class.java,
                                Where.identifier(Song::class.java, songToEdit.value!!.key.replace("[item]", "")),
                                {
                                    if (it.hasNext()) {
                                        val original = it.next()
                                        val song = original.copyOfBuilder()
                                            .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${fileKey}")
                                            .fileKey(fileKey!!.replace("public/", ""))
                                            .name(name.value)
                                            .selectedCategory(selectedCategory.value)
                                            .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                                            .thumbnailKey(
                                                imageKey!!.replace(
                                                    "public/",
                                                    ""
                                                )
                                            )
                                            .selectedCreator(selectedCreator.value)
                                            .partOf(partOf.value)
                                            .build()
                                        saveSongToDb(song = song)
                                    }
                                },
                                {
                                    Log.e("SONG TO EDIT ERROR", it.message.toString())
                                }
                            )
                        }
                    } else {
                        fileUploadProgress.postValue("Upload Complete. Saving changes...")
                        Amplify.DataStore.query(
                            Song::class.java,
                            Where.identifier(Song::class.java, songToEdit.value!!.key.replace("[item]", "")),
                            {
                                if (it.hasNext()) {
                                    val original = it.next()
                                    val song = original.copyOfBuilder()
                                        .name(name.value)
                                        .selectedCategory(selectedCategory.value)
                                        .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${imageKey}")
                                        .thumbnailKey(imageKey!!.replace("public/", ""))
                                        .selectedCreator(selectedCreator.value)
                                        .partOf(partOf.value)
                                        .build()
                                    saveSongToDb(song = song)
                                }
                            },
                            {
                                Log.e("SONG TO EDIT ERROR", it.message.toString())
                            }
                        )
                    }
                }

                /*val imageUpload = uploadImage(songToEdit.value!!.key, true)
                imageUpload.start()
                imageUpload.setOnProgress {
                    val p = (it.currentBytes / it.totalBytes) * 100
                    imageUploadProgress.postValue("$p%")
                    if (it.currentBytes == it.totalBytes) {
                        imageUploadProgress.postValue("Image Upload Complete")
                    }
                }

                imageUpload.setOnError {
                    it.printStackTrace()
                    Log.e("EDIT IMAGE ERROR", it.message.toString())
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }

                imageUpload.setOnSuccess { storageUploadFileResult ->
                    deleteImage(songToEdit.value!!.thumbnailKey)
                    _thumbnailKey.postValue(storageUploadFileResult.key.replace("public/", ""))
                    _thumbnail.postValue("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                    if (*//*imageUpload.transferState == TransferState.COMPLETED*//*imageUploadProgress.value!!.contains("Image Upload Complete")) {
                        imageUploadProgress.postValue("")
                        if (fileUri.value != null) {
                            val fileUpload = uploadSongFile(songToEdit.value!!.key, true)
                            fileUpload.start()
                            fileUpload.setOnProgress {
                                val p = (it.currentBytes / it.totalBytes) * 100
                                fileUploadProgress.postValue("$p%")
                                if (it.currentBytes == it.totalBytes) {
                                    fileUploadProgress.postValue("File Upload Complete")
                                }
                            }

                            fileUpload.setOnError {
                                _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                            }

                            fileUpload.setOnSuccess {
                                deleteSongFile(songToEdit.value!!.fileKey)
                                _fileKey.postValue(it.key.replace("public/", ""))
                                _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                                if (*//*fileUpload.transferState == TransferState.COMPLETED*//*fileUploadProgress.value!!.contains("File Upload Complete")) {
                                    fileUploadProgress.postValue("Upload Complete. Saving changes...")
                                    val song = songToEdit.value!!.copyOfBuilder()
                                        .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                                        .fileKey(it.key.replace("public/", ""))
                                        .name(name.value)
                                        .selectedCategory(selectedCategory.value)
                                        .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                                        .thumbnailKey(
                                            storageUploadFileResult.key.replace(
                                                "public/",
                                                ""
                                            )
                                        )
                                        .selectedCreator(selectedCreator.value)
                                        .partOf(partOf.value)
                                        .build()
                                    saveSongToDb(song = song)
                                }
                            }
                        } else {
                            fileUploadProgress.postValue("Upload Complete. Saving changes...")
                            val song = songToEdit.value!!.copyOfBuilder()
                                .name(name.value)
                                .selectedCategory(selectedCategory.value)
                                .thumbnail("https://dn1i8z7909ivj.cloudfront.net/${storageUploadFileResult.key}")
                                .thumbnailKey(storageUploadFileResult.key.replace("public/", ""))
                                .selectedCreator(selectedCreator.value)
                                .partOf(partOf.value)
                                .build()
                            saveSongToDb(song = song)
                        }
                    }
                }*/
            } else {
                if (fileUri.value != null) {

                    uploadSongFile(songToEdit.value!!.key, true) { fileKey ->
                        deleteSongFile(songToEdit.value!!.fileKey)
                        fileUploadProgress.postValue("Upload Complete. Saving changes...")
                        Amplify.DataStore.query(
                            Song::class.java,
                            Where.identifier(Song::class.java, songToEdit.value!!.key.replace("[item]", "")),
                            {
                                if (it.hasNext()) {
                                    val original = it.next()
                                    val song = original.copyOfBuilder()
                                        .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${fileKey}")
                                        .fileKey(fileKey!!.replace("public/", ""))
                                        .name(name.value)
                                        .selectedCategory(selectedCategory.value)
                                        .selectedCreator(selectedCreator.value)
                                        .partOf(partOf.value)
                                        .build()
                                    saveSongToDb(song = song)
                                }
                            },
                            {
                                Log.e("SONG TO EDIT ERROR", it.message.toString())
                            }
                        )
                    }

                    /*val fileUpload = uploadSongFile(songToEdit.value!!.key, true)
                    fileUpload.start()
                    fileUpload.setOnProgress {
                        val p = (it.currentBytes / it.totalBytes) * 100
                        fileUploadProgress.postValue("$p%")
                        if (it.currentBytes == it.totalBytes) {
                            fileUploadProgress.postValue("File Upload Complete")
                        }
                    }

                    fileUpload.setOnError {
                        _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                    }

                    fileUpload.setOnSuccess {
                        deleteSongFile(songToEdit.value!!.fileKey)
                        _fileKey.postValue(it.key.replace("public/", ""))
                        _fileUrl.postValue("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                        if (*//*fileUpload.transferState == TransferState.COMPLETED*//*fileUploadProgress.value!!.contains("File Upload Complete")) {
                            fileUploadProgress.postValue("Upload Complete. Saving changes...")
                            val song = songToEdit.value!!.copyOfBuilder()
                                .fileUrl("https://dn1i8z7909ivj.cloudfront.net/${it.key}")
                                .fileKey(it.key.replace("public/", ""))
                                .name(name.value)
                                .selectedCategory(selectedCategory.value)
                                .selectedCreator(selectedCreator.value)
                                .partOf(partOf.value)
                                .build()
                            saveSongToDb(song = song)
                        }
                    }*/
                } else {
                    imageUploadProgress.postValue("")
                    fileUploadProgress.postValue("Saving changes...")
                    Amplify.DataStore.query(
                        Song::class.java,
                        Where.identifier(Song::class.java, songToEdit.value!!.key.replace("[item]", "")),
                        {
                            if (it.hasNext()) {
                                val original = it.next()
                                val song = original.copyOfBuilder()
                                    .name(name.value)
                                    .selectedCategory(selectedCategory.value)
                                    .selectedCreator(selectedCreator.value)
                                    .partOf(partOf.value)
                                    .build()
                                saveSongToDb(song = song)
                            }
                        },
                        {
                            Log.e("SONG TO EDIT ERROR", it.message.toString())
                        }
                    )
                }
            }

        }

        fun deleteSong() {
            val delOptions = StorageRemoveOptions.builder()
                .accessLevel(StorageAccessLevel.PUBLIC)
                .build()
            Amplify.Storage.remove(
                songToEdit.value?.thumbnailKey!!,
                delOptions,
                {
                    Log.e("STORAGEREMOVERESULT", it.key)
                },
                {
                    Log.e("STORAGEREMOVEEXCEPTION", it.message.toString())
                }
            )
            Amplify.Storage.remove(
                songToEdit.value?.fileKey!!,
                delOptions,
                {
                    Log.e("STORAGEREMOVERESULT", it.key)
                },
                {
                    Log.e("STORAGEREMOVEEXCEPTION", it.message.toString())
                }
            )
            Amplify.DataStore.delete(
                songToEdit.value!!,
                {
                    Log.e("SONG DELETE", it.item().name)
                    _data.postValue(WriteResult(true))
                },
                {
                    Log.e("SONG DELETE EXCEPTION", it.message.toString())
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            )
        }

        fun clearResult(@StringRes success: Int) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)