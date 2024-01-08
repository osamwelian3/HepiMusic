package com.hepimusic.main.requests

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.location.Location
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat.MessagingStyle.Message
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.amplifyframework.api.graphql.PaginatedResult
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestSong
import com.amplifyframework.datastore.generated.model.Song
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.common.Constants.AUTH_USER
import com.hepimusic.common.Constants.SERVER_KEY
import com.hepimusic.common.networkobserver.ConnectivityObserver
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.main.requests.common.BaseRequestsViewModel
import com.hepimusic.main.requests.common.toRequestPlayer
import com.hepimusic.main.requests.common.toRequestSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    val application: Application
): BaseRequestsViewModel(application) {

    val sharedPreferences = application.getSharedPreferences("main", MODE_PRIVATE)

    val connectivityStatus = MutableLiveData<ConnectivityObserver.Status>()

    val currentAuthUserString = sharedPreferences.getString(AUTH_USER, null)

    val foundEndpointIds = MutableLiveData<List<EndPointData>>()

    val wifiP2PDeviceList = MutableLiveData<List<WifiP2pDevice>>()

    val wifiP2pInfo = MutableLiveData<WifiP2pInfo>()
    val wifiP2pDevice = MutableLiveData<WifiP2pDevice>()

    val wifiP2PEnabled = MutableLiveData<Boolean>()

    private val STRATEGY = Strategy.P2P_CLUSTER

    val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(application)

    var receivedNotification: OtherNotificationData? = null

    fun sendRequestSong(notification: OtherNotificationData, playerId: String) {
        connectionsClient.sendPayload(
            playerId,
            Payload.fromBytes(Gson().toJson(notification).toByteArray(Charsets.UTF_8))
        )
    }

    val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endPointId: String, payload: Payload) {
            payload.asBytes()?.let { bytes ->
                receivedNotification = Gson().fromJson(bytes.decodeToString(), OtherNotificationData::class.java)
                receivedNotification?.let { addToPlaylist(it) }
            }
        }

        override fun onPayloadTransferUpdate(endPointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS && receivedNotification != null) {

            }
        }
    }

    fun addToPlaylist(receivedNotification: OtherNotificationData) {
        var songKey = ""
        var playlistKey = ""
        var owner = ""

        Log.e("MESSAGE DATA", receivedNotification.toString())

        try {
            Log.e("MESSAGE DATA", receivedNotification.songKey.toString())
            songKey = receivedNotification.songKey
            Log.e("MESSAGE DATA", receivedNotification.playlistKey)
            playlistKey = receivedNotification.playlistKey
            Log.e("MESSAGE DATA", receivedNotification.owner)
            owner = receivedNotification.owner
        } catch (e: java.lang.Exception) {
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
                                        .ownerData(currentAuthUserString)
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
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endPointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endPointId, payloadCallback)
            Toast.makeText(application.applicationContext, "${info.endpointName} joined the session", Toast.LENGTH_LONG).show()
        }

        override fun onConnectionResult(endPointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {

            }
        }

        override fun onDisconnected(endPointId: String) {

        }
    }

    fun startAdvertising(myDeviceName: String) {
        val options = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        connectionsClient.startAdvertising(
            myDeviceName,
            application.packageName,
            connectionLifecycleCallback,
            options
        )
    }

    val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endPointId: String, info: DiscoveredEndpointInfo) {
            val tempList = mutableListOf<EndPointData>()
            if (foundEndpointIds.value != null) {
                tempList.addAll(foundEndpointIds.value!!)
            }
            val player = players.value?.find { it.name == info.endpointName.replace(" [WIFI]", "") }
            tempList.add(EndPointData(endPointId, info, player))
            foundEndpointIds.postValue(tempList)
        }

        override fun onEndpointLost(endpointId: String) {
            val tempList = mutableListOf<EndPointData>()
            if (foundEndpointIds.value != null) {
                tempList.addAll(foundEndpointIds.value!!)
                tempList.removeIf { it.endpointId == endpointId }
                foundEndpointIds.postValue(tempList)
            }
        }
    }

    fun requestConnection(myCodeName: String, endPointId: String, connectionLifecycleCallback: ConnectionLifecycleCallback) {
        connectionsClient.requestConnection(myCodeName, endPointId, connectionLifecycleCallback)
    }

    fun startDiscovery() {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(application.packageName, endpointDiscoveryCallback, options)
    }

    fun stopDiscovery() {
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
            foundEndpointIds.postValue(listOf())
        }
    }

    private val observable = RequestsObservable()

    fun getObservable(): RequestsObservable {
        return observable
    }

    inner class RequestsObservable: BaseObservable() {

        val _addSongsToPlaylistClicked = MutableLiveData<Boolean>()
        val addSongsToPlaylistClicked: LiveData<Boolean> = _addSongsToPlaylistClicked

        private val _data = MutableLiveData<WriteResult>()
        internal val data: LiveData<WriteResult> get() = _data

        val _playerToEdit = MutableLiveData<RequestPlayer?>()
        val playerToEdit: LiveData<RequestPlayer?> = _playerToEdit

        val _playlistToEdit = MutableLiveData<RequestPlaylist?>()
        val playlistToEdit: LiveData<RequestPlaylist?> = _playlistToEdit

        val _songToAdd = MutableLiveData<Song?>()
        val songToAdd: LiveData<Song?> = _songToAdd

        val _location = MutableLiveData<Location?>()
        val location: LiveData<Location?> = _location

        //Player
        @Bindable
        val pplayerName = MutableLiveData<String?>()
        @Bindable
        val pplayerDesc = MutableLiveData<String?>()
        @Bindable
        val pplayerLong = MutableLiveData<String?>()
        @Bindable
        val pplayerLat = MutableLiveData<String?>()

        val playerName: LiveData<String?> = pplayerName
        val playerDesc: LiveData<String?> = pplayerDesc
        val playerLong: LiveData<String?> = pplayerLong
        val playerLat: LiveData<String?> = pplayerLat

        // Playlist
        @Bindable
        val pplaylistName = MutableLiveData<String?>()
        @Bindable
        val pplaylistDesc = MutableLiveData<String?>()
        @Bindable
        val pplaylistPlayer = MutableLiveData<RequestPlayer?>()

        val playlistName: LiveData<String?> = pplaylistName
        val playlistDesc: LiveData<String?> = pplaylistDesc
        val playlistPlayer: LiveData<RequestPlayer?> = pplaylistPlayer

        fun savePlayerToDb(player: RequestPlayer) {
            if (connectivityStatus.value != ConnectivityObserver.Status.Available) {
                viewModelScope.launch {
                    Toast.makeText(application.applicationContext, "You need an internet connection to perform this operation", Toast.LENGTH_LONG).show()
                    _data.postValue(WriteResult(false, R.string.no_internet))
                }
                return
            }
            Amplify.DataStore.save(
                player,
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        _data.postValue(
                            WriteResult(
                                true,
                                R.string.request_player_added
                            )
                        )
                    }
                },
                {
                    it.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        _data.postValue(
                            WriteResult(
                                false,
                                R.string.sth_went_wrong
                            )
                        )
                    }
                }
            )
        }

        fun createNewPlayer() {
            val uuid = UUID.randomUUID().toString()
            var playerBuilder: RequestPlayer.Builder? = null
            if (!playerName.value.isNullOrEmpty() && !playerLat.value.isNullOrEmpty() && !playerLong.value.isNullOrEmpty()) {
                playerBuilder = RequestPlayer.Builder()
                playerBuilder.key(uuid)
                playerBuilder.name(playerName.value)
                playerDesc.value?.let {
                    playerBuilder.desc(it)
                }
                playerBuilder.latitude(playerLat.value)
                playerBuilder.longitude(playerLong.value)
                playerBuilder.followers(emptyList())
                playerBuilder.following(emptyList())
                playerBuilder.createdDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                playerBuilder.updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                playerBuilder.ownerData(currentAuthUserString)

                savePlayerToDb(playerBuilder.build())
            }
        }

        fun editPlayer() {
            playerToEdit.value?.let {
                val playerBuilder: RequestPlayer.Builder = it.copyOfBuilder()

                playerName.value?.let {
                    playerBuilder.name(playerName.value)
                }

                playerDesc.value?.let {
                    playerBuilder.desc(it)
                }

                playerLat.value?.let {
                    playerBuilder.latitude(playerLat.value)
                }

                playerLong.value?.let {
                    playerBuilder.longitude(playerLong.value)
                }

                playerBuilder.updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))

                savePlayerToDb(playerBuilder.build())
            }
        }

        fun savePlaylistToDb(playist: RequestPlaylist) {
            if (connectivityStatus.value != ConnectivityObserver.Status.Available) {
                viewModelScope.launch {
                    Toast.makeText(application.applicationContext, "You need an internet connection to perform this operation", Toast.LENGTH_LONG).show()
                    _data.postValue(WriteResult(false, R.string.no_internet))
                }
                return
            }
            Amplify.DataStore.save(
                playist,
                {
                    /*val player = it.item().player.copyOfBuilder()
                        .build()
                    savePlayerToDb(player)*/
                    subscribeToTopic(playist.name.trim().replace(" ", "_"))
                    CoroutineScope(Dispatchers.Main).launch {
                        _data.postValue(
                            WriteResult(
                                true,
                                R.string.request_playlist_added
                            )
                        )
                    }
                },
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        _data.postValue(
                            WriteResult(
                                false,
                                R.string.sth_went_wrong
                            )
                        )
                    }
                }
            )
        }

        fun createNewPlaylist() {
            val uuid = UUID.randomUUID().toString()
            var playlistBuilder: RequestPlaylist.Builder? = null
            if (!playlistName.value.isNullOrEmpty() && playlistPlayer.value != null) {
                playlistBuilder = RequestPlaylist.Builder()
                playlistBuilder.key(uuid)
                playlistBuilder.name(playlistName.value)
                playlistDesc.value?.let {
                    playlistBuilder.desc(it)
                }
                playlistBuilder.createdDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                playlistBuilder.updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                playlistBuilder.ownerData(currentAuthUserString)
                playlistBuilder.player(playlistPlayer.value)

                savePlaylistToDb(playlistBuilder.build())
            }
        }

        fun editPlaylist() {
            playlistToEdit.value?.let {
                val playlistBuilder: RequestPlaylist.Builder = it.copyOfBuilder()

                playlistName.value?.let {
                    playlistBuilder.name(playlistName.value)
                }

                playlistDesc.value?.let {
                    playlistBuilder.desc(it)
                }

                playlistPlayer.value?.let {
                    playlistBuilder.player(playlistPlayer.value)
                }

                playlistBuilder.updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))

                savePlaylistToDb(playlistBuilder.build())
            }
        }

        fun addSongToPlaylist(song: Song, owner: String = "") {
            val requestSong = song.toRequestSong(playlistToEdit.value!!, Temporal.DateTime(Calendar.getInstance().time, 0), Temporal.DateTime(Calendar.getInstance().time, 0), currentAuthUserString)

            unsubscribeFromTopic(playlistToEdit.value!!.name.trim().replace(" ", "_"))

            val data = NotificationData("New song requested", requestSong.name)
            sendNotification(PushNotification(MessageData(playlistToEdit.value!!.name.trim().replace(" ", "_"), data, OtherNotificationData(requestSong.key, playlistToEdit.value!!.key, playlistToEdit.value!!.player.key, owner))))

            /*Amplify.DataStore.save(
                requestSong,
                {
                    Amplify.API.query(
                        ModelQuery.get(RequestSong::class.java, it.item().key),
                        { song ->
                            Log.e("API MUTATION GET REQUEST SONG", song.data.name)
                        },
                        { exception ->
                            exception.printStackTrace()
                            Log.e("API MUTATION GET REQUEST SONG EXCEPTION", exception.message.toString())
                        }
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        _data.postValue(
                            WriteResult(
                                true,
                                R.string.request_song_added_to_playlist
                            )
                        )
                    }
                },
                {
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            )*/
        }

        fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
            if (connectivityStatus.value != ConnectivityObserver.Status.Available) {
                viewModelScope.launch {
                    Toast.makeText(application.applicationContext, "You need an internet connection to perform this operation", Toast.LENGTH_LONG).show()
                    _data.postValue(WriteResult(false, R.string.no_internet))
                }
                return@launch
            }
            val jsonPL = Gson().toJson(notification)
            Log.e("JSON PAYLOAD", jsonPL)
            try {
                RetrofitInstance.token = sharedPreferences.getString(SERVER_KEY, "")!!
                if (RetrofitInstance.token.isEmpty()) return@launch
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    Log.e("SEND NOTIFICATION RESPONSE", Gson().toJson(response))
                    _data.postValue(WriteResult(true))
                } else {
                    Log.e("SEND NOTIFICATION ERROR", response.errorBody().toString())
                    _data.postValue(WriteResult(false, R.string.sth_went_wrong))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SEND NOTIFICATION EXCEPTION", e.message.toString())
                _data.postValue(WriteResult(false, R.string.sth_went_wrong))
            }
        }

        fun subscribeToTopic(topic: String) {
            if (connectivityStatus.value != ConnectivityObserver.Status.Available) {
                viewModelScope.launch {
                    Toast.makeText(application.applicationContext, "You may not be able to process requests for your player sessions due to lack of connectivity", Toast.LENGTH_LONG).show()
                }
            }
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.e("SUBSCRIBED TO TOPIC", "Topic $topic")
                } else {
                    Log.e("SUBSCRIBE TO TOPIC ERROR", it.exception?.message.toString())
                }
            }
        }

        fun unsubscribeFromTopic(topic: String) {
            if (connectivityStatus.value != ConnectivityObserver.Status.Available) {
                viewModelScope.launch {
                    Toast.makeText(application.applicationContext, "A network operation failed because your internet is unavailable, please check and try again.", Toast.LENGTH_LONG).show()
                }
            }
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.e("UNSUBSCRIBED FROM TOPIC", "Topic $topic")
                } else {
                    Log.e("UNSUBSCRIBE FROM TOPIC ERROR", it.exception?.message.toString())
                }
            }
        }

        fun clearResult(@StringRes success: Int? = null) {
            _data.postValue(WriteResult(false, success))
        }
    }
}

internal data class WriteResult(val success: Boolean, @StringRes val message: Int? = null)