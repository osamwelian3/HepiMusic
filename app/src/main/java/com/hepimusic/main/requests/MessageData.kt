package com.hepimusic.main.requests

data class MessageData(
    val topic: String,
    val notification: NotificationData,
    val data: OtherNotificationData
)