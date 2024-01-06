package com.hepimusic.main.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Profile
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.s3.options.AWSS3StorageUploadFileOptions
import com.hepimusic.R
import com.hepimusic.main.common.utils.ImageUtils
import com.hepimusic.main.playlist.WriteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WriteProfileViewModel @Inject constructor(val application: Application): ViewModel() {

    var originalProfile = MutableLiveData<Profile>()
    var uploadProgress = MutableLiveData<String>()
    private val _data = MutableLiveData<WriteResult>()
    internal val data: LiveData<WriteResult> get() = _data

    fun createProfile(
        profileName: String,
        profileEmail: String,
        profilePhone: String,
        tempThumbUri: Uri?,
        userId: String
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    val key = if (profileName.isNotEmpty()) {
                        "profilePictureImage/${profileName}"
                    } else {
                        "profilePictureImage/$userId"
                    }

                    val uploadOptions = StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PUBLIC)
                        .build()

                    val options = AWSS3StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PUBLIC)
                        .setUseAccelerateEndpoint(true)
                        .build()

                    tempThumbUri?.let {
                        val contentResolver = application.contentResolver
                        val mimeTypeMap = MimeTypeMap.getSingleton()
                        val ext = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(tempThumbUri))
                        val imageStream: InputStream? = application.contentResolver.openInputStream(
                            tempThumbUri
                        )
                        val tempFile = File.createTempFile("image", ".image")
                        ImageUtils.copyStreamToFile(imageStream!!, tempFile)

                        val file = File(tempFile.absolutePath)
                        val upload = Amplify.Storage.uploadFile(
                            "$key.$ext",
                            file,
                            options,
                            {
                                /*Amplify.DataStore.save(
                                    profile,
                                    {
                                        _data.postValue(WriteResult(true, R.string.profile_saved))
                                    },
                                    {
                                        _data.postValue(WriteResult(false, R.string.profile_save_error))
                                    }
                                )*/
                            },
                            {
                                _data.postValue(WriteResult(false, R.string.something_went_wrong))
                            }
                        )

                        upload.setOnSuccess {
                            val profile = Profile.builder()
                                .key(userId)
                                .name(profileName)
                                .email(profileEmail)
                                .phoneNumber(profilePhone)
                                .imageKey(it.key)
                                .build()
                            Amplify.DataStore.save(
                                profile,
                                { prfle ->
                                    originalProfile.postValue(prfle.item())
                                    _data.postValue(WriteResult(true, R.string.profile_saved))
                                },
                                {
                                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                                }
                            )
                        }
                    }

                    /*val bitmap = Glide.with(application)
                        .asBitmap()
                        .load(tempThumbUri)
                        .submit().get()
                    write(playlist, bitmap)
//                    writeImageFile(playlist, tempThumbUri)
                    _data.postValue(WriteResult(true))*/
                } catch (e: Exception) {
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                }
            }
        }

    }

    fun editProfile(profileName: String, profileEmail: String, profilePhone: String, profile: Profile, tempThumbUri: Uri?, deleteImageFile: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (deleteImageFile) {
                    val delImage = Amplify.Storage.remove(
                        profile.imageKey,
                        {
                            Log.e("REMOVE IMAGE", "Succesfully deleted image for ${profile.email} with key ${profile.imageKey}")
                        },
                        {
                            Log.e("REMOVE IMAGE EXCEPTION", it.cause?.message.toString())
                        }
                    )
                    delImage.start()
                }

                try {
                    val key = if (profileName.isNotEmpty()) {
                        "profilePictureImage/${profileName}_${UUID.randomUUID().toString().substring(0, 4)}"
                    } else {
                        "profilePictureImage/${profile.key}_${UUID.randomUUID().toString().substring(0, 4)}"
                    }

                    val options = AWSS3StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PUBLIC)
                        .setUseAccelerateEndpoint(true)
                        .build()

                    if (tempThumbUri != null) {
                        val contentResolver = application.contentResolver
                        val mimeTypeMap = MimeTypeMap.getSingleton()
                        val ext = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(tempThumbUri))
                        val imageStream: InputStream? = application.contentResolver.openInputStream(
                            tempThumbUri
                        )
                        val tempFile = File.createTempFile("image", ".image")
                        ImageUtils.copyStreamToFile(imageStream!!, tempFile)

                        val file = File(tempFile.absolutePath)

                        Log.e("FILE", file.name)
//                        Log.e("URI", tempThumbUri.toString())
                        Log.e("PATH", tempThumbUri.path.toString())
                        val upload = Amplify.Storage.uploadFile(
                            "$key.$ext",
                            file,
                            options,
                            {
                                /*Amplify.DataStore.save(
                                profile,
                                {
                                    _data.postValue(WriteResult(true, R.string.profile_saved))
                                },
                                {
                                    _data.postValue(WriteResult(false, R.string.profile_save_error))
                                }
                            )*/
                            },
                            {
                                _data.postValue(
                                    WriteResult(
                                        false,
                                        R.string.something_went_wrong
                                    )
                                )
                            }
                        )

                        upload.setOnProgress {
                            val p = (it.currentBytes / it.totalBytes) * 100
                            uploadProgress.postValue("$p%")
                        }

                        upload.setOnSuccess {
                            val editedProfile = profile.copyOfBuilder()
                                .name(profileName)
                                .email(profileEmail)
                                .phoneNumber(profilePhone)
                                .imageKey(it.key)
                                .build()
                            Amplify.DataStore.save(
                                editedProfile,
                                { prfle ->
                                    originalProfile.postValue(prfle.item())
                                    _data.postValue(WriteResult(true, R.string.profile_saved))
                                },
                                {
                                    _data.postValue(
                                        WriteResult(
                                            false,
                                            R.string.something_went_wrong
                                        )
                                    )
                                }
                            )
                        }
                    } else {
                        val editedProfile = profile.copyOfBuilder()
                            .name(profileName)
                            .email(profileEmail)
                            .phoneNumber(profilePhone)
                            .build()
                        Amplify.DataStore.save(
                            editedProfile,
                            {
                                _data.postValue(WriteResult(true, R.string.profile_saved))
                            },
                            {
                                _data.postValue(
                                    WriteResult(
                                        false,
                                        R.string.something_went_wrong
                                    )
                                )
                            }
                        )
                    }
                } catch (e: Exception) {
                    _data.postValue(WriteResult(false, R.string.something_went_wrong))
                }
            }
        }

    }

    fun clearResult(@StringRes success: Int) {
        _data.postValue(WriteResult(false, success))
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)