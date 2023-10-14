package com.hepimusic.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

object Utils {

    fun isPermissionGranted(permission: String, c: Context?): Boolean {
        val context = WeakReference(c).get()
        return context?.let { ContextCompat.checkSelfPermission(it, permission) } == PackageManager.PERMISSION_GRANTED
    }
}