package com.hepimusic.main.requests.users

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.*
import android.location.Location
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.collection.SparseArrayCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlayerCopy
import com.amplifyframework.geo.location.models.AmazonLocationPlace
import com.amplifyframework.geo.maplibre.util.toJsonElement
import com.amplifyframework.geo.maplibre.view.AmplifyMapView
import com.amplifyframework.geo.models.Coordinates
import com.amplifyframework.geo.models.CountryCode
import com.amplifyframework.geo.models.SearchArea
import com.amplifyframework.geo.options.GeoSearchByTextOptions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentPlayersBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.BaseRequestsFragment
import com.hepimusic.main.requests.RequestsViewModel
import com.hepimusic.main.requests.common.RequestsBaseAdapter
import com.hepimusic.main.requests.common.toRequestPlayer
import com.hepimusic.main.requests.location.DefaultLocationClient
import com.hepimusic.main.requests.location.LocationClient
import com.hepimusic.main.requests.location.networkConnect.LocationTask
import com.hepimusic.main.requests.location.networkConnect.WiFiDirectBroadcastReceiver
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class PlayersFragment : BaseRequestsFragment(), View.OnClickListener, OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentPlayersBinding
    lateinit var locationClient: LocationClient
    lateinit var viewModel: RequestsViewModel
    lateinit var profileViewModel: ProfileViewModel
    lateinit var mapView: AmplifyMapView
    var locationJob: Job? = null
    var playersJob: Job? = null
    var players = emptyList<RequestPlayerCopy>()

    private var mySymbol: Symbol? = null
    private var playerSymbols = mutableListOf<Symbol>()
    private var previousLocation: Location? = null
    private var polyline: LineOptions? = null
    private var lineLayerId: String? = null

    private val amplifyMapView by lazy {
        requireActivity().findViewById<AmplifyMapView>(R.id.mapView)
    }

    private val requestNearbyPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {

        } else {
            Log.e("NOTIFICATION PERMISSION DENIED", "FCM NOTIFICATION PERMISSION DENIED")
            Toast.makeText(requireContext(), "FCM NOTIFICATION PERMISSION DENIED", Toast.LENGTH_LONG).show()
        }
    }

    private fun askForNearbyDevicesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.NEARBY_WIFI_DEVICES)) {
                requestNearbyPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                requestNearbyPermissionLauncher.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {

        } else {
            Log.e("NOTIFICATION PERMISSION DENIED", "FCM NOTIFICATION PERMISSION DENIED")
            Toast.makeText(requireContext(), "FCM NOTIFICATION PERMISSION DENIED", Toast.LENGTH_LONG).show()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        amplifyMapView.mapView
        askNotificationPermission()
        askForNearbyDevicesPermission()

        binding = FragmentPlayersBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[RequestsViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        baseViewModel = viewModel
        baseProfileViewModel = profileViewModel

        locationClient = DefaultLocationClient(
            requireContext(),
            LocationServices.getFusedLocationProviderClient(requireContext())
        )
        mapView = binding.mapView

        Amplify.Geo.getAvailableMaps(
            {
                for (mapStyle in it) {
                    Log.i("MyAmplifyApp", mapStyle.toString())
                }
            },
            { Log.e("MyAmplifyApp", "Failed to get available maps.", it) }
        )

        /*mapView.onPlaceSelect { amazonLocationPlace, symbol ->
            symbol.iconImage = "custompoint"
            mapView.mapView.symbolManager.update(symbol)
            Log.i("MyAmplifyApp", "The selected place is ${amazonLocationPlace.label}")
            Log.i("MyAmplifyApp", "It is located at ${amazonLocationPlace.coordinates}")
        }*/

        mapView.onPlaceSelectListener = object : AmplifyMapView.OnPlaceSelectListener {
            override fun onSelect(place: AmazonLocationPlace, symbol: Symbol) {
                if (place.label?.contains("current") == true) {
                    symbol.iconImage = "custompoint"
                    mapView.mapView.symbolManager.update(symbol)
                } else {
                    symbol.iconImage = "hepimusic"
                    mapView.mapView.symbolManager.update(symbol)
                }
                Log.e("PLACE LABEL", place.label.toString())
                Log.e("PLACE COORDINATES", place.coordinates.toString())
                val player = players.find { it.name == place.label }
                player?.let {
                    val position = players.indexOf(it)
                    findNavController().safeNavigate(
                        PlayersFragmentDirections.actionPlayersFragment2ToPlayerControlFragment(players[position].key)
                    )
                }
            }
        }

        getLocation()

        val position = Coordinates(1.0, 38.0)
        val options = GeoSearchByTextOptions.builder()
            .maxResults(50)
            .searchArea(SearchArea.near(position))
            .countries(listOf(CountryCode.KEN))
            .build()
        mapView.searchField.onSearchAction { searchQuery ->
            Amplify.Geo.searchByText(
                searchQuery,
                options,
                {
                    for (place in it.places) {
                        val p = (place as AmazonLocationPlace)
                        Log.e("SEARCH RESULT PLACE", p.toJsonElement().asString)
                    }
                },
                {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                    it.printStackTrace()
                }
            )
        }



        /*mapView.overlayLayout.setOnClickListener {
            Log.e("MAPVIEW OVERLAY CLICKED", it.accessibilityClassName.toString())
        }*/
        /*binding.mapView.onPlaceSelectListener = object : AmplifyMapView.OnPlaceSelectListener {
            override fun onSelect(place: AmazonLocationPlace, symbol: Symbol) {

            }

        }*/
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun getLocation() {
        locationJob = locationClient.getLocationUpdates(3000L)
            .catch { e ->
                e.printStackTrace()
                createLocationRequest()
            }
            .onEach { location ->
                val lat = location.latitude
                val long = location.longitude
                Log.e("LATLONG", "Lat: $lat, Long: $long")
                if (playersJob == null) {
                    viewModel.getObservable()._location.postValue(location)
                    playersJob = CoroutineScope(Dispatchers.Main).launch {
                        observePlayers()
                    }
                }
                mapView.mapView.getStyle { map, style ->
                    try {
                        style.addImage("custompoint", AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_my_location_24)!!.toBitmap())
                        style.addImage("hepimusic", AppCompatResources.getDrawable(requireContext(), R.drawable.thumb_circular_default)!!.toBitmap(24, 24))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Log.e("MAP STYLE", style.json)
                    val spaceNeedle = LatLng(lat, long);
                    if (mySymbol == null) {
                        previousLocation = location
                        val place = AmazonLocationPlace(
                            Coordinates(location.latitude, location.longitude),
                            "Your current estimated location",
                            "Address Number",
                            "Street",
                            "Kenya",
                            "Region Africa",
                            "subRegion EA",
                            "Municipality",
                            "Neighbourhood",
                            "Postal Code"
                        )

                        /*val symbolManager = SymbolManager(mapView.mapView, map, style)
                        mySymbol = symbolManager.create(
                            SymbolOptions()
                                .withIconImage("place")
                                .withLatLng(spaceNeedle)
                                .withData(place.toJsonElement())
                        )*/
                        try {
                            mySymbol = mapView.mapView.symbolManager.create(
                                SymbolOptions()
                                    .withIconImage("custompoint")
                                    .withLatLng(spaceNeedle)
                                    .withData(place.toJsonElement())
                            )
                            viewModel.getObservable()._location.postValue(location)
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(spaceNeedle, 16.0))
                            addPlayersMarkers(players.map { it.toRequestPlayer() })
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Log.e(TAG, e.message.toString())
                            Toast.makeText(requireContext(), "Error initializing map. Check your network connection and restart.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (location.distanceTo(previousLocation!!) != 50f) {
                            Log.e(
                                "DISTANCE TO PREV",
                                location.distanceTo(previousLocation!!).toString()
                            )
                            // mapView.mapView.symbolManager.delete(mySymbol)
                            CoroutineScope(Dispatchers.IO).launch {
                                val player = baseViewModel.players.value!!.find { it.ownerData.contains(baseViewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST") }
                                if (player != null) {
                                    Amplify.DataStore.save(
                                        player.toRequestPlayer().copyOfBuilder()
                                            .longitude(long.toString())
                                            .latitude(lat.toString())
                                            .build(),
                                        {
                                            Log.e(TAG, "DEVICE LOCATION UPDATED")
                                            Toast.makeText(requireContext(), "DEVICE LOCATION UPDATED", Toast.LENGTH_LONG).show()
                                        },
                                        {
                                            it.printStackTrace()
                                            Log.e(TAG, "DEVICE LOCATION UPDATE FAILED")
                                            Toast.makeText(requireContext(), "DEVICE LOCATION UPDATE FAILED", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                            previousLocation = location

                            mySymbol?.let {
                                val place = AmazonLocationPlace(
                                    Coordinates(location.latitude, location.longitude),
                                    "Your current estimated location",
                                    "Address Number",
                                    "Street",
                                    "Kenya",
                                    "Region Africa",
                                    "subRegion EA",
                                    "Municipality",
                                    "Neighbourhood",
                                    "Postal Code"
                                )
                                it.iconImage = "custompoint"
                                it.data = place.toJsonElement()
                                it.geometry =
                                    Point.fromLngLat(spaceNeedle.longitude, spaceNeedle.latitude)

                                mapView.mapView.symbolManager.update(it)
                                viewModel.getObservable()._location.postValue(location)
                            }
                        }
                    }

                }
            }.launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Main))
        locationJob?.start()
    }

    private fun addPlayersMarkers(players: List<RequestPlayer>) {
        playerSymbols.let {
            if (it.isNotEmpty()) {
                mapView.mapView.symbolManager.delete(it)
                playerSymbols.clear()
            }
        }
        for (player in players) {
            val place = AmazonLocationPlace(
                Coordinates(player.latitude.toDouble(), player.longitude.toDouble()),
                player.name,
                player.desc,
                "",
                "Kenya",
                "Region Africa",
                "subRegion EA",
                "",
                "",
                ""
            )
            val spaceNeedle = LatLng(player.latitude.toDouble(), player.longitude.toDouble())
            val symbol = mapView.mapView.symbolManager.create(
                SymbolOptions()
                    .withIconImage("hepimusic")
                    .withLatLng(spaceNeedle)
                    .withData(place.toJsonElement())
            )
            playerSymbols.add(symbol)
            mapView.mapView.symbolManager.update(playerSymbols)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root // return inflater.inflate(R.layout.fragment_players, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val navController = activity?.findNavController(R.id.mainNavHostFragment)
            navController?.setGraph(R.navigation.navigation_graph)
            Toast.makeText(requireContext(), "Disconnecting nearby services before exiting Hepi Requests", Toast.LENGTH_LONG).show()
            disconnect(
                {
                    navController?.popBackStack()
                },
                {
                    Toast.makeText(requireContext(), "Something went wrong while disconnecting nearby services. Exiting anyway.", Toast.LENGTH_LONG).show()
                    navController?.popBackStack()
                }
            )
        }
        setUpViews()
        observeViewModel()
    }

    @SuppressLint("MissingPermission")
    private fun setUpViews() {
        binding.swipeContainer.setOnRefreshListener(this)
        binding.searchViewButton.setOnClickListener {
            if (binding.viewSwitcher.currentView != binding.searchView) {
                binding.viewSwitcher.showPrevious()
            }
        }

        binding.mapViewButton.setOnClickListener {
            if (binding.viewSwitcher.currentView != binding.mapViewView) {
                binding.viewSwitcher.showNext()
            }
        }

        var variables : SparseArrayCompat<Any>? = null

        profileViewModel.profile.value?.let { profile ->
            val username =
                profile.originalProfile.name
            val email =
                profile.originalProfile.email

            variables = SparseArrayCompat<Any>(1)
            variables?.put(BR.userNameOrEmail, viewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"/*.split("::").last()*/)
        }

        val playersAdapter = RequestsBaseAdapter(
            players.sortedByDescending { it.createdDate }, requireActivity(), R.layout.request_player_item, BR.requestPlayer, this, null,
            setOf(R.anim.fast_fade_in), true, variables = variables
        )
        binding.playersRV.adapter = playersAdapter
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.playersRV.layoutManager = layoutManager

        manager?.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "Discovery Initialized")
                Toast.makeText(requireContext(), "Discovery Initialized", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Discovery Failed")
                Toast.makeText(requireContext(), "Discovery Failed", Toast.LENGTH_LONG).show()

            }
        })
    }

    @Suppress("UNCHECKED_CAST")
    fun observeViewModel() {

    }

    fun locationInstance(lat: Double, long: Double) : Location {
        val targetLocation = Location("")
        targetLocation.latitude = lat
        targetLocation.longitude = long
        return targetLocation
    }

    @Suppress("UNCHECKED_CAST")
    fun observePlayers() {
        val location = viewModel.getObservable().location.value!!
        viewModel.players.value?.let { requestPlayerList ->
            profileViewModel.profile.value?.let { profile ->
                val username = profile.originalProfile.name?.ifEmpty { "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                val email = profile.originalProfile.email?.ifEmpty { "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST"
                requestPlayerList.map { requestPlayer ->
                    if (requestPlayer.ownerData.contains(viewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST")) {
                        viewModel.startAdvertising(requestPlayer.name+" [WIFI]")
                        viewModel.startDiscovery()
                    } else {
                        viewModel.startDiscovery()
                    }
                    requestPlayer.owners?.map { owner ->
                        players = if (owner.takeLast(username.length).contains(username, true) || owner.takeLast(email.length).contains(email, true)) {
                            requestPlayerList.filter { locationInstance(it.latitude.toDouble(), it.longitude.toDouble()).distanceTo(location) < 5000 }
                        } else {
                            requestPlayerList.filter {  locationInstance(it.latitude.toDouble(), it.longitude.toDouble()).distanceTo(location) < 5000 }
                        }
                    }

                }
            }
            (binding.playersRV.adapter as RequestsBaseAdapter<RequestPlayerCopy>).updateItems(players.sortedByDescending { it.createdDate })
        }
        viewModel.players.observe(viewLifecycleOwner) { requestPlayerList ->
            try {
                addPlayersMarkers(requestPlayerList.map { it.toRequestPlayer() })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            profileViewModel.profile.value?.let { profile ->
                val username = profile.originalProfile.name?.ifEmpty { "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                val email = profile.originalProfile.email?.ifEmpty { "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST"
                requestPlayerList.map { requestPlayer ->
                    val authUser = Gson().fromJson(viewModel.currentAuthUserString, AuthUser::class.java)
                    if (requestPlayer.ownerData.contains(viewModel.currentAuthUserString ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST")) {
                        viewModel.startAdvertising(requestPlayer.name+" [WIFI]")
                        viewModel.startDiscovery()
                    } else {
                        viewModel.startDiscovery()
                    }
                    requestPlayer.owners?.map { owner ->
                        players = if (owner.takeLast(username.length).contains(username, true) || owner.takeLast(email.length).contains(email, true)) {
                            requestPlayerList.filter { /*it.key != requestPlayer.key &&*/ locationInstance(it.latitude.toDouble(), it.longitude.toDouble()).distanceTo(location) < 5000 }
                        } else {
                            requestPlayerList.filter {  locationInstance(it.latitude.toDouble(), it.longitude.toDouble()).distanceTo(location) < 5000 }
                        }
                    }

                }
            }
            (binding.playersRV.adapter as RequestsBaseAdapter<RequestPlayerCopy>).updateItems(players.sortedByDescending { it.createdDate })
        }

        viewModel.foundEndpointIds.observe(viewLifecycleOwner) { foundPlayers ->
            if (!foundPlayers.isNullOrEmpty()) {
                for (player in foundPlayers) {
                    player.player?.let {
                        val tempPlayers = players.toMutableList()
                        val newPlayer = it.copyOfBuilder()
                            .name(player.info.endpointName)
                            .build()
                        tempPlayers.add(newPlayer)
                        players = tempPlayers
                    }
                }
                (binding.playersRV.adapter as RequestsBaseAdapter<RequestPlayerCopy>).updateItems(players.sortedByDescending { it.createdDate })
            }
        }

        viewModel.wifiP2PDeviceList.observe(viewLifecycleOwner) { foundPlayers ->
            if (!foundPlayers.isNullOrEmpty()) {
                val tempList = players.toMutableList()
                for (plyr in players) {
                    if (plyr.key.contains("[WIFI]")) {
                        tempList.removeIf { it.key == plyr.key }
                        players = tempList
                    }
                }
                for (wifiplayer in foundPlayers) {
                    Log.e(TAG, wifiplayer.deviceName)
                    for (plyr in players) {
                        if (plyr.requestPlayer.device != null && plyr.requestPlayer.device.contains(wifiplayer.deviceName, true)) {
                            val newPlyr = plyr.copyOfBuilder()
                                .key("[WIFI]${plyr.key}")
                                .name(plyr.name + " on device: ${wifiplayer.deviceName}")
                                .build()
                            tempList.add(newPlyr)
                            players = tempList
                        }
                    }
                }
                (binding.playersRV.adapter as RequestsBaseAdapter<RequestPlayerCopy>).updateItems(players.sortedByDescending { it.createdDate })
            } else {
                for (plyr in players) {
                    if (plyr.key.contains("[WIFI]")) {
                        val tempList = players.toMutableList()
                        tempList.removeIf { it.key == plyr.key }
                        players = tempList
                    }
                }
                (binding.playersRV.adapter as RequestsBaseAdapter<RequestPlayerCopy>).updateItems(players.sortedByDescending { it.createdDate })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        locationJob?.isActive ?: locationJob?.start()
    }


    companion object {
        private const val TAG = "PLAYERS FRAGMENT"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayersFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayersFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun createLocationRequest(interval: Long = 10000) {
        val locationRequest = LocationRequest.Builder(interval)
            .setIntervalMillis(interval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)


        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            getLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(requireActivity(),
                        Constants.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationJob?.cancel()
        /*disconnect()*/
    }

    override fun onClick(v: View?) {

    }

    override fun onItemClick(position: Int, sharableView: View?) {
        findNavController().safeNavigate(
            PlayersFragmentDirections.actionPlayersFragment2ToPlayerControlFragment(players[position].key.replace("[WIFI]", ""))
        )
    }

    @SuppressLint("MissingPermission")
    override fun onRefresh() {
        binding.swipeContainer.isRefreshing = true
        manager?.discoverPeers(channel, object : ActionListener {
            override fun onSuccess() {
                Log.e(TAG, "Discovery Initialized")
                Toast.makeText(requireContext(), "Discovery Initialized", Toast.LENGTH_LONG).show()
                binding.swipeContainer.isRefreshing = false
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Discovery Failed")
                Toast.makeText(requireContext(), "Discovery Failed", Toast.LENGTH_LONG).show()
                binding.swipeContainer.isRefreshing = false
            }
        })
        /*disconnect(
            {
                requireActivity().unregisterReceiver(WiFiDirectBroadcastReceiver(manager, channel!!, this))

                manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
                channel = manager?.initialize(requireActivity(), requireActivity().mainLooper, this)

                requireActivity().registerReceiver(WiFiDirectBroadcastReceiver(manager, channel!!, this), intentFilter)

                manager?.discoverPeers(channel, object : ActionListener {
                    override fun onSuccess() {
                        Log.e(TAG, "Discovery Initialized")
                        Toast.makeText(requireContext(), "Discovery Initialized", Toast.LENGTH_LONG).show()
                        binding.swipeContainer.isRefreshing = false
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Discovery Failed")
                        Toast.makeText(requireContext(), "Discovery Failed", Toast.LENGTH_LONG).show()
                        binding.swipeContainer.isRefreshing = false
                    }
                })
            },
            {
                requireActivity().unregisterReceiver(WiFiDirectBroadcastReceiver(manager, channel!!, this))

                manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
                channel = manager?.initialize(requireActivity(), requireActivity().mainLooper, this)

                requireActivity().registerReceiver(WiFiDirectBroadcastReceiver(manager, channel!!, this), intentFilter)

                manager?.discoverPeers(channel, object : ActionListener {
                    override fun onSuccess() {
                        Log.e(TAG, "Discovery Initialized")
                        Toast.makeText(requireContext(), "Discovery Initialized", Toast.LENGTH_LONG).show()
                        binding.swipeContainer.isRefreshing = false
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Discovery Failed")
                        Toast.makeText(requireContext(), "Discovery Failed", Toast.LENGTH_LONG).show()
                        binding.swipeContainer.isRefreshing = false
                    }
                })
            }
        )*/

    }


}