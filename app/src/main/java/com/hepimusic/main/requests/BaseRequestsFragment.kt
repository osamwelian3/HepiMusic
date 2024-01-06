package com.hepimusic.main.requests

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.temporal.Temporal
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.common.toRequestPlayer
import com.hepimusic.main.requests.location.networkConnect.LocationTask
import com.hepimusic.main.requests.location.networkConnect.WiFiDirectBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.Calendar

abstract class BaseRequestsFragment: Fragment(), ChannelListener, LocationTask.LocationListener  {

    lateinit var baseViewModel: RequestsViewModel
    lateinit var baseProfileViewModel: ProfileViewModel
    // WIFI DIRECT

    var manager: WifiP2pManager? = null
    private var isWifiP2pEnabled = false
    private var retryChannel = false

    val intentFilter = IntentFilter().apply {
        addAction(WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }
    var channel: Channel? = null
    private var receiver: BroadcastReceiver? = null

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    fun setIsWifiP2pEnabled(isWifiP2pEnabled: Boolean) {
        this.isWifiP2pEnabled = isWifiP2pEnabled
        if (isWifiP2pEnabled) {
            baseViewModel.wifiP2PEnabled.postValue(true)
        } else {
            baseViewModel.wifiP2PEnabled.postValue(false)
        }
    }

    val peerListListener = PeerListListener { deviceList ->
        baseViewModel.wifiP2PDeviceList.postValue(deviceList.deviceList.toMutableList())
    }

    val connectionInfoListener = ConnectionInfoListener { wifiP2pInfo ->
        baseViewModel.wifiP2pInfo.postValue(wifiP2pInfo)
    }

    fun updateThisDevice(device: WifiP2pDevice) {
        baseViewModel.wifiP2pDevice.postValue(device)
        CoroutineScope(Dispatchers.IO).launch {
            val player = baseViewModel.players.value!!.find { it.ownerData.contains(baseViewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST") }
            if (player != null) {
                if (player.requestPlayer.device == null || !player.requestPlayer.device.equals(device.deviceName)) {
                    Amplify.DataStore.save(
                        player.toRequestPlayer().copyOfBuilder()
                            .device(device.deviceName)
                            .updatedDate(Temporal.DateTime(Calendar.getInstance().time, 0))
                            .build(),
                        {
                            Log.e(TAG, "DEVICE NAME UPDATED")
                            Toast.makeText(requireContext(), "DEVICE NAME UPDATED", Toast.LENGTH_LONG).show()
                        },
                        {
                            it.printStackTrace()
                            Log.e(TAG, "DEVICE NAME UPDATE FAILED")
                            Toast.makeText(requireContext(), "DEVICE NAME UPDATE FAILED", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }

    // WIFI DIRECT

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WIFI DIRECT

        manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager?.initialize(requireActivity(), requireActivity().mainLooper, this)
        channel?.also { channel ->
            receiver = WiFiDirectBroadcastReceiver(manager, channel, this)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            channel?.let { channell ->
                manager?.requestDeviceInfo(channell) { device ->
                    Log.e(TAG, "DEVICE NAME: ${device?.deviceName}")
                    device?.let {
                        CoroutineScope(Dispatchers.IO).launch {
                            val player = baseViewModel.players.value!!.find { it.ownerData.contains(baseViewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST") }
                            if (player != null) {
                                if (player.requestPlayer.device == null || !player.requestPlayer.device.equals(device.deviceName)) {
                                    Amplify.DataStore.save(
                                        player.toRequestPlayer().copyOfBuilder()
                                            .device(device.deviceName)
                                            .build(),
                                        {
                                            Log.e(TAG, "DEVICE NAME UPDATED")
                                            Toast.makeText(requireContext(), "DEVICE NAME UPDATED", Toast.LENGTH_LONG).show()
                                        },
                                        {
                                            it.printStackTrace()
                                            Log.e(TAG, "DEVICE NAME UPDATE FAILED")
                                            Toast.makeText(requireContext(), "DEVICE NAME UPDATE FAILED", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // WIFI DIRECT

    }

    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            requireActivity().registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            requireActivity().unregisterReceiver(receiver)
        }
    }


    fun resetData() {
        baseViewModel.wifiP2pDevice.postValue(null)
        baseViewModel.wifiP2PDeviceList.postValue(null)
        baseViewModel.wifiP2pInfo.postValue(null)
        baseViewModel.wifiP2PEnabled.postValue(false)
    }

    override fun onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            activity?.let {
                Toast.makeText(it, "Channel lost. Trying again", Toast.LENGTH_LONG).show()
                resetData()
                retryChannel = true
                manager!!.initialize(requireActivity(), requireActivity().mainLooper, this)
            }
        } else {
            activity?.let {
                Toast.makeText(it,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    /*
    Set WifiP2p Device Name
     */
    private fun setDeviceName(manager: WifiP2pManager, channel: Channel, deviceName: String){
        //set device name programatically
        try {
            val paramTypes = arrayOf<Class<*>>(
                Channel::class.java,
                String::class.java,
                ActionListener::class.java
            )

            val setDeviceName: Method = manager.javaClass.getMethod("setDeviceName", *paramTypes)

            setDeviceName.setAccessible(true);

            val arglist = arrayOfNulls<Any>(3)
            arglist[0] = channel
            arglist[1] = deviceName
            arglist[2] = object : ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "Device Renamed successfully")
                    Toast.makeText(requireContext(), "Player Named $deviceName", Toast.LENGTH_LONG).show()
                }

                override fun onFailure(p0: Int) {
                    Log.e(TAG, "Device Rename Failed")
                    Toast.makeText(requireContext(), "Player Naming Failed", Toast.LENGTH_LONG).show()
                }
            };

            setDeviceName.invoke(manager, *arglist);

        }
        catch (e: NoSuchMethodException) {
            e.printStackTrace();
        } catch (e: IllegalArgumentException) {
            e.printStackTrace();
        } catch (e: InvocationTargetException) {
            e.printStackTrace();
        } catch (e: IllegalAccessException) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    fun createGroup(name: String) {
        manager?.let {
            channel?.let {
                setDeviceName(manager!!, channel!!, name)
            }
        }
        manager?.also { manager ->

            manager.requestGroupInfo(channel) { group ->
                Log.d(TAG, "createGroup group:$group")
            }

            manager.createGroup(channel, object : ActionListener {
                override fun onSuccess() {
                    Log.e(TAG, "create group success")
                    Toast.makeText(requireActivity(), "create group success", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(reason: Int) {
                    Log.e(TAG, "create group failure")
                    Toast.makeText(requireActivity(), "create group failure", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun disconnect(onSuccess: () -> Unit, onFailure: (reason: Int) -> Unit) {
        manager?.let { manager ->
            channel?.let { channel ->
                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    return
                }

                manager.requestGroupInfo(channel
                ) { group ->
                    if (group == null) {
                        onSuccess.invoke()
                    }
                    group?.let {
                        manager.removeGroup(channel, object : ActionListener {
                            override fun onSuccess() {
                                Log.e(TAG, "Remove group success ${group.owner.deviceName} - ${group.networkName}")
                                onSuccess.invoke()
                            }

                            override fun onFailure(reason: Int) {
                                Log.e(TAG, "Remove group failure ${group.owner.deviceName} - ${group.networkName}")
                                onFailure.invoke(reason)
                            }
                        })
                    }
                }
            }
        }
    }

    override fun onLocationReceived(location: String, ip: String, loc: String) {
        val message = "Location: $location\nIP: $ip"
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "WIFI DIRECT FRAGMENT"
    }
}