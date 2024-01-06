package com.hepimusic.main.requests.players

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.generated.model.RequestPlayer
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestSong
import com.google.firebase.messaging.RemoteMessageCreator
import com.google.gson.Gson
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentPlayerControlBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.BaseRequestsFragment
import com.hepimusic.main.requests.MessageData
import com.hepimusic.main.requests.NotificationData
import com.hepimusic.main.requests.OtherNotificationData
import com.hepimusic.main.requests.PushNotification
import com.hepimusic.main.requests.RequestsViewModel
import com.hepimusic.main.requests.common.RequestsBaseAdapter
import com.hepimusic.main.requests.common.toRequestPlayer

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlayerControlFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlayerControlFragment : BaseRequestsFragment(), View.OnClickListener, OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentPlayerControlBinding
    lateinit var viewModel: RequestsViewModel
    lateinit var profileViewModel: ProfileViewModel
    var playerKey: String = ""

    var playlists = emptyList<RequestPlaylist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlayerControlBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[RequestsViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        baseViewModel = viewModel
        baseProfileViewModel = profileViewModel

        playerKey = arguments?.getString("playerKey")!!
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root //return inflater.inflate(R.layout.fragment_player_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.myPlaylists.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.addPlaylistsFAB.setOnClickListener {
            setupAddPlaylist()
        }
        val playlistsAdapter = RequestsBaseAdapter(
            playlists.sortedByDescending { it.createdDate }, requireActivity(), R.layout.request_playlist_item, BR.requestPlaylist, this, null,
            setOf(R.anim.fast_fade_in), true
        )
        binding.playlistsRV.adapter = playlistsAdapter
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.playlistsRV.layoutManager = layoutManager
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        viewModel.players.value?.let { players ->
            profileViewModel.profile.value?.let { profile ->
                val username = profile.originalProfile.name?.ifEmpty { "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                val email = profile.originalProfile.email?.ifEmpty { "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                players.map { requestPlayer ->
                    Log.e("PLAYER OBJECT", Gson().toJson(requestPlayer))
                    Log.e("PLAYER PLAYLIST SIZE", requestPlayer.playlists?.size.toString())
                    if (requestPlayer.key == playerKey) {
                        playlists = requestPlayer.playlists ?: emptyList()
                        requestPlayer.owners?.map { owner ->
                            if (owner.takeLast(username.length).contains(username, true) || owner.takeLast(email.length).contains(email, true)) {
                                binding.addPlaylistsFAB.visibility = View.VISIBLE
                                binding.myPlaylists.setText("     My Playlists")
                                for (playlist in playlists) {
                                    viewModel.getObservable().subscribeToTopic(playlist.name.trim().replace(" ", "_"))
                                }
                                viewModel.startAdvertising(requestPlayer.name)
                                disconnect(
                                    {
                                        if (baseViewModel.wifiP2PEnabled.value == true) {
                                            createGroup(requestPlayer.name)
                                        } else {
                                            baseViewModel.wifiP2PEnabled.observe(viewLifecycleOwner) {enabled ->
                                                if (enabled) {
                                                    createGroup(requestPlayer.name)
                                                }
                                            }
                                        }
                                    },
                                    {
                                        if (baseViewModel.wifiP2PEnabled.value == true) {
                                            createGroup(requestPlayer.name)
                                        } else {
                                            baseViewModel.wifiP2PEnabled.observe(viewLifecycleOwner) {enabled ->
                                                if (enabled) {
                                                    createGroup(requestPlayer.name)
                                                }
                                            }
                                        }
                                    }
                                )

                            } else {
                                binding.myPlaylists.setText("     ${requestPlayer.name}'s Playlists")
                            }
                        }
                        viewModel.getObservable().pplaylistPlayer.postValue(requestPlayer.toRequestPlayer())
                        (binding.playlistsRV.adapter as RequestsBaseAdapter<RequestPlaylist>).updateItems(playlists.sortedByDescending { it.createdDate })
                    }
                }
            }
        }

        viewModel.players.observe(viewLifecycleOwner) { players ->
            profileViewModel.profile.value?.let { profile ->
                val username = profile.originalProfile.name?.ifEmpty { "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                val email = profile.originalProfile.email?.ifEmpty { "VERYWEIRDRANDOMEMAILTHATWILLNEVEREVEREXIST" } ?: "VERYWEIRDRANDOMUSERNAMETHATWILLNEVEREVEREXIST"
                players.map { requestPlayer ->
                    Log.e("PLAYER OBJECT", Gson().toJson(requestPlayer))
                    Log.e("PLAYER PLAYLIST SIZE", requestPlayer.playlists?.size.toString())
                    if (requestPlayer.key == playerKey) {
                        requestPlayer.owners?.map { owner ->
                            playlists = requestPlayer.playlists ?: emptyList()
                            if (owner.takeLast(username.length).contains(username, true) || owner.takeLast(email.length).contains(email, true)) {
                                binding.addPlaylistsFAB.visibility = View.VISIBLE
                                binding.myPlaylists.setText("     My Playlists")
                                for (playlist in playlists) {
                                    viewModel.getObservable().subscribeToTopic(playlist.name.trim().replace(" ", "_"))
                                }
                                viewModel.startAdvertising(requestPlayer.name)
                                disconnect(
                                    {
                                        if (baseViewModel.wifiP2PEnabled.value == true) {
                                            createGroup(requestPlayer.name)
                                        } else {
                                            baseViewModel.wifiP2PEnabled.observe(viewLifecycleOwner) {enabled ->
                                                if (enabled) {
                                                    createGroup(requestPlayer.name)
                                                }
                                            }
                                        }
                                    },
                                    {
                                        if (baseViewModel.wifiP2PEnabled.value == true) {
                                            createGroup(requestPlayer.name)
                                        } else {
                                            baseViewModel.wifiP2PEnabled.observe(viewLifecycleOwner) {enabled ->
                                                if (enabled) {
                                                    createGroup(requestPlayer.name)
                                                }
                                            }
                                        }
                                    }
                                )
                            } else {
                                binding.myPlaylists.setText("     ${requestPlayer.name}'s Playlists")
                            }
                        }
                        viewModel.getObservable().pplaylistPlayer.postValue(requestPlayer.toRequestPlayer())
                        (binding.playlistsRV.adapter as RequestsBaseAdapter<RequestPlaylist>).updateItems(playlists.sortedByDescending { it.createdDate })
                    }
                }
            }
        }
    }

    private fun setupAddPlaylist() {
        var username: String? = null
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_add_playlist)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        viewModel.getObservable().data.observe(viewLifecycleOwner) {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                dialog.dismiss()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        val playlistNameField = dialog.findViewById<EditText>(R.id.playlistNameField)
        val playlistDescField = dialog.findViewById<EditText>(R.id.playlistDescField)
        val writePlaylistButton = dialog.findViewById<Button>(R.id.writePlaylist)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)

        fun validateData() : Boolean {
            viewModel.getObservable().pplaylistName.postValue(playlistNameField.text.toString())
            viewModel.getObservable().pplaylistDesc.postValue(playlistDescField.text.toString())

            if (playlistNameField.text.isEmpty() || viewModel.getObservable().playlistPlayer.value == null) {
                return false
            }
            return true
        }

        fun enableWriteButton() {
            writePlaylistButton.isEnabled = validateData()
        }

        viewModel.getObservable().playlistToEdit.value?.let {
            playlistNameField.setText(it.name)
            playlistDescField.setText(it.desc)
        }

        playlistNameField.setTextColor(Color.BLACK)
        playlistDescField.setTextColor(Color.BLACK)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                enableWriteButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        playlistNameField.addTextChangedListener(textWatcher)
        playlistDescField.addTextChangedListener(textWatcher)

        writePlaylistButton.setOnClickListener {
            progressBar.crossFadeWidth(writePlaylistButton)
            if (viewModel.getObservable().playlistToEdit.value == null) {
                viewModel.getObservable().createNewPlaylist()
            } else {
                viewModel.getObservable().editPlaylist()
            }

        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlayerControlFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlayerControlFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {

    }

    override fun onItemClick(position: Int, sharableView: View?) {
        findNavController().safeNavigate(
            PlayerControlFragmentDirections.actionPlayerControlFragmentToPlaylistFragment2(playlists[position].key)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        /*disconnect()*/
    }

}