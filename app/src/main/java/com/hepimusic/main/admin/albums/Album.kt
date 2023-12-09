package com.hepimusic.main.admin.albums

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Album(
    override val id: String?,
    @Contextual
    val originalAlbum: com.amplifyframework.datastore.generated.model.Album
): Model(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        Gson().fromJson(parcel.readString(), com.amplifyframework.datastore.generated.model.Album::class.java)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Gson().toJson(originalAlbum))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }
}