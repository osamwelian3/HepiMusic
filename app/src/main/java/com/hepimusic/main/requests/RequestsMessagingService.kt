package com.hepimusic.main.requests

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestSong
import com.amplifyframework.datastore.generated.model.Song
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hepimusic.R
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.ui.MainActivity
import org.json.JSONObject
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

private const val CHANNEL_ID = "Hepi_Notification_Channel"

class RequestsMessagingService: FirebaseMessagingService() {
    override fun getStartCommandIntent(originalIntent: Intent?): Intent {
        return super.getStartCommandIntent(originalIntent)
    }

    override fun handleIntent(intent: Intent?) {
        super.handleIntent(intent)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        var songKey = ""
        var playlistKey = ""
        var owner = ""

        val messageData = (message.data as Map<*, *>?)?.let { JSONObject(it) }

        Log.e("MESSAGE DATA", messageData.toString())
        messageData?.get("playlistKey")?.let { Log.e("MESSAGE DATA", it.toString()) }

        try {
            Log.e("MESSAGE DATA", messageData?.get("songKey").toString())
            songKey = messageData?.get("songKey") as String
            Log.e("MESSAGE DATA", messageData?.get("playlistKey").toString())
            playlistKey = messageData?.get("playlistKey") as String
            Log.e("MESSAGE DATA", messageData?.get("owner").toString())
            owner = messageData?.get("owner") as String
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (playlistKey.isNotEmpty() && songKey.isNotEmpty() && owner.isNotEmpty()) {
            Amplify.API.query(
                ModelQuery.get(RequestPlaylist::class.java, playlistKey),
                { playlistResponseGraphql ->
                    if (playlistResponseGraphql.hasData()) {
                        val playlist = playlistResponseGraphql.data
                        Amplify.API.query(
                            ModelQuery.get(Song::class.java, songKey),
                            { songResponseGraphql ->
                                if (songResponseGraphql.hasData()) {
                                    val song = songResponseGraphql.data
                                    val requestSong = RequestSong.builder()
                                        .key(songKey)
                                        .fileUrl(song.fileUrl)
                                        .fileKey(song.fileKey)
                                        .name(song.name)
                                        .selectedCategory(song.selectedCategory)
                                        .thumbnail(song.thumbnail)
                                        .thumbnailKey(song.thumbnailKey)
                                        .playlist(playlist)
                                        .createdDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                                        .updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                                        .ownerData(playlist.ownerData)
                                        .requestUpVotes(emptyList())
                                        .requests(listOf(owner))
                                        .partOf(song.partOf)
                                        .selectedCreator(song.selectedCreator)
                                        .listOfUidDownVotes(song.listOfUidDownVotes)
                                        .listOfUidUpVotes(song.listOfUidUpVotes)
                                        .listens(song.listens)
                                        .trendingListens(song.trendingListens)
                                        .build()

                                    Amplify.DataStore.save(
                                        requestSong,
                                        {
                                            MediaItemTree.liveDataRequestSong.postValue(it.item())
                                        },
                                        {
                                            it.printStackTrace()
                                        }
                                    )
                                }
                            },
                            {
                                it.printStackTrace()
                            }
                        )
                    }
                },
                {
                    it.printStackTrace()
                }
            )
        }

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        createNotificationChannel(notificationManager)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.thumb_circular_default)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelname = "HepiChannelName"
        val channel = NotificationChannel(CHANNEL_ID, channelname, IMPORTANCE_HIGH).apply {
            description = "Hepi Music Channel Description"
            enableLights(true)
            lightColor = Color.YELLOW
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
        Log.e("FCM SEND", "MSG_ID: $msgId")
    }

    override fun onSendError(msgId: String, exception: Exception) {
        super.onSendError(msgId, exception)
        Log.e("FCM SEND ERROR", "MessageID: "+msgId+" Exception: "+exception.message.toString())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("FCM NEW TOKEN", token)
    }
}
// project id
// storage-urli

// access key
// cd2795e29b0817ca176fdf629fe7b0dd758c95ea