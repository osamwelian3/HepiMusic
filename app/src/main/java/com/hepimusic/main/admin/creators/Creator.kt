package com.hepimusic.main.admin.creators

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Creator(
    override val id: String?,
    @Contextual
    val originalCreator: com.amplifyframework.datastore.generated.model.Creator
): Model(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        Gson().fromJson(parcel.readString(), com.amplifyframework.datastore.generated.model.Creator::class.java)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(Gson().toJson(originalCreator))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Creator> {
        override fun createFromParcel(parcel: Parcel): Creator {
            return Creator(parcel)
        }

        override fun newArray(size: Int): Array<Creator?> {
            return arrayOfNulls(size)
        }
    }
}