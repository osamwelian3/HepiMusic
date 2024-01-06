package com.hepimusic.main.artists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hepimusic.BR
import com.hepimusic.R
import com.hepimusic.common.safeNavigate
import com.hepimusic.common.safeNavigationOnClickListener
import com.hepimusic.databinding.FragmentArtistsBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseAdapter
import com.hepimusic.main.common.view.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ArtistsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ArtistsFragment : BaseFragment(), OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var items: List<Artist> = emptyList()
    private lateinit var viewModel: ArtistsViewModel
    lateinit var binding: FragmentArtistsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentArtistsBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[ArtistsViewModel::class.java]
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
        return binding.root // inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*viewModel = ViewModelProvider(requireActivity())[ArtistsViewModel::class.java]*/
        viewModel.init()
        setupRecyclerView()
        observeViewModel()
        binding.navigationIcon.setOnClickListener(
            Navigation.safeNavigationOnClickListener(
                R.id.artistsFragment,
                R.id.action_artistsFragment_to_navigationDialogFragment
            )
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeViewModel() {
        if (items.isEmpty()) {
            viewModel.items.observe(viewLifecycleOwner, Observer {
                this.items = it
                (binding.artistsRV.adapter as BaseAdapter<Artist>).updateItems(it)
            })
        } else {
            viewModel.overrideCurrentItems(items)

        }
    }

    private fun setupRecyclerView() {
        val adapter = BaseAdapter(items, requireActivity(), R.layout.item_artist, BR.artist, this)
        binding.artistsRV.adapter = adapter

        val layoutManager = LinearLayoutManager(activity)
        binding.artistsRV.layoutManager = layoutManager
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        val transitionName = ViewCompat.getTransitionName(sharableView!!)!!
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(sharableView, transitionName)
            .build()
        val action =
            ArtistsFragmentDirections.actionArtistsFragmentToArtistAlbumsFragment(items[position], transitionName)
        findNavController().safeNavigate(action, extras)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ArtistsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ArtistsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}