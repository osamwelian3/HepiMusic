package com.hepimusic.main.profile

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Profile(
    /*val key: String,
    val owner: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val imageKey: String?,
    val followers: List<String?>?,
    val follows: List<String?>?,*/
    @Contextual
    var originalProfile: com.amplifyframework.datastore.generated.model.Profile
): Parcelable {
    constructor(parcel: Parcel) : this(
        Gson().fromJson(parcel.readString(), com.amplifyframework.datastore.generated.model.Profile::class.java)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Gson().toJson(originalProfile))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Profile> {
        override fun createFromParcel(parcel: Parcel): Profile {
            return Profile(parcel)
        }

        override fun newArray(size: Int): Array<Profile?> {
            return arrayOfNulls(size)
        }
    }

}