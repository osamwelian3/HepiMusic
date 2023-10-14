package com.hepimusic.onBoarding

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.edit
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.R
import com.hepimusic.common.BaseActivity
import com.hepimusic.databinding.ActivityOnBoardingBinding
import com.hepimusic.getStarted.GetStartedActivity
import com.hepimusic.main.common.callbacks.OnPageChangeListener
import com.hepimusic.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class OnBoardingActivity : BaseActivity(), OnPageChangeListener, View.OnClickListener {

    private lateinit var preferences: SharedPreferences

    private lateinit var binding: ActivityOnBoardingBinding

    private lateinit var glide: RequestManager

    private lateinit var onBoardingRoot: MotionLayout
    private lateinit var viewPager: ViewPager
    private lateinit var skipButton: Button
    private lateinit var next: Button
    private lateinit var gotIt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        preferences = getSharedPreferences("main", Context.MODE_PRIVATE)
        glide = Glide.with(this).setDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.drawable.album_art)
                .error(R.drawable.album_art)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
        )
        onBoardingRoot = binding.onBoardingRoot // findViewById(R.id.onBoardingRoot)
        viewPager = binding.viewPager // findViewById(R.id.viewPager)
        skipButton = binding.skipButton // findViewById(R.id.skipButton)
        next = binding.next // findViewById(R.id.next)
        gotIt = binding.gotIt // findViewById(R.id.gotIt)
        viewPager.adapter = OnBoardingAdapter(this, boards, glide)
        viewPager.addOnPageChangeListener(this)
        skipButton.setOnClickListener(this)
        next.setOnClickListener(this)
        gotIt.setOnClickListener(this)
    }


    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        val newProgress = (position + positionOffset) / (boards.size - 1)
        onBoardingRoot.progress = newProgress
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            skipButton.id, gotIt.id -> {
                startNextActivity()
            }
            next.id -> {
                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            }
        }
    }

    private fun startNextActivity() {
        val nextActivity =
            if (isPermissionGranted(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                MainActivity::class.java
            } else {
                GetStartedActivity::class.java
            }

        val intent = Intent(this, nextActivity)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        preferences.edit {
            putBoolean(HAS_SEEN_ON_BOARDING, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager.removeOnPageChangeListener(this)
    }

    companion object {
        const val HAS_SEEN_ON_BOARDING = "com.hepimusic.onBoarding.hasSeenOnBoarding"
    }

}


private val boards = arrayOf(
    Board(R.drawable.on_boarding1, R.string.board_title_1, R.string.board_description_1),
    Board(R.drawable.on_boarding2, R.string.board_title_2, R.string.board_description_2),
    Board(R.drawable.on_boarding3, R.string.board_title_3, R.string.board_description_3)
)