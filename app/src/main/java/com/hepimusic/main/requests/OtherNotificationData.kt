package com.hepimusic.main.requests

data class OtherNotificationData(
    val songKey: String,
    val playlistKey: String,
    val playerKey: String,
    val owner: String
)
