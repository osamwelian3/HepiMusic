package com.hepimusic.main.requests.users

import android.Manifest
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.SparseArrayCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.datastore.generated.model.RequestPlaylistCopy
import com.amplifyframework.datastore.generated.model.RequestSong
import com.google.gson.Gson
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.databinding.FragmentPlaylistFragmentBinding
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.RequestsViewModel
import com.hepimusic.main.requests.common.RequestsBaseAdapter
import com.hepimusic.models.mappers.toMediaItem
import com.hepimusic.playback.PlaybackViewModel
import kotlinx.coroutines.Job

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment(), View.OnClickListener, OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentPlaylistFragmentBinding
    lateinit var viewModel: RequestsViewModel
    lateinit var profileViewModel: ProfileViewModel
    lateinit var playbackViewModel: PlaybackViewModel

    lateinit var playlistKey: String
    var playlist: RequestPlaylistCopy? = null

    var requestSongs = emptyList<RequestSong>()

    lateinit var preferences: SharedPreferences

    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPlaylistFragmentBinding.inflate(LayoutInflater.from(requireContext()))
        preferences = requireActivity().getSharedPreferences("main", MODE_PRIVATE)
        viewModel = ViewModelProvider(requireActivity())[RequestsViewModel::class.java]
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        playlistKey = requireArguments().getString("playlistKey")!!
        playlist = viewModel.playlists.value?.find { it.key == playlistKey }
        if (playlist == null) {
            findNavController().popBackStack()
            return
        }
        playlist?.songs?.let {
            requestSongs = playlist!!.songs.sortedBy { it.createdAt }
        }
        viewModel.getObservable()._playlistToEdit.postValue(playlist!!.playlist)
        viewModel.getObservable().subscribeToTopic(playlist!!.name.trim().replace(" ", "_"))

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
        // return inflater.inflate(R.layout.fragment_playlist, container, false)
        binding.let {
            it.playlist = playlist
            return it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.sectionBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.moreOptions.setOnClickListener {
            setupAddSongToPlaylist()
        }

        val variables = SparseArrayCompat<Any>(1)
        variables.put(BR.userID, profileViewModel.userId.value!!)

        val requestSongsAdapter = RequestsBaseAdapter(
            requestSongs.sortedBy { it.requests.size }.sortedByDescending { it.createdDate }, requireActivity(), R.layout.request_song_item, BR.requestSong, this, null,
            setOf(R.anim.fast_fade_in), true, variables = variables
        )
        binding.requestPlaylistSongsRV.adapter = requestSongsAdapter
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.requestPlaylistSongsRV.layoutManager = layoutManager
        binding.requestPlaylistSongsRV.getChildAt(0)?.requestFocus()

        if (playlist!!.ownerData.contains(viewModel.currentAuthUserString ?: "SADNJNDDSCJNIJNJCDNJNIDNNJJNUCDCKDJNNCNHJD") && requestSongs.isNotEmpty() && playbackViewModel.isPlaying.value == false) {
            playbackViewModel.playAll(
                requestSongs[0].key,
                requestSongs.map { it.toMediaItem() })
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun observeViewModel() {
        binding.sectionBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.requestSongs.observe(viewLifecycleOwner) { requestSongList ->
            requestSongs = requestSongList.filter { it.playlist.key == playlist!!.key }
            (binding.requestPlaylistSongsRV.adapter as RequestsBaseAdapter<RequestSong>).updateItems(requestSongs.sortedBy { it.requests.size }.sortedByDescending { it.createdDate })
            binding.requestPlaylistSongsRV.getChildAt(0)?.requestFocus()

            if (playlist!!.ownerData.contains(viewModel.currentAuthUserString ?: "SADNJNDDSCJNIJNJCDNJNIDNNJJNUCDCKDJNNCNHJD") && requestSongs.isNotEmpty() && playbackViewModel.isPlaying.value == false) {
                playbackViewModel.playAll(
                    requestSongs[0].key,
                    requestSongs.map { it.toMediaItem() })
            }

        }

        viewModel.getObservable().addSongsToPlaylistClicked.observe(viewLifecycleOwner) { clicked ->
            if (clicked) {
                viewModel.getObservable()._addSongsToPlaylistClicked.postValue(false)

            }
        }

        MediaItemTree.liveDataRequestSong.observe(viewLifecycleOwner) { liveDataRequestSong ->
            val existingSong = requestSongs.find { it.key == liveDataRequestSong.key }
            if (existingSong == null) {
                val copy = requestSongs.toMutableList()
                copy.add(liveDataRequestSong)
                viewModel._requestSongs.postValue(copy)
            }
        }
    }

    private fun setupAddSongToPlaylist() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_request_song_spinner)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        viewModel.getObservable().data.observe(viewLifecycleOwner) {
            if (it.success || it.message == null) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                Toast.makeText(requireContext(), "Request sent to player.", Toast.LENGTH_LONG).show()
                dialog.dismiss()
                job?.cancel()
            } else {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        val selectionTitle = dialog.findViewById<TextView>(R.id.selectionTitle)
        val editText = dialog.findViewById<EditText>(R.id.editText)
        val listView = dialog.findViewById<ListView>(R.id.listView)

        selectionTitle.text = "Select or search for an Song"
        selectionTitle.setTextColor(Color.WHITE)
        editText.setTextColor(Color.WHITE)

        var songsList = viewModel.songs.value ?: emptyList()
        val songSuggestions = mutableListOf<String>()
        songSuggestions.addAll(songsList.map { it.name })

        val adapter = ArrayAdapter<String>(requireContext(), /*androidx.appcompat.R.layout.support_simple_spinner_dropdown_item*/R.layout.searchable_song_item, R.id.text_view, songSuggestions)
        adapter.setNotifyOnChange(true)

        viewModel.songs.observe(viewLifecycleOwner) {
            songsList = it
            songSuggestions.clear()
            songSuggestions.addAll(songsList.map { it.name })
            adapter.clear()
            adapter.addAll(songsList.map { it.name })
            adapter.notifyDataSetChanged()
        }

        listView.adapter = adapter
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.getObservable()._songToAdd.postValue(songsList[position])
                viewModel.getObservable()._playlistToEdit.postValue(playlist!!.playlist)
                val authUser = Gson().fromJson(viewModel.currentAuthUserString, AuthUser::class.java)
                viewModel.getObservable().addSongToPlaylist(songsList[position], authUser?.userId ?: "")
//                dialog.dismiss()
            }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistFragmentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onClick(v: View?) {

    }

    override fun onItemClick(position: Int, sharableView: View?) {

    }
}