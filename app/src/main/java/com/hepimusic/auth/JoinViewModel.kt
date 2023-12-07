package com.hepimusic.auth

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class JoinViewModel @Inject constructor(val application: Application): ViewModel() {
    val _username = MutableLiveData<String>()
    val _email = MutableLiveData<String>()
    val _phone = MutableLiveData<String>()
    val _password = MutableLiveData<String>()

    val username: LiveData<String> = _username
    val emailAddr: LiveData<String> = _email
    val phone: LiveData<String> = _phone
    val password: LiveData<String> = _password

    fun setUserName(username: String) = _username.postValue(username)

    fun setEmail(email: String) = _email.postValue(email)

    fun setPhone(phone: String) = _phone.postValue(phone)

    fun setPassword(password: String) = _password.postValue(password)

    fun createProfile() {
        val profile = Profile.builder()
            .key(UUID.randomUUID().toString())
            .name(username.value)
            .email(emailAddr.value)
            .phoneNumber(phone.value)
            .build()

        Amplify.DataStore.save(
            profile,
            {
                Log.e("PROFILE SAVED TO OWNER", it.item().owner)
            },
            {
                Log.e("PROFILE SAVE EXCEPTION", it.message.toString())
                Toast.makeText(application.applicationContext, it.cause?.message.toString(), Toast.LENGTH_LONG).show()
            }
        )
    }
}