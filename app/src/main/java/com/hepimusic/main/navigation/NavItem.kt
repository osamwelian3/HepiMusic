package com.hepimusic.main.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class NavItem(val id: String, @StringRes val title: Int, @DrawableRes val icon: Int, val isFromThis: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavItem

        if (id != other.id) return false
        if (title != other.title) return false
        if (icon != other.icon) return false
        if (isFromThis != other.isFromThis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title
        result = 31 * result + icon
        result = 31 * result + isFromThis.hashCode()
        return result
    }
}