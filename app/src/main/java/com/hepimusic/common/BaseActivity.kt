package com.hepimusic.common

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import com.hepimusic.utils.Utils

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    fun isPermissionGranted(permission: String): Boolean = Utils.isPermissionGranted(permission, this)
}