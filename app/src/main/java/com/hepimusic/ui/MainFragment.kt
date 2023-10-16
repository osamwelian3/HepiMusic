package com.hepimusic.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.NavHost
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hepimusic.R
import com.hepimusic.databinding.FragmentMainBinding
import com.hepimusic.main.explore.ExploreFragment
import com.hepimusic.main.explore.ExploreViewModel
import com.hepimusic.main.songs.SongsViewModel
import com.hepimusic.playback.BottomPlaybackFragment
import com.hepimusic.playback.PlaybackViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var playbackViewModel: PlaybackViewModel
    lateinit var songsViewModel: SongsViewModel
    val exploreViewModel: ExploreViewModel by activityViewModels()

    val navHostF = NavHostFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(LayoutInflater.from(requireContext()))

        // Dynamically add BottomPlaybackFragment
//        requireActivity().supportFragmentManager.beginTransaction().add(R.id.bottomPlaybackFragment, BottomPlaybackFragment()).commit()
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*playbackViewModel = ViewModelProvider(requireActivity()).get(PlaybackViewModel::class.java)
        songsViewModel = ViewModelProvider(requireActivity()).get(SongsViewModel::class.java)

        songsViewModel.isBrowserConnected.observeForever { connected ->
            if (connected) {
                songsViewModel.loadData()
            }
        }

        exploreViewModel.isBrowserConnected.observeForever { connected ->
            if (connected) {
                exploreViewModel.loadData()
            }
        }*/

        /*val navigationBar: BottomNavigationView = view.findViewById(R.id.navigationBar)

        val navController = Navigation.findNavController(requireActivity(), R.id.bottomNavHostFragment)
        navigationBar.setupWithNavController(navController)*/

        // Dynamically add navHostFragment

        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.bottomNavHostFragment) as NavHostFragment
         val navController = navHostFragment.navController // Navigation.findNavController(requireActivity(), R.id.bottomNavHostFragment)
        binding.navigationBar.setupWithNavController(navController)
        /*requireActivity().supportFragmentManager.beginTransaction().replace(R.id.bottomNavHostFragment, navHostF).commitNow()
        navHostF.navController.setGraph(R.navigation.navigation_graph)
        binding.navigationBar.setupWithNavController(navHostF.navController)*/

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}