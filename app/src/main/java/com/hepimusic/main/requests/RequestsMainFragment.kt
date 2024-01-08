package com.hepimusic.main.requests

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.amplifyframework.core.Amplify
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.common.networkobserver.ConnectivityObserver
import com.hepimusic.common.networkobserver.NetworkConnectivityObserver
import com.hepimusic.databinding.FragmentRequestsMainBinding
import com.hepimusic.main.common.utils.Utils
import com.hepimusic.main.profile.ProfileViewModel
import com.hepimusic.main.requests.common.toRequestPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RequestsMainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestsMainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding: FragmentRequestsMainBinding
    lateinit var viewModel: RequestsViewModel
    lateinit var profileViewModel: ProfileViewModel
    val translationYaxis = 100f
    val interpolater = OvershootInterpolator()
    var menuOpen: Boolean = false
    var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRequestsMainBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[RequestsViewModel::class.java]
        viewModel.initialize()
        profileViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        showMenu()

        val connectivityManager = NetworkConnectivityObserver(requireContext())
        connectivityManager.observe()
            .onEach {
                if (viewModel.connectivityStatus.value != null && viewModel.connectivityStatus.value == ConnectivityObserver.Status.Available) {
                    Toast.makeText(requireContext(), "Internet connection restored", Toast.LENGTH_LONG).show()
                }
                viewModel.connectivityStatus.postValue(it)
                Log.e("REQUESTS MAIN FRAGMENT", "Internet is $it - ${it.name}")
                if (it == ConnectivityObserver.Status.Lost) {
                    Toast.makeText(requireContext(), "Your internet connection is lost, most request features  may require an internet connection.", Toast.LENGTH_LONG).show()
                }
            }
            .catch {
                it.printStackTrace()
                Log.e("REQUESTS MAIN FRAGMENT", "Error on connectivity observer: ${it.message.toString()}")
            }
            .launchIn(lifecycleScope)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun showMenu() {
        binding.requestsFAB.setImageResource(R.drawable.ic_add)
        binding.fabone.alpha = 0f
        binding.fabtwo.alpha = 0f

        binding.fabone.translationY = translationYaxis
        binding.fabtwo.translationY = translationYaxis

        binding.requestsFAB.setOnClickListener {
            if (menuOpen) {
                closeMenu()
            } else {
                openMenu()
            }
        }

        binding.fabone.setOnClickListener {
            Log.e("FAB", "fabone is clicked")
            job?.cancel()
            job = CoroutineScope(Job() + Dispatchers.Main).launch {
                setupAddPlayer()
            }
            closeMenu()
        }
        binding.fabtwo.setOnClickListener {
            Log.e("FAB", "fabtwo is clicked")
            closeMenu()
        }

        binding.fabOverlay.setOnClickListener {
            Log.e("FAB", "faboverlay is clicked")
            closeMenu()
        }

    }

    private fun openMenu() {
        menuOpen = !menuOpen
        binding.fabOverlay.visibility = View.VISIBLE
        binding.requestsFAB.setImageResource(R.drawable.ic_close)
        binding.fabone.animate().translationY(0f).alpha(1f).setInterpolator(interpolater).setDuration(300).start()
        binding.fabtwo.animate().translationY(0f).alpha(1f).setInterpolator(interpolater).setDuration(300).start()
    }

    private fun closeMenu() {
        menuOpen = !menuOpen
        binding.fabOverlay.visibility = View.GONE
        binding.requestsFAB.setImageResource(R.drawable.ic_add)
        binding.fabone.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolater).setDuration(300).start()
        binding.fabtwo.animate().translationY(translationYaxis).alpha(0f).setInterpolator(interpolater).setDuration(300).start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root // return inflater.inflate(R.layout.fragment_requests_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.requestsBottomNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    private fun setupAddPlayer() {
        var username: String? = null
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_add_player)
        dialog.window?.setLayout(650, 800)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.show()

        val playerNameField = dialog.findViewById<EditText>(R.id.playerNameField)
        val playerDescField = dialog.findViewById<EditText>(R.id.playerDescField)
        val writePlayerButton = dialog.findViewById<Button>(R.id.writePlayer)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)

        viewModel.getObservable().data.observe(viewLifecycleOwner) {
            if (it.success) {
                Utils.vibrateAfterAction(activity)
                viewModel.getObservable().clearResult(R.string.saved)
                dialog.dismiss()
                job?.cancel()
            } else {
                if (it.message != null) {
                    writePlayerButton.crossFadeWidth(
                        progressBar,
                        visibility = View.INVISIBLE,
                        duration = 0
                    )
                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                    viewModel.getObservable().clearResult()
                }
            }
        }

        fun validateData() : Boolean {
            viewModel.getObservable().pplayerName.postValue(playerNameField.text.toString())
            viewModel.getObservable().pplayerDesc.postValue(playerDescField.text.toString())
            viewModel.getObservable().location.value?.let {
                viewModel.getObservable().pplayerLong.postValue(it.longitude.toString())
                viewModel.getObservable().pplayerLat.postValue(it.latitude.toString())
            }
            if (playerNameField.text.isEmpty()) {
                return false
            }
            return true
        }

        fun enableWriteButton() {
            writePlayerButton.isEnabled = validateData()
        }

        profileViewModel.username.value?.let {
            playerNameField.setText(it)
            username = it
            viewModel.players.value?.map { requestPlayer ->
                requestPlayer.owners?.let { owners ->
                    owners.map { owner ->
                        if (owner.contains(username!!, true)) {
                            playerDescField.setText(requestPlayer.desc)
                            viewModel.getObservable()._playerToEdit.postValue(requestPlayer.toRequestPlayer())
                        } else {
                            enableWriteButton()
                        }
                    }
                }
            }
        }

        if (username.isNullOrEmpty()) {
            profileViewModel.profile.value?.let { profile ->
                profile.originalProfile.email?.let {
                    if (it.isNotEmpty()) {
                        username = it.split("@")[0]
                    }
                }
                profile.originalProfile.name?.let {
                    if (it.isNotEmpty()) {
                        username = it
                    }
                }
                playerNameField.setText(username)

                viewModel.players.value?.map { requestPlayer ->
                    requestPlayer.owners?.let { owners ->
                        owners.map { owner ->
                            if (owner.contains(username!!, true)) {
                                playerDescField.setText(requestPlayer.desc)
                                viewModel.getObservable()._playerToEdit.postValue(requestPlayer.toRequestPlayer())
                            } else {
                                enableWriteButton()
                            }
                        }
                    }
                }
            }
        }

        playerNameField.setTextColor(Color.BLACK)
        playerDescField.setTextColor(Color.BLACK)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                enableWriteButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        }

        playerNameField.addTextChangedListener(textWatcher)
        playerDescField.addTextChangedListener(textWatcher)

        writePlayerButton.setOnClickListener {
            progressBar.crossFadeWidth(writePlayerButton, visibility = View.INVISIBLE, duration = 0)
            if (viewModel.players.value.isNullOrEmpty()) {
                viewModel.getObservable().createNewPlayer()
            }
            viewModel.players.value?.map { requestPlayer ->
                if (!requestPlayer.owners.isNullOrEmpty()) {
                    if (username != null) {
                        requestPlayer.owners.map {
                            if (it.contains(username!!, true)) {
                                viewModel.getObservable()._playerToEdit.postValue(requestPlayer.toRequestPlayer())
                                viewModel.getObservable().editPlayer()
                            } else {
                                viewModel.getObservable().createNewPlayer()
                            }
                        }
                    } else {
                        viewModel.getObservable().createNewPlayer()
                    }
                } else {
                    viewModel.getObservable().createNewPlayer()
                }
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
         * @return A new instance of fragment RequestsMainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RequestsMainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val fragment = requireActivity().supportFragmentManager.findFragmentById(R.id.requestsBottomNavHostFragment)
        val fragmentBP = requireActivity().supportFragmentManager.findFragmentById(R.id.requestsBottomPlaybackFragment)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        if (fragment != null && fragmentBP != null) {
            fragmentTransaction.remove(fragment)
            fragmentTransaction.remove(fragmentBP)
            fragmentTransaction.commitAllowingStateLoss()
        }
        viewModel.job?.cancel()
    }
}