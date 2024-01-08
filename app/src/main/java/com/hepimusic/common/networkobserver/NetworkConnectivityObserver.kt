package com.hepimusic.common.networkobserver

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.main.requests.MessageData
import com.hepimusic.main.requests.NotificationData
import com.hepimusic.main.requests.OtherNotificationData
import com.hepimusic.main.requests.PushNotification
import com.hepimusic.main.requests.RetrofitInstance
import com.hepimusic.main.requests.WriteResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkConnectivityObserver(
    val context: Context
): ConnectivityObserver {

    private val sharedPreferences = context.getSharedPreferences("main", Context.MODE_PRIVATE)
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val notification = PushNotification(MessageData("testing_network_connectivity", NotificationData("title", "body"), OtherNotificationData("songKey", "playlistKey", "playerKey", "owner")))

    override fun observe(): Flow<ConnectivityObserver.Status> {
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch {
                        try {
                            val response = RetrofitInstance.api2.getLocation()
                            if (response.isSuccessful) {
                                Log.e("GET LOCATION RESPONSE", Gson().toJson(response))
                                send(ConnectivityObserver.Status.Available)
                            } else {
                                Log.e("GET LOCATION ERROR", response.errorBody().toString())
                                send(ConnectivityObserver.Status.Available)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("GET LOCATION EXCEPTION", e.message.toString())
                            send(ConnectivityObserver.Status.Lost)
                        }
                    }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch {
                        try {
                            val response = RetrofitInstance.api2.getLocation()
                            if (response.isSuccessful) {
                                Log.e("GET LOCATION RESPONSE", Gson().toJson(response))
                                send(ConnectivityObserver.Status.Available)
                            } else {
                                Log.e("GET LOCATION ERROR", response.errorBody().toString())
                                send(ConnectivityObserver.Status.Losing)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e("GET LOCATION EXCEPTION", e.message.toString())
                            send(ConnectivityObserver.Status.Losing)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(ConnectivityObserver.Status.Lost) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(ConnectivityObserver.Status.Unavailable) }
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                }

                override fun onLinkPropertiesChanged(
                    network: Network,
                    linkProperties: LinkProperties
                ) {
                    super.onLinkPropertiesChanged(network, linkProperties)
                }

                override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
                    super.onBlockedStatusChanged(network, blocked)
                }
            }

            connectivityManager.registerDefaultNetworkCallback(callback)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
    }
}