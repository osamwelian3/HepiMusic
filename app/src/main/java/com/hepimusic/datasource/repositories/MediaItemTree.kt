package com.hepimusic.datasource.repositories

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MediaMetadata
import com.google.common.collect.ImmutableList
import com.hepimusic.common.Constants.INITIALIZATION_COMPLETE
import com.hepimusic.common.Resource
import com.hepimusic.datasource.local.entities.AlbumEntity
import com.hepimusic.datasource.local.entities.SongEntity
import com.hepimusic.models.Album
import com.hepimusic.models.Song
import com.hepimusic.models.mappers.toAlbum
import com.hepimusic.models.mappers.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * A sample media catalog that represents media items as a tree.
 *
 * It fetched the data from {@code catalog.json}. The root's children are folders containing media
 * items from the same album/artist/genre.
 *
 * Each app should have their own way of representing the tree. MediaItemTree is used for
 * demonstration purpose only.
 */
object MediaItemTree {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var titleMap: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var isInitialized = false
    private const val ROOT_ID = "[rootID]"
    private const val LATEST_ID = "[latestID]"
    private const val ALBUM_ID = "[albumID]"
    private const val GENRE_ID = "[genreID]"
    private const val ARTIST_ID = "[artistID]"
    private const val CATEGORY_ID = "[categoryID]"
    private const val ALL_SONGS_ID = "[allSongsID]"
    private const val ALBUM_PREFIX = "[album]"
    private const val GENRE_PREFIX = "[genre]"
    private const val ARTIST_PREFIX = "[artist]"
    private const val CATEGORY_PREFIX = "[category]"
    private const val LATEST_PREFIX = "[latest]"
    private const val ITEM_PREFIX = "[item]"
    private const val ALL_SONGS_PREFIX = "[allSongs]"

    private class MediaItemNode(val item: MediaItem) {
        private val children: MutableList<MediaItem> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(treeNodes[childID]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        mediaType: @MediaMetadata.MediaType Int,
        subtitleConfigurations: List<SubtitleConfiguration> = mutableListOf(),
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null,
        year: Int? = null,
        month: Int? = null,
        day: Int? = null,
        trackCount: Int? = null
    ): MediaItem {
        val metadata =
            MediaMetadata.Builder()
                .setAlbumTitle(album)
                .setTitle(title)
                .setArtist(artist)
                .setGenre(genre)
                .setIsBrowsable(isBrowsable)
                .setIsPlayable(isPlayable)
                .setArtworkUri(imageUri)
                .setMediaType(mediaType)
                .setReleaseYear(year)
                .setReleaseMonth(month)
                .setReleaseDay(day)
                .setTotalTrackCount(trackCount)
                .build()

        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setSubtitleConfigurations(subtitleConfigurations)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
    }

