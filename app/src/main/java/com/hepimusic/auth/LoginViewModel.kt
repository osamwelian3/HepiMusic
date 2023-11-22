package com.hepimusic.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(val application: Application): ViewModel() {
    val _username = MutableLiveData<String>()
    val _password = MutableLiveData<String>()

    val username: LiveData<String> = _username
    val password: LiveData<String> = _password

    fun setUserName(username: String) = _username.postValue(username)

    fun setPassword(password: String) = _password.postValue(password)
}