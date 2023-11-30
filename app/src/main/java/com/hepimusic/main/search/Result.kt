package com.hepimusic.main.search

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
data class Result(@StringRes val title: Int, val type: Type?, var hasResults: Boolean = false) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Type::class.java.classLoader),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(title)
        parcel.writeByte(if (hasResults) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Result> {
        override fun createFromParcel(parcel: Parcel): Result {
            return Result(parcel)
        }

        override fun newArray(size: Int): Array<Result?> {
            return arrayOfNulls(size)
        }
    }
}

enum class Type {
    Songs, Albums, Artists, Genres, Playlists
}