    suspend fun initialize(context: Context, songRepository: SongRepository) = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        isInitialized = true
        // create root and folders for album/artist/genre.
        treeNodes[ROOT_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Root Folder",
                    mediaId = ROOT_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
                )
            )
        treeNodes[LATEST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Latest Folder",
                    mediaId = LATEST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS
                )
            )
        treeNodes[ALL_SONGS_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "All Songs Folder",
                    mediaId = ALL_SONGS_ID,
                    isPlayable = true,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
                )
            )
        treeNodes[ALBUM_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Album Folder",
                    mediaId = ALBUM_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS
                )
            )
        treeNodes[ARTIST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Artist Folder",
                    mediaId = ARTIST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS
                )
            )
        treeNodes[GENRE_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Genre Folder",
                    mediaId = GENRE_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_GENRES
                )
            )
        treeNodes[CATEGORY_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Category Folder",
                    mediaId = CATEGORY_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS
                )
            )
        treeNodes[ROOT_ID]!!.addChild(ALBUM_ID)
        treeNodes[ROOT_ID]!!.addChild(ARTIST_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)
        treeNodes[ROOT_ID]!!.addChild(CATEGORY_ID)
        treeNodes[ROOT_ID]!!.addChild(LATEST_ID)
        treeNodes[ROOT_ID]!!.addChild(ALL_SONGS_ID)

        val songs: MutableList<Song> = mutableListOf()
        val albums: MutableList<Album> = mutableListOf()

        val albumsFlow = songRepository.getAlbums()
        val songsFlow = songRepository.getSongs()

        albumsFlow.combine(songsFlow){ albumsResource, songsResource ->
            if (albumsResource is Resource.Success && songsResource is Resource.Success) {
                return@combine MusicData(songsResource.data, albumsResource.data)
            }
            return@combine null
        }.collect { musicData ->
            if (musicData != null){
                musicData.albums?.map {
                    albums.add(it.toAlbum())
                }
                musicData.songs?.map {
                    Log.e("MEDIA ITEM TREE", "ALBUM SIZE: "+albums.size)
                    songs.add(it.toSong())
                    addNodeToTree(it.toSong(), albums)
                }
                context.applicationContext.getSharedPreferences("main", Context.MODE_PRIVATE).edit().putBoolean(INITIALIZATION_COMPLETE, true).apply()
                Log.e("PREFERENCES ADDED", "TRUE")
            }
        }

        context.applicationContext.getSharedPreferences("main", Context.MODE_PRIVATE).edit().putBoolean(INITIALIZATION_COMPLETE, true).apply()
        Log.e("PREFERENCES ADDED", "TRUE")

        /*songRepository.getAlbums().collect { listAlbumResource ->
            listAlbumResource.data?.map {albumEntity ->
                albums.add(albumEntity.toAlbum())
                Log.e("ALBUM FETCHED", albumEntity.name)
            }
            songRepository.getSongs().collect { listResource ->
                if (listResource.error != null) {
                    Toast.makeText(context, listResource.error.message, Toast.LENGTH_LONG).show()
                    AlertDialog.Builder(context)
                        .setTitle("Database Error")
                        .setMessage(listResource.error.message)
                        .setCancelable(true)
                        .setNegativeButton("Close"){ dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        .show()
                }
                if (listResource.data == null) {
                    Toast.makeText(context, "empty song list", Toast.LENGTH_LONG).show()
                    AlertDialog.Builder(context)
                        .setTitle("Database Error")
                        .setMessage("Empty Song List")
                        .setCancelable(true)
                        .setNegativeButton("Close"){ dialogInterface, _ ->
                            dialogInterface.cancel()
                        }
                        .show()
                }
                listResource.data?.map {
                    Log.e("MediaItemTree", "initialize: "+it.name, )
                    songs.add(it.toSong())
                    addNodeToTree(it.toSong(), albums)
                }
            }
        }*/

    }

    private suspend fun addNodeToTree(song: Song, albums: List<Album>) = withContext(Dispatchers.IO) {

        val id = song.key
        val albm = song.partOf.let {
            albums.find { album -> album.key == it  }?.name
        }
        val albmArt = Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/"+song.partOf.let { albums.find { album -> album.key == it  }?.thumbnailKey })
        val album = albm ?: "Unknown Album"
        val title = song.name
        val creator = song.selectedCreator?.let {
            Json.parseToJsonElement(it).jsonObject["name"]
        }
        Log.e("Selected Creator", "selectedCreator: $creator")
        val artist = creator?.toString()?.removeSurrounding("\"") ?: "Unknown Artist"
        val genre = song.selectedCategory
        val subtitleConfigurations: MutableList<SubtitleConfiguration> = mutableListOf()

        val sourceUri = Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/"+song.fileKey)
        Log.e("ALBUM", album)
        Log.e("SONG URI", "https://dn1i8z7909ivj.cloudfront.net/public/"+song.thumbnailKey)
        val imageUri = Uri.parse("https://dn1i8z7909ivj.cloudfront.net/public/"+song.thumbnailKey)

        // key of such items in tree
        val allSongsIdInTree = ALL_SONGS_PREFIX
        val idInTree = ITEM_PREFIX + id
        val albumFolderIdInTree = ALBUM_PREFIX + album
        val artistFolderIdInTree = ARTIST_PREFIX + artist
        val genreFolderIdInTree = GENRE_PREFIX + genre
        val categoryFolderIdInTree = CATEGORY_PREFIX + genre
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.timeZone = TimeZone.getDefault() // Use the device's default timezone

        treeNodes[idInTree] =
            MediaItemNode(
                buildMediaItem(
                    title = title,
                    mediaId = idInTree,
                    isPlayable = true,
                    isBrowsable = false,
                    mediaType = MediaMetadata.MEDIA_TYPE_MUSIC,
                    subtitleConfigurations,
                    album = album,
                    artist = artist,
                    genre = genre,
                    sourceUri = sourceUri,
                    imageUri = imageUri,
                    year = song.createdAt?.toDate()?.year,
                    month = song.createdAt?.toDate()?.month,
                    day = song.createdAt?.toDate()?.day,
                    trackCount = null
                )
            )

        titleMap[title.lowercase()] = treeNodes[idInTree]!!

        if (!treeNodes.containsKey(albumFolderIdInTree)) {
            treeNodes[albumFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = album,
                        mediaId = albumFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_ALBUM,
                        subtitleConfigurations,
                        imageUri = albmArt,
                        trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                    )
                )
            treeNodes[ALBUM_ID]!!.addChild(albumFolderIdInTree)
        }
        treeNodes[albumFolderIdInTree]!!.addChild(idInTree)

        // add into all songs folder
        if (!treeNodes.containsKey(ALL_SONGS_ID)) {
            treeNodes[ALL_SONGS_ID] =
                MediaItemNode(
                    buildMediaItem(
                        title = "All Songs Folder",
                        mediaId = ALL_SONGS_ID,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
                        subtitleConfigurations,
                        trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                    )
                )
        }
        treeNodes[ALL_SONGS_ID]!!.addChild(idInTree)

        // add into artist folder
        if (!treeNodes.containsKey(artistFolderIdInTree)) {
            treeNodes[artistFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = artist,
                        mediaId = artistFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_ARTIST,
                        subtitleConfigurations,
                        trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                    )
                )
            treeNodes[ARTIST_ID]!!.addChild(artistFolderIdInTree)
        }
        treeNodes[artistFolderIdInTree]!!.addChild(idInTree)

        // add into genre folder
        if (!treeNodes.containsKey(genreFolderIdInTree)) {
            treeNodes[genreFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = genre,
                        mediaId = genreFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_GENRE,
                        subtitleConfigurations,
                        trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                    )
                )
            treeNodes[GENRE_ID]!!.addChild(genreFolderIdInTree)
        }
        treeNodes[genreFolderIdInTree]!!.addChild(idInTree)

        // add into category folder
        if (!treeNodes.containsKey(categoryFolderIdInTree)) {
            treeNodes[categoryFolderIdInTree] =
                MediaItemNode(
                    buildMediaItem(
                        title = genre,
                        mediaId = categoryFolderIdInTree,
                        isPlayable = true,
                        isBrowsable = true,
                        mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
                        subtitleConfigurations,
                        trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                    )
                )
            treeNodes[CATEGORY_ID]!!.addChild(categoryFolderIdInTree)
        }
        treeNodes[categoryFolderIdInTree]!!.addChild(idInTree)

        val date: Date? = song.createdAt?.toDate()

        // add into latest folder
        if (date != null) {
            if (!date.before(Date(Date().time - 3 * 24 * 60 * 60 * 1000))!!) {
                val latestFolderIdInTree = LATEST_PREFIX + "songs"

                if (!treeNodes.containsKey(latestFolderIdInTree)) {
                    treeNodes[latestFolderIdInTree] =
                        MediaItemNode(
                            buildMediaItem(
                                title = "Latest",
                                mediaId = latestFolderIdInTree,
                                isPlayable = true,
                                isBrowsable = true,
                                mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST,
                                subtitleConfigurations,
                                trackCount = treeNodes[albumFolderIdInTree]?.getChildren()?.size
                            )
                        )
                    treeNodes[LATEST_ID]!!.addChild(latestFolderIdInTree)
                }
                treeNodes[latestFolderIdInTree]!!.addChild(idInTree)
            }
        }

    }

    fun getItem(id: String): MediaItem? {
        return treeNodes[id]?.item
    }

    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    fun getChildren(id: String): List<MediaItem>? {
        return treeNodes[id]?.getChildren()
    }

    fun getChildrenFlow(id: String?): Flow<List<MediaItem>?> {
        return flowOf(treeNodes[id]?.getChildren())
    }

    fun getRandomItem(): MediaItem {
        var curRoot = getRootItem()
        while (curRoot.mediaMetadata.isBrowsable == true) {
            val children = getChildren(curRoot.mediaId)!!
            curRoot = children.random()
        }
        return curRoot
    }

    fun getItemFromTitle(title: String): MediaItem? {
        return titleMap[title]?.item
    }

}

data class MusicData(
    val songs: List<SongEntity>?,
    val albums: List<AlbumEntity>?
)


