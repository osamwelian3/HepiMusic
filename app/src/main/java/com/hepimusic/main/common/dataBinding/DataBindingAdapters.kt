package com.hepimusic.main.common.dataBinding

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.databinding.BindingAdapter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.QuerySortBy
import com.amplifyframework.core.model.query.QuerySortOrder
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.core.model.temporal.Temporal.DateTime
import com.amplifyframework.datastore.generated.model.Creator
import com.amplifyframework.datastore.generated.model.Profile
import com.amplifyframework.datastore.generated.model.RequestPlaylist
import com.amplifyframework.datastore.generated.model.RequestSong
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.hepimusic.R
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.main.albums.Album
import com.hepimusic.main.artists.Artist
import com.hepimusic.main.common.image.CircularTransparentCenter
import com.hepimusic.main.explore.RecentlyPlayed
import com.hepimusic.main.genres.Genre
import com.hepimusic.main.playlist.Playlist
import com.hepimusic.main.songs.Song
import com.hepimusic.playback.MediaItemData
import okhttp3.internal.http.toHttpDateString
import java.io.File
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DataBindingAdapters {
    @JvmStatic val centerCrop = CenterCrop()
    @JvmStatic val circleCrop = CircleCrop()

    @BindingAdapter("genreSrc")
    @JvmStatic
    fun setGenreSrc(view: ImageView, genre: Genre) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(genre)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setRequestSongArtWork(view: ImageView, song: RequestSong?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load("https://dn1i8z7909ivj.cloudfront.net/public/"+song?.thumbnailKey)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    @BindingAdapter("android:textArtist")
    @JvmStatic
    fun setRequestSongArtist(view: TextView, requestSong: RequestSong) {
        requestSong.selectedCreator?.let {
            val artist = Gson().fromJson(requestSong.selectedCreator, Creator::class.java)
            view.setText(artist.name)
        }

        if (requestSong.selectedCreator == null) {
            view.setText(requestSong.name)
        }

    }

    @BindingAdapter("android:text")
    @JvmStatic
    fun setRequestPlaylistDate(view: TextView, temporalDateTim: Temporal.DateTime?) {
        val temporalDateTime = temporalDateTim ?: Temporal.DateTime(Date.from(Calendar.getInstance().toInstant()).toString())
        val calender = Calendar.getInstance()
        calender.time = temporalDateTime.toDate()
        val date = calender.get(Calendar.DAY_OF_MONTH)
        val month = calender.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale("EN"))

        view.setText("$month $date")
    }

    @BindingAdapter("android:textTime")
    @JvmStatic
    fun setRequestPlaylistTime(view: TextView, temporalDateTim: Temporal.DateTime?) {
        val temporalDateTime = temporalDateTim ?: Temporal.DateTime(Date.from(Calendar.getInstance().toInstant()).toString())
        val calender = Calendar.getInstance()
        calender.time = temporalDateTime.toDate()
        val hr = calender.get(Calendar.HOUR_OF_DAY)
        val hour = if (hr.toString().length == 1) "0$hr" else "$hr"
        val min = calender.get(Calendar.MINUTE)
        val minute = if (min.toString().length == 1) "0$min" else "$min"

        view.setText("$hour:$minute")
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAdminArtistAvatar(view: ImageView, artist: com.hepimusic.main.admin.creators.Creator) {
        Glide.with(view)
            .load("https://dn1i8z7909ivj.cloudfront.net/public/"+artist.originalCreator.thumbnailKey)
            .transform(
                MultiTransformation(centerCrop, circleCrop)
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setAdminAlbumAvatar(view: ImageView, album: com.hepimusic.main.admin.albums.Album) {
        Glide.with(view)
            .load("https://dn1i8z7909ivj.cloudfront.net/public/"+album.originalAlbum.thumbnailKey)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

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

    @BindingAdapter("android:srca")
    @JvmStatic
    fun setAdminAlbumCover(view: ImageView, song: com.hepimusic.main.admin.songs.Song?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load("https://dn1i8z7909ivj.cloudfront.net/public/"+song?.originalSong?.thumbnailKey)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.thumb_circular_default)
            .into(view)
    }

    @BindingAdapter("android:adminText")
    @JvmStatic
    fun setAdminArtist(view: TextView, song: com.hepimusic.main.admin.songs.Song?) {
        try {
            val artist = MediaItemTree.artists.find {
                it.key == (Gson().fromJson(
                    song?.originalSong?.selectedCreator ?: "",
                    Creator::class.java
                )?.key ?: "")
            }
            view.setText(artist?.name ?: "Unknown artist")
        } catch (e: Exception) {
            e.printStackTrace()
            view.setText("Unknown Artist")
        }

    }

    @BindingAdapter("trending")
    @JvmStatic
    fun setTrendingCover(view: ImageView, trending: Song?) {
        Glide.with(view)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.album_art)
                    .error(R.drawable.album_art)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
            .load(trending?.artWork)
            .transform(
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
            .into(view)
    }

    fun fomartnumber(number: Int): String {
        val decimalFormat = DecimalFormat("#.#")
        decimalFormat.maximumFractionDigits = 1
        return when {
            number < 1000 -> number.toString()
            number in 1000 .. 999_999 -> {
                val decimalNumber: Double = number.toDouble() / 1000
                decimalFormat.format(decimalNumber) + "K"
            }
            else -> decimalFormat.format(number.toDouble() / 1_000_000) + "M"
        }
    }

    @BindingAdapter("setTrendingListens")
    @JvmStatic
    fun setTrendingListens(textView: TextView, song: Song?) {
        val songs = MediaItemTree.songs
        Log.e("TRENDING LISTENS", songs.find { it.key == song!!.id.replace("[item]", "") }?.trendingListens.toString())
        if (song != null) {
            val number = songs.find { it.key == song.id.replace("[item]", "") }?.trendingListens?.size

            textView.text = fomartnumber(number ?: 0)
        } else {
            textView.text = ""
        }
    }

    @BindingAdapter("setListOfUidUpVotes")
    @JvmStatic
    fun setListOfUidUpVotes(textView: TextView, song: Song?) {
        val songs = MediaItemTree.songs
        if (song != null) {
            textView.text = fomartnumber(songs.find { it.key == song.id.replace("[item]", "") }?.listOfUidUpVotes?.size ?: 0)
        } else {
            textView.text = ""
        }
    }

    @BindingAdapter("setListOfUidDownVotes")
    @JvmStatic
    fun setListOfUidDownVotes(textView: TextView, song: Song?) {
        val songs = MediaItemTree.songs
        if (song != null) {
            textView.text = fomartnumber(songs.find { it.key == song.id.replace("[item]", "") }?.listOfUidDownVotes?.size ?: 0)
        } else {
            textView.text = ""
        }
    }

    @BindingAdapter("setListens")
    @JvmStatic
    fun setListens(textView: TextView, song: Song?) {
        val songs = MediaItemTree.songs
        if (song != null) {
            textView.text = fomartnumber(songs.find { it.key == song.id.replace("[item]", "") }?.listens?.size ?: 0)
        } else {
            textView.text = ""
        }
    }

    @BindingAdapter("setListens")
    @JvmStatic
    fun setListensByKey(textView: TextView, key: String?) {
        val songs = MediaItemTree.songs
        if (key != null) {
            val number = songs.find { it.key == key.replace("[item]", "") }?.listens?.size ?: 0
            textView.text = fomartnumber(number)
        } else {
            textView.text = ""
        }
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
                MultiTransformation(centerCrop, RoundedCorners(10))
            )
            .placeholder(R.drawable.album_art)
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
            .placeholder(R.drawable.album_art)
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
//        Log.e("URI", uri.toString())
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

    @BindingAdapter("requestSrc")
    @JvmStatic
    fun setRequestSrc(view: ImageView, playlist: RequestPlaylist) {
        val profile = Amplify.DataStore.query(
            Profile::class.java,
            Where.matchesAll(),
            { profilesIterator ->
                profilesIterator.forEach { profile ->
                    playlist.player.owners?.map { owner ->
                        if (profile.owner == owner) {
                            Glide.with(view)
                                .setDefaultRequestOptions(
                                    RequestOptions()
                                        .placeholder(R.drawable.album_art)
                                        .error(R.drawable.album_art)
                                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                                )
                                .load("https://dn1i8z7909ivj.cloudfront.net/public/"+profile.imageKey)
                                .transform(
                                    MultiTransformation(centerCrop, FitCenter(), RoundedCorners(10))
                                )
                                .placeholder(R.drawable.album_art)
                                .into(view)
                        }
                    }
                }
            },
            {
                Glide.with(view)
                    .setDefaultRequestOptions(
                        RequestOptions()
                            .placeholder(R.drawable.album_art)
                            .error(R.drawable.album_art)
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                    )
                    .load(R.drawable.album_art)
                    .transform(
                        MultiTransformation(centerCrop, FitCenter(), RoundedCorners(10))
                    )
                    .placeholder(R.drawable.album_art)
                    .into(view)
            }
        )
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