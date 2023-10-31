package com.hepimusic.main.playlist

import android.app.Application
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.hepimusic.main.songs.Song
import com.hepimusic.main.songs.SongsRepository

class PlaylistSongsRepository(application: Application, browser: MediaBrowser) : SongsRepository(application, browser) {

    override fun transform(cursor: MediaItem): Song {
        return Song(cursor/*, cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID))*/)
    }
}
