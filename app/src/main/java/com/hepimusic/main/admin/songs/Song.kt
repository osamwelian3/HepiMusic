package com.hepimusic.main.admin.songs

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Song(
    override val id: String?,
    @Contextual
    val originalSong: com.amplifyframework.datastore.generated.model.Song
): Model(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        Gson().fromJson(parcel.readString(), com.amplifyframework.datastore.generated.model.Song::class.java)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Gson().toJson(originalSong))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}