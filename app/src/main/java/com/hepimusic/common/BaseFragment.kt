package com.hepimusic.common

import androidx.fragment.app.Fragment
import com.hepimusic.utils.Utils

open class BaseFragment: Fragment() {
    fun isPermissionGranted(permission: String): Boolean = Utils.isPermissionGranted(permission, requireContext())
}