package com.hepimusic.main.requests.location.networkConnect

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.fadeIn
import com.hepimusic.main.requests.BaseRequestsFragment
import com.hepimusic.main.requests.players.PlayerControlFragment
import com.hepimusic.main.requests.users.PlayersFragment
import com.hepimusic.ui.MainActivity

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
class WiFiDirectBroadcastReceiver
/**
 * @param manager WifiP2pManager system service
 * @param channel Wifi p2p channel
 * @param activity activity associated with the receiver
 */
(private val manager: WifiP2pManager?, private val channel: Channel,
 private val fragment: BaseRequestsFragment
) : BroadcastReceiver() {

    val activity = fragment.requireActivity() as MainActivity

    companion object {
        private const val TAG = "WifiDirectReceiver"
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION == action) {
            // UI update to indicate wifi p2p status.
            val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                fragment.setIsWifiP2pEnabled(true)
            } else {
                fragment.setIsWifiP2pEnabled(false)
                fragment.resetData()

            }
            Log.d(TAG, "P2P state changed - $state")
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION == action) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.d(TAG, "P2P peers changed")
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                try {
                    manager?.requestPeers(channel, fragment.peerListListener)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return
            }
            manager?.requestPeers(channel, fragment.peerListListener)
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION == action) {
            if (manager == null) {
                return
            }

            val networkInfo = intent
                    .getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo
            Log.d(TAG, "P2P connection changed $networkInfo")

            if (networkInfo.isConnected) {

                // we are connected with the other device, request connection
                // info to find group owner IP

                manager.requestConnectionInfo(channel, fragment.connectionInfoListener)
            } else {
                // It's a disconnect
                fragment.resetData()
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION == action) {
            val wifiP2pDevice = intent.getParcelableExtra<Parcelable>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE) as WifiP2pDevice
            Log.d(TAG, "P2P this device changed $wifiP2pDevice")

            fragment.updateThisDevice(wifiP2pDevice)

        }
    }
}
