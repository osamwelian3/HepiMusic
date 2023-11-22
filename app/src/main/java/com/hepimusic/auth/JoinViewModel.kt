package com.hepimusic.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
}