package com.hepimusic.common

object Constants {
    const val LOGGED_IN = "com.hepimusic.playback.LOGGED_IN"
    val SESSION_ID: String = "com.hepimusic.playback.SESSION_ID"
    const val NOTIFICATION_ID = 1
    const val NOTIFICATION_CHANNEL_ID = "music"

    // Media playback
    const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
    const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    const val CONTENT_STYLE_LIST = 1
    const val CONTENT_STYLE_GRID = 2
    const val BROWSABLE_ROOT = "[rootID]" // "/"
    const val EMPTY_ROOT = "@empty@"
    const val ALBUMS_ROOT = "[albumID]" // "__ALBUMS__"
    const val ARTISTS_ROOT = "[artistID]" // "_ARTISTS__"
    const val SONGS_ROOT = "[allSongsID]" // "_SONGS__"
    const val NETWORK_FAILURE = "com.hepimusic.playback.NETWORK_FAILURE"
    const val LAST_SHUFFLE_MODE = "com.hepimusic.playback.LAST_SHUFFLE_MODE"
    const val LAST_REPEAT_MODE = "com.hepimusic.playback.LAST_REPEAT_MODE"
    const val LAST_ID = "com.hepimusic.playback.LAST_ID"
    const val LAST_POSITION = "com.hepimusic.playback.LAST_POSITION"
    const val LAST_PARENT_ID = "com.hepimusic.playback.LAST_PARENT_ID"
    const val CURRENT_ALBUM_ID = "com.hepimusic.playback.CURRENT_ALBUM_ID"
    const val PLAY_FIRST = "com.hepimusic.playback.PLAY_FIRST"
    const val PLAY_RANDOM = "com.hepimusic.playback.PLAY_RANDOM"
    const val PLAYBACK_NOTIFICATION: Int = 0xb2017


    // Keys for items in NavigationDialogFragment
    const val NAV_SONGS = "com.hepimusic.nav.songs"
    const val NAV_IDENTIFY = "com.hepimusic.nav.identify"
    const val NAV_ARTISTS = "com.hepimusic.nav.artists"
    const val NAV_FAVOURITES = "com.hepimusic.nav.favourites"
    const val NAV_GENRES = "com.hepimusic.nav.genres"
    const val NAV_PLAYLIST = "com.hepimusic.nav.playlist"
    const val NAV_RADIO = "com.hepimusic.nav.radio"
    const val NAV_SETTINGS = "com.hepimusic.nav.settings"
    const val NAV_PROFILE = "com.hepimusic.nav.profile"
    const val NAV_VIDEOS = "com.hepimusic.nav.videos"
    const val NAV_CREATORS_DASHBOARD = "com.hepimusic.nav.admin"
    const val LOGOUT = "com.hepimusic.logout"


    // Keys for items in AdminDashboardFragment
    const val ADMIN_NAV_SONGS = "com.hepimusic.admin.nav.songs"
    const val ADMIN_NAV_ALBUMS = "com.hepimusic.admin.nav.albums"
    const val ADMIN_NAV_CATEGORIES = "com.hepimusic.admin.nav.categories"
    const val ADMIN_NAV_CREATORS = "com.hepimusic.admin.nav.creators"

    // Auth Constants
    const val USERNAME: String = "com.hepimusic.auth.USERNAME"
    const val USER_EMAIL: String = "com.hepimusic.auth.USER.EMAIL"
    const val USER_PHONE: String = "com.hepimusic.auth.USER.PHONE"
    const val USER_PASSWORD: String = "com.hepimusic.auth.USER.PASSWORD"
    const val AUTH_TYPE_SOCIAL = "com.hepimusic.auth.AUTH_TYPE_SOCIAL"
    const val AUTH_USER_ATTRIBUTES = "com.hepimusic.auth.AUTH_USER_ATTRIBUTES"
    const val AUTH_USER = "com.hepimusic.auth.AUTH_USER"

    // Other constants
    const val MAX_RECENTLY_PLAYED = 50
    const val BASE_URL = "https://dn1i8z7909ivj.cloudfront.net/public/"
    const val INITIALIZATION_COMPLETE = "com.hepimusic.INITIALIZATION_COMPLETE"
    const val DATASTORE_READY = "com.hepimusic.DATASTORE_READY"
    const val MAX_MODEL_IMAGE_THUMB_WIDTH = 100
    const val REQUEST_CHECK_SETTINGS = 0

    // Push Notification (FCM)
    const val FCM_BASE_URL = "https://fcm.googleapis.com"
    const val SERVER_KEY = "cd2795e29b0817ca176fdf629fe7b0dd758c95ea"
    const val CONTENT_TYPE = "application/json"
    const val PROJECT_ID = "storage-urli"
    var ACCESS_TOKEN = ""

}