package com.hepimusic.main.admin.categories

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.hepimusic.main.common.data.Model
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class Category(
    override val id: String?,
    @Contextual
    val originalCategory: com.amplifyframework.datastore.generated.model.Category
): Model(), Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        Gson().fromJson(parcel.readString(), com.amplifyframework.datastore.generated.model.Category::class.java)
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(Gson().toJson(originalCategory))
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}