package com.hepimusic.main.search

import android.animation.Animator
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hepimusic.R
import com.hepimusic.common.fadeInSlideInHorizontally
import com.hepimusic.common.fadeOutSlideOutHorizontally
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentSearchBinding
import com.hepimusic.main.common.data.Model
import com.hepimusic.playback.PlaybackViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: SearchViewModel // by sharedViewModel()
    private lateinit var playbackViewModel: PlaybackViewModel
    private val handler = Handler()
    private var ascendingSortOrder = true
    private val minCharacterLength = 2
    private val queryDelayInMills = 500L
    private var filterAnimator: Animator? = null
    private var progressAnimator: Animator? = null
    private var query: String = ""
    private val results = mutableListOf(
        Result(R.string.songs, Type.Songs),
        Result(R.string.albums, Type.Albums),
        Result(R.string.artists, Type.Artists),
        Result(R.string.genres, Type.Genres),
        Result(R.string.playlist, Type.Playlists)
    )
    lateinit var binding: FragmentSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        playbackViewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        viewModel.init(playbackViewModel.liveBrowser)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root // inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.songsResults.observe(viewLifecycleOwner, Observer { updateResultState(Type.Songs, it) })

        viewModel.albumsResults.observe(viewLifecycleOwner, Observer { updateResultState(Type.Albums, it) })

        viewModel.artistsResults.observe(viewLifecycleOwner, Observer { updateResultState(Type.Artists, it) })

        viewModel.genresResults.observe(viewLifecycleOwner, Observer { updateResultState(Type.Genres, it) })

        viewModel.playlistResults.observe(viewLifecycleOwner, Observer { updateResultState(Type.Playlists, it) })

        viewModel.searchNavigation.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                if (it.navigatorExtras == null) {
                    findNavController().safeNavigate(it.directions)
                } else {
                    findNavController().safeNavigate(it.directions, it.navigatorExtras)
                }
            }
        })

        viewModel.resultSize.observe(viewLifecycleOwner, Observer {
            hideProgressBar()
            filterAnimator?.cancel()
            filterAnimator = if (it == 0) {
                binding.searchStatus.setText(getString(R.string.no_results, query))
                binding.resultsGroup.visibility = View.INVISIBLE
                binding.emptySearchGroup.visibility = View.VISIBLE
                if (binding.filterButton.visibility == View.VISIBLE) binding.filterButton.fadeOutSlideOutHorizontally(duration = 500) else null

            } else {
                binding.emptySearchGroup.visibility = View.INVISIBLE
                binding.resultsGroup.visibility = View.VISIBLE
                if (binding.filterButton.visibility != View.VISIBLE) binding.filterButton.fadeInSlideInHorizontally(duration = 500) else null
            }
            filterAnimator?.start()
        })
    }

    private fun setupViews() {
        // Show keyboard
        binding.searchText.requestFocus()
        binding.searchText.addTextChangedListener(searchTextWatcher)

        // Setup viewpager
        binding.resultsPager.adapter = SearchAdapter(childFragmentManager, activity, results)
        binding.resultsTab.setupWithViewPager(binding.resultsPager)
        binding.resultsPager.isSaveFromParentEnabled = false

        // Click listeners
        binding.sectionBackButton.setOnClickListener(this)
        binding.filterButton.setOnClickListener(this)
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            /*super.onTextChanged(s, start, before, count)*/

            // Remove any pending schedule to execute search function
            handler.removeCallbacksAndMessages(null)

            if (s == null || s.length <= minCharacterLength) {
                // Remove all fragments
                results.forEach { it.hasResults = false }
                binding.searchStatus.setText(getString(R.string.search_hint))
                binding.resultsGroup.visibility = View.INVISIBLE
                binding.emptySearchGroup.visibility = View.VISIBLE
                updateAdapter()
                hideProgressBar()
                if (binding.filterButton.visibility == View.VISIBLE) binding.filterButton.fadeOutSlideOutHorizontally().start()
                return
            }

            showProgressBar()
            filterAnimator?.cancel()
            filterAnimator = binding.filterButton.fadeOutSlideOutHorizontally(duration = 500).apply { start() }
            // Schedule execution of search to queryDelayInMills after query text changes
            handler.postDelayed({ query(s.toString()) }, queryDelayInMills)
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    private fun updateAdapter() = (binding.resultsPager.adapter as SearchAdapter).updateResults(results)

    private fun updateResultState(t: Type, items: List<Model>) {
        results.first { it.type == t }.hasResults = items.isNotEmpty()
        updateAdapter()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sectionBackButton -> findNavController().popBackStack()
            R.id.filterButton -> toggleFilter()
        }
    }

    private fun toggleFilter() {
        ascendingSortOrder = !ascendingSortOrder
        query(binding.searchText.text.toString())
    }

    private fun query(query: String) {
        val trim = query.trim()
        this.query = trim
        viewModel.query(trim, ascendingSortOrder)
    }

    private fun showProgressBar() {
        progressAnimator?.cancel()
        progressAnimator = binding.progressBar.fadeInSlideInHorizontally(duration = 500)
        progressAnimator?.start()
    }

    private fun hideProgressBar() {
        progressAnimator?.cancel()
        progressAnimator = binding.progressBar.fadeOutSlideOutHorizontally(duration = 500)
        progressAnimator?.start()
    }

    override fun onDestroyView() {
        filterAnimator?.cancel()
        progressAnimator?.cancel()
        handler.removeCallbacksAndMessages(null)
        binding.searchText.removeTextChangedListener(searchTextWatcher)
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}