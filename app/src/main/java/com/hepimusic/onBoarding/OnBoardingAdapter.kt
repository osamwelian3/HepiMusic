package com.hepimusic.onBoarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hepimusic.databinding.ItemOnBoardingBinding

class OnBoardingAdapter(private val context: Context, private val boards: Array<Board>, val glide: RequestManager) : PagerAdapter() {

    lateinit var binding: ItemOnBoardingBinding

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        binding = ItemOnBoardingBinding.inflate(LayoutInflater.from(context))
        val layout = binding.root // LayoutInflater.from(context).inflate(R.layout.item_on_boarding, container, false)

        val board = boards[position]
        glide.load(board.image)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(15)))
            .into(binding.onBoardingImg) // layout.findViewById(R.id.onBoardingImg)

//        layout.findViewById<TextView>(R.id.onBoardingTitle).text = context.getString(board.title)
//        layout.findViewById<TextView>(R.id.onBoardingDescription).text = context.getString(board.description)
        binding.onBoardingTitle.text = context.getString(board.title)
        binding.onBoardingDescription.text = context.getString(board.description)

        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
        return boards.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(boards[position].title)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

}