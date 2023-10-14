package com.hepimusic.onBoarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Board(@DrawableRes val image: Int, @StringRes val title: Int, @StringRes val description: Int)