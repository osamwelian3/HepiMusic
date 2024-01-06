package com.hepimusic.playback

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import androidx.transition.TransitionInflater
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.hepimusic.R
import com.hepimusic.common.fadeInSlideDown
import com.hepimusic.common.fadeInSlideUp
import com.hepimusic.common.fadeOutSlideDown
import com.hepimusic.common.fadeOutSlideUp
import com.hepimusic.common.px
import com.hepimusic.common.safeNavigate
import com.hepimusic.databinding.FragmentPlaybackBinding
import com.hepimusic.main.common.callbacks.AnimatorListener
import com.hepimusic.main.common.callbacks.OnSeekBarChangeListener
import com.hepimusic.main.common.view.BaseFullscreenDialogFragment
import com.hepimusic.models.mappers.toSong
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaybackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaybackFragment : /*BaseFragment()*/ BaseFullscreenDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var userTouchingSeekBar = false
    lateinit var viewModel: PlaybackViewModel // by sharedViewModel()
    private lateinit var rotationAnimSet: AnimatorSet
    private val handler = Handler(Looper.getMainLooper())

    lateinit var binding: FragmentPlaybackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PlaybackViewModel::class.java]
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        var i = 0
        while (i < requireActivity().supportFragmentManager.backStackEntryCount){
            Log.e("BACKSTACK", requireActivity().supportFragmentManager.getBackStackEntryAt(i).name.toString())
            Log.e("BACKSTACK", requireActivity().supportFragmentManager.getBackStackEntryAt(i).id.toString())
            i++
        }
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playback, container, false)
        // Inflate the layout for this fragment
        binding.let {
            viewModel.isBrowserConnected.observe(viewLifecycleOwner) { connected ->
                if (connected) {
                    it.viewModel = viewModel
                    it.lifecycleOwner = requireActivity()
                }
            }
            return it.root
        } // inflater.inflate(R.layout.fragment_playback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewData()
        binding.albumArt.viewTreeObserver.addOnPreDrawListener {
            updatePlayerColors()
            return@addOnPreDrawListener true
        }
        updatePlayerColors()
    }

    fun invertColor(color: Int): Int {
        val red = 255 - Color.red(color)
        val green = 255 - Color.green(color)
        val blue = 255 - Color.blue(color)
        return Color.rgb(red, green, blue)
    }

    private fun updatePlayerColors() {
        val bmp: Bitmap = binding.albumArt.drawable?.toBitmap() ?: R.drawable.album_art.toDrawable().toBitmap(24, 24)
        binding.blurImageView.setImageBitmap(bmp)
        binding.blurImageView.setBlur(4)

        // player control colors
        Palette.from(bmp).generate { palette ->
            if (palette != null) {
                var swatch: Palette.Swatch? = palette.darkVibrantSwatch
                if (swatch == null) {
                    swatch = palette.mutedSwatch
                    if (swatch == null) {
                        swatch = palette.dominantSwatch
                    }
                }

                assert(swatch != null)
                // extract text colors
                val titleTextColorMain: Int = swatch!!.titleTextColor
                val bodyTextColorMain: Int = swatch.bodyTextColor
                val rgbColor: Int = swatch.rgb

                // calculate contrast between background and text colors
                val titleContrast = ColorUtils.calculateContrast(titleTextColorMain, rgbColor)
                val bodyContrast = ColorUtils.calculateContrast(bodyTextColorMain, rgbColor)
                val backgroundLuminance = ColorUtils.calculateLuminance(rgbColor)


                val titleTextColor: Int = invertColor(rgbColor) // if (titleContrast < 4.5) Color.BLACK else if (backgroundLuminance > 0.9) Color.WHITE else titleTextColorMain
                val bodyTextColor: Int = invertColor(rgbColor) // if (bodyContrast < 4.5) Color.BLACK else if (backgroundLuminance > 0.9) Color.WHITE else bodyTextColorMain

                // set color to player view
                activity?.let {
                    it.window.statusBarColor = rgbColor
                    it.window.navigationBarColor = rgbColor
                }

                // more view colors
                binding.songTitle.children.forEach { (it as TextView).setTextColor(titleTextColor) }
                binding.sectionBackButton.setTextColor(titleTextColor)
                binding.sectionBackButton.compoundDrawables[0].setTint(titleTextColor)
                binding.nowPlaying.setTextColor(bodyTextColor)
                binding.playingTracks.drawable.setTint(bodyTextColor)
                binding.countdownDuration.setTextColor(bodyTextColor)
                binding.totalDuration.setTextColor(bodyTextColor)
                binding.upVoteButton.setTextColor(bodyTextColor)
                binding.streamsCount.setTextColor(bodyTextColor)

                binding.repeatButton.drawable.setTint(bodyTextColor)
                binding.repeatButton.setColorFilter(bodyTextColor)
                binding.shuffleButton.drawable.setTint(bodyTextColor)
                binding.shuffleButton.setColorFilter(bodyTextColor)
                binding.moreOptions.drawable.setTint(bodyTextColor)
                binding.nextButton.drawable.setTint(bodyTextColor)
                binding.previousButton.drawable.setTint(bodyTextColor)
                binding.playPauseButton.forEach { (it as ImageView).drawable.setTint(titleTextColor) }

            }
        }
    }

    private fun observeViewData() {
        viewModel.mediaPosition.observe(viewLifecycleOwner) {
            if (!userTouchingSeekBar) binding.playbackSeekBar.progress = it.toInt()
        }

        viewModel.mediaBufferPosition.observe(viewLifecycleOwner) {
            if (!userTouchingSeekBar) binding.playbackSeekBar.secondaryProgress = it.toInt()
        }

        viewModel.currentItem.observe(viewLifecycleOwner) {
            binding.playbackSeekBar.max =
                viewModel.contentLength.value ?: 0 // it?.duration?.toInt() ?: 0
        }

//        viewModel.playbackState.observe(viewLifecycleOwner, Observer { updateState(it) })
        viewModel.isPlaying.observe(viewLifecycleOwner) { updateState(it) }

        viewModel.playbackState.observe(viewLifecycleOwner) {
            if (it == Player.STATE_BUFFERING) {
                updatePlayerColors()
                binding.progressBarPlay.visibility = View.VISIBLE
                binding.playPauseButton.visibility = View.INVISIBLE
            } else {
                updatePlayerColors()
                binding.playPauseButton.visibility = View.VISIBLE
                binding.progressBarPlay.visibility = View.INVISIBLE
            }
        }
        viewModel.isPlaying.observe(viewLifecycleOwner) {
            if (it) {
                binding.playPauseButton.visibility = View.VISIBLE
                binding.progressBarPlay.visibility = View.INVISIBLE
            }
        }
    }


    private fun setupView() {
        /*rotationAnimSet = AnimatorInflater.loadAnimator(activity, R.animator.album_art_rotation) as AnimatorSet
        rotationAnimSet.setTarget(binding.albumArt)*/
        binding.songTitle.children.forEach { (it as TextView).isSelected = true }
        binding.sectionBackButton.setOnClickListener(this)
        binding.lyricsButton.setOnClickListener(this)
        binding.closeButton.setOnClickListener(this)
        binding.moreOptions.setOnClickListener(this)
        binding.playingTracks.setOnClickListener(this)
        binding.playbackSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener)
        updatePlayerColors()
    }

    /*private fun updateState(state: Int) {
        if (state == Player.STATE_READY || viewModel.browser.isPlaying) {
            if (binding.playPauseButton.currentView != binding.pauseButton) binding.playPauseButton.showNext()
            if (!rotationAnimSet.isStarted) rotationAnimSet.start() else rotationAnimSet.resume()
        } else {
            rotationAnimSet.pause()
            if (binding.playPauseButton.currentView != binding.playButton) binding.playPauseButton.showPrevious()
        }
    }*/
    private fun updateState(isPlaying: Boolean) {
        if (isPlaying) {
            updatePlayerColors()
            if (binding.playPauseButton.currentView != binding.pauseButton) binding.playPauseButton.showNext()
            /*if (!rotationAnimSet.isStarted) rotationAnimSet.start() else rotationAnimSet.resume()*/
        } else {
            updatePlayerColors()
            /*rotationAnimSet.pause()*/
            if (binding.playPauseButton.currentView != binding.playButton) binding.playPauseButton.showPrevious()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.sectionBackButton -> {
                /*val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
                val navController = navHostFragment.navController
                Log.e("DESTINATION", navController.currentDestination?.displayName.toString())
                navController.safeNavigate(R.id.action_playbackFragment_to_mainFragment2)*/
                /*(requireActivity() as MainActivity).navController.popBackStack()*/
//                requireActivity().supportFragmentManager.beginTransaction().replace(R.id.mainNavHostFragment, MainFragment()).commit()
                activity?.findNavController(R.id.mainNavHostFragment)?.popBackStack()
            }
            R.id.lyricsButton -> showFindingLyrics()
            R.id.closeButton -> closeLyrics()
            R.id.moreOptions -> showMenuBottomSheet()
            R.id.playingTracks -> showCurrentTracks()
        }
    }



    private fun showCurrentTracks() {
        if ((binding.playingTracks.drawable as Animatable).isRunning) {
            return
        }

        val fragment = childFragmentManager.findFragmentByTag("CurrentSongsFragment")
        val animDrawable = AnimatedVectorDrawableCompat.create(
            requireActivity(),
            if (fragment != null) R.drawable.anim_close_to_playlist_current else R.drawable.anim_playlist_current_to_close
        )
        binding.playingTracks.setImageDrawable(animDrawable)
        (binding.playingTracks.drawable as Animatable).start()
        if (fragment == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.currentSongsContainer, CurrentSongsFragment(), "CurrentSongsFragment").commit()
        } else {
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }

    }

    private fun showMenuBottomSheet() {
        val mediaItem = viewModel.currentItem.value ?: return
        val action =
            PlaybackFragmentDirections.actionPlaybackFragmentToSongsMenuBottomSheetDialogFragment(mediaItem.mediaId, mediaItem.toSong())
        findNavController().safeNavigate(action)
    }

    private fun closeLyrics() {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            binding.albumArt.fadeInSlideDown(translationY, slideDuration),
            binding.lyricsButton.fadeInSlideDown(translationY, slideDuration),
            binding.closeButton.fadeOutSlideDown(translationY, slideDuration),
            binding.quoteImg.fadeOutSlideDown(translationY, slideDuration),
            binding.lyricsText.fadeOutSlideDown(translationY, slideDuration),
            binding.lyricsSource.fadeOutSlideDown(translationY, slideDuration)
        )
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                binding.albumArtGroup.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                binding.lyricsGroup.visibility = View.GONE
                animatorSet.removeAllListeners()
            }
        })

        animatorSet.start()
    }

    private fun showFindingLyrics() {
        if (hasLyrics()) {
            showFoundLyrics()
            return
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            binding.albumArt.fadeOutSlideUp(translationY, slideDuration),
            binding.lyricsButton.fadeOutSlideUp(translationY, slideDuration),
            binding.progressBar.fadeInSlideUp(translationY, slideDuration),
            binding.findingLyrics.fadeInSlideUp(translationY, slideDuration)
        )
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                binding.loadingLyricsGroup.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                binding.albumArtGroup.visibility = View.GONE
                Handler(Looper.getMainLooper()).postDelayed({
                    showFoundLyrics(getString(R.string.dummyLyrics), getString(R.string.dummyLyricsSource))
                }, TimeUnit.SECONDS.toMillis(4))
                animatorSet.removeAllListeners()
            }
        })

        animatorSet.start()
    }

    private fun showFoundLyrics(lyrics: String? = null, source: String? = null) {
        val lyricsIsEmpty = lyrics.isNullOrEmpty()
        if (!lyricsIsEmpty) {
            binding.lyricsText.text = lyrics
            binding.lyricsSource.text = source
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            if (!lyricsIsEmpty) binding.progressBar.fadeOutSlideUp(translationY, slideDuration) else
                binding.albumArt.fadeOutSlideUp(translationY, slideDuration),
            if (!lyricsIsEmpty) binding.findingLyrics.fadeOutSlideUp(translationY, slideDuration) else
                binding.lyricsButton.fadeOutSlideUp(translationY, slideDuration),
            binding.closeButton.fadeInSlideUp(translationY, slideDuration),
            binding.quoteImg.fadeInSlideUp(translationY, slideDuration),
            binding.lyricsText.fadeInSlideUp(translationY, slideDuration),
            binding.lyricsSource.fadeInSlideUp(translationY, slideDuration)
        )

        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                binding.lyricsGroup.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!lyricsIsEmpty) binding.loadingLyricsGroup.visibility = View.GONE else binding.albumArtGroup.visibility = View.GONE
                animatorSet.removeAllListeners()
            }
        })
        animatorSet.start()
    }


    private fun hasLyrics() = !binding.lyricsText.text.isNullOrEmpty()

    private val onSeekBarChangeListener = object : OnSeekBarChangeListener {

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            userTouchingSeekBar = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            viewModel.seek(seekBar?.progress?.toLong() ?: 0)
            // Let's delay the updating of the userTouchingSeekBar to ensure the seek has taken effect before
            // continuing to update the progress bar
            handler.postDelayed({ userTouchingSeekBar = false }, 200)
        }

    }

    override fun onDestroyView() {
        /*rotationAnimSet.cancel()*/
        super.onDestroyView()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaybackFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaybackFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}

private const val slideDuration = 600L
private val translationY = 110F.px