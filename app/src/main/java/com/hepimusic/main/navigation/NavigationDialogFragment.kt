package com.hepimusic.main.navigation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.hepimusic.R
import com.hepimusic.auth.LoginActivity
import com.hepimusic.common.Constants
import com.hepimusic.databinding.FragmentNavigationDialogBinding
import com.hepimusic.main.common.callbacks.OnItemClickListener
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment
import com.hepimusic.main.dragSwipe.ItemTouchHelperAdapter
import com.hepimusic.main.dragSwipe.OnStartDragListener
import com.hepimusic.main.dragSwipe.SimpleItemTouchHelperCallback
import com.hepimusic.utils.BlurKit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NavigationDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class NavigationDialogFragment : BaseFullscreenDialogFragment(), OnStartDragListener,
    ItemTouchHelperAdapter,
    OnItemClickListener {

    lateinit var binding: FragmentNavigationDialogBinding

    private var origin: Int? = null
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var adapter: NavAdapter
    private val viewModel: NavViewModel by viewModels()
    private var items: List<NavItem> = emptyList()
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main)

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentNavigationDialogBinding.inflate(LayoutInflater.from(requireContext()))
        arguments?.let {
            origin = it.getInt("origin")
        }
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
        return binding.root // inflater.inflate(R.layout.fragment_navigation_dialog, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NavigationDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NavigationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init(origin)
        setupRecyclerView()
        observeViewModel()
        binding.closeButton.setOnClickListener { findNavController().popBackStack() }
    }


    private fun observeViewModel() {
        viewModel.navItemsAdded.observe(viewLifecycleOwner, Observer { added ->
            if (added) {
                viewModel.navItems?.observe(viewLifecycleOwner, Observer {
                    this.items = it
                    adapter.updateItems(it)
                })
            }
        })
        viewModel.navItems?.observe(viewLifecycleOwner, Observer {
            this.items = it
            adapter.updateItems(it)
        })
    }

    private fun setupRecyclerView() {
        adapter = NavAdapter(items, this, this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter

        val layoutManager = GridLayoutManager(activity, 3)
        binding.recyclerView.layoutManager = layoutManager

        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }


    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // This is not the best place to do this.
        // Model modification should be done in the repository but I currently can't find a way to do this in the
        // repository and communicate back to this View that items has moved and the indexes of the moved items.

        // So what I am doing here is, swapping the items, notifying the adapter and notifying the repository
        // to save the new indexes in  SharePreferences
        Collections.swap(items, fromPosition, toPosition)
        adapter.updateItems(items, fromPosition, toPosition)
        viewModel.swap(items)
        return true
    }


    private fun setBlurredBackground() {
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                BitmapDrawable(resources, getBlurredBitmap())
            }
            binding.rootView.background = bitmap
        }
    }

    private fun getBlurredBitmap(): Bitmap? {
        val viewGroup = activity?.findViewById<ViewGroup>(R.id.container)
        if (viewGroup != null) {
            if (viewGroup.width > 0 && viewGroup.height > 0) {
                return BlurKit.getInstance(requireContext())
                    .fastBlur(viewGroup, 10, 0.3F)
            }
        }
        return null
    }

    override fun onItemClick(position: Int, sharableView: View?) {
        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.bottomNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        when (items[position].id) {
            Constants.LOGOUT -> {
                if (sharableView != null) {
                    sharableView.visibility = View.GONE
                }
                Amplify.Auth.signOut {
                    lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            findNavController().popBackStack()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            navController.navigate(R.id.action_navigationDialogFragment_to_exploreFragment)
                        }
                        startActivity(Intent(requireActivity(), LoginActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }
        val navId = when (items[position].id) {
            Constants.NAV_SONGS -> R.id.action_navigationDialogFragment_to_songsFragment
            Constants.NAV_PLAYLIST -> R.id.action_navigationDialogFragment_to_playlistFragment
            Constants.NAV_ARTISTS -> R.id.action_navigationDialogFragment_to_artistsFragment
            Constants.NAV_GENRES -> R.id.action_navigationDialogFragment_to_genresFragment
            Constants.NAV_PROFILE -> R.id.action_navigationDialogFragment_to_profileFragment
            else -> null
        }

        if (navId != null) navController.navigate(navId) // findNavController()

    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            it.window?.setLayout(width, height)
        }
        setBlurredBackground()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemTouchHelper.attachToRecyclerView(null)
        binding.recyclerView?.adapter = null
    }
}