package com.hepimusic.main.common.dataBinding

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hepimusic.R
import com.hepimusic.main.albums.Album
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.common.image.CircularTransparentCenter
import com.hepimusic.main.explore.RecentlyPlayed
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.songs.Song
import com.hepimusic.playback.MediaItemData
import java.io.File

object DataBindingAdapters {
    @JvmStatic val centerCrop = CenterCrop()
    @JvmStatic val circleCrop = CircleCrop()

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAlbumCover(view: ImageView, song: Song?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(song?.artWork)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    /*@BindingAdapter("android:src")
    @JvmStatic
    fun setImageButton(view: ImageButton, @DrawableRes res: Int?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.thumb_circular_default)
                    .error(R.drawable.thumb_circular_default)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(res)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }*/

//    @BindingAdapter("android:src")
//    @JvmStatic
//    fun setAlbumCover(view: ImageView, uri: Uri?) {
//        Glide.with(view)
//            .setDefaultRequestOptions(
//                RequestOptions()
//                    .placeholder(R.drawable.thumb_circular_default)
//                    .error(R.drawable.thumb_circular_default)
//                    .diskCacheStrategy(DiskCacheStrategy.DATA)
//            )
//            .load(uri)
//            .transform(
//                MultiTransformation(centerCrop, circleCrop)
//            )
//            .placeholder(R.drawable.thumb_circular_default)
//            .into(view)
//    }

    @BindingAdapter("mediaSrc")
    @JvmStatic
    fun setAlbumCoverCompat(view: ImageView, item: MediaItem?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.thumb_circular_default_hollow)
                    .error(R.drawable.thumb_circular_default_hollow)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(item?.mediaMetadata?.artworkUri)
            .transform(
                MultiTransformation(centerCrop, circleCrop, CircularTransparentCenter(.3F))
            )
            .placeholder(R.drawable.thumb_circular_default_hollow)
            .into(view)
    }

    @BindingAdapter("repeatSrc")
    @JvmStatic
    fun setRepeatModeSrc(view: ImageView, repeat: Int?) {
        val src = when (repeat) {
            Player.REPEAT_MODE_ALL -> R.drawable.ic_repeat_all
            Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_once
            else -> R.drawable.ic_repeat_none
        }
        view.setImageResource(src)
    }

    @BindingAdapter("shuffleSrc")
    @JvmStatic
    fun setShuffleModeSrc(view: ImageView, shuffle: Boolean?) {
        val src = if (shuffle!!) {
            R.drawable.ic_shuffle_off
        } else {
            R.drawable.ic_shuffle_on
        }
        view.setImageResource(src)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAlbumCover(view: ImageView, mediaItem: MediaItemData?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(mediaItem?.albumArtUri)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAlbumCover(view: ImageView, item: RecentlyPlayed?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(Uri.parse(item?.artWorkUri))
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }


    @BindingAdapter("mediaSrc")
    @JvmStatic
    fun setAlbumCoverCompat(view: ImageView, item: MediaItemData?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(item?.albumArtUri)
            .transform(
                MultiTransformation(centerCrop, circleCrop/*, CircularTransparentCenter(.3F)*/)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAlbumCover(view: ImageView, album: Album) {
        Log.e("ARTWORK URI", album.albumArt.toString())
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(album.albumArt)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setArtistAvatar(view: ImageView, artist: Artist) {
        Glide.with(view)
            .load(artist.albumArt)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setPlaylistCover(view: ImageView, playlist: Playlist) {
        val uri = FileProvider.getUriForFile(view.context.applicationContext, "${view.context.applicationContext.packageName}.provider", File(view.context.applicationContext.filesDir, playlist.name))
        Log.e("URI", uri.toString())
        Glide.with(view)
            .load(/*playlist.modForViewWidth(view.measuredWidth)*/uri)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:bottomcoversrc")
    @JvmStatic
    fun setBottomPlaybackCover(view: ImageView, metadata: MediaMetadata?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.thumb_circular_default)
                    .error(R.drawable.thumb_circular_default)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(metadata?.artworkUri)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("artistSrc")
    @JvmStatic
    fun setAlbumSrc(view: ImageView, album: Album) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(album.albumArt)
            .transform(
                MultiTransformation(centerCrop, FitCenter(), RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    @BindingAdapter("playlistSrc")
    @JvmStatic
    fun setPlaylistSrc(view: ImageView, playlist: Playlist) {
        val uri = FileProvider.getUriForFile(view.context.applicationContext, "${view.context.applicationContext.packageName}.provider", File(view.context.applicationContext.filesDir, playlist.name))
        Glide.with(view)
            .load(uri)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    @BindingAdapter("android:srco")
    @JvmStatic
    fun setImageResource(imageView: ImageView, resource: Int) {
        imageView.setImageResource(resource)
    }

    @BindingAdapter("android:srcp")
    @JvmStatic
    fun setImageRes(imageView: ImageButton, resource: Drawable) {
        imageView.setImageDrawable(resource)
    }
}