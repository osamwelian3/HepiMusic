package com.hepimusic.models

import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.annotations.Index
import com.amplifyframework.core.model.annotations.ModelConfig
import com.amplifyframework.core.model.annotations.ModelField
import com.amplifyframework.core.model.temporal.Temporal

@ModelConfig(pluralName = "Songs", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = ["key"])
data class Song(
    @ModelField(targetType = "String", isRequired = true) val key: String,
    @ModelField(targetType = "String", isRequired = true) val fileUrl: String,
    @ModelField(targetType = "String", isRequired = true) val fileKey: String,
    @ModelField(targetType = "String") val listens: List<String>? = null,
    @ModelField(targetType = "String") val trendingListens: List<String>? = null,
    @ModelField(targetType = "String") val listOfUidDownVotes: List<String>? = null,
    @ModelField(targetType = "String") val listOfUidUpVotes: List<String>? = null,
    @ModelField(targetType = "String", isRequired = true) val name: String,
    @ModelField(targetType = "String") val partOf: String? = null,
    @ModelField(targetType = "String", isRequired = true) val selectedCategory: String,
    @ModelField(targetType = "String") val selectedCreator: String? = null,
    @ModelField(targetType = "String", isRequired = true) val thumbnail: String,
    @ModelField(targetType = "String", isRequired = true) val thumbnailKey: String,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val createdAt: Temporal.DateTime? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val updatedAt: Temporal.DateTime? = null
) : Model {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val song = other as Song

        return key == song.key &&
                fileUrl == song.fileUrl &&
                fileKey == song.fileKey &&
                listens == song.listens &&
                trendingListens == song.trendingListens &&
                listOfUidDownVotes == song.listOfUidDownVotes &&
                listOfUidUpVotes == song.listOfUidUpVotes &&
                name == song.name &&
                partOf == song.partOf &&
                selectedCategory == song.selectedCategory &&
                selectedCreator == song.selectedCreator &&
                thumbnail == song.thumbnail &&
                thumbnailKey == song.thumbnailKey &&
                createdAt == song.createdAt &&
                updatedAt == song.updatedAt
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + fileUrl.hashCode()
        result = 31 * result + fileKey.hashCode()
        result = 31 * result + (listens?.hashCode() ?: 0)
        result = 31 * result + (trendingListens?.hashCode() ?: 0)
        result = 31 * result + (listOfUidDownVotes?.hashCode() ?: 0)
        result = 31 * result + (listOfUidUpVotes?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (partOf?.hashCode() ?: 0)
        result = 31 * result + selectedCategory.hashCode()
        result = 31 * result + (selectedCreator?.hashCode() ?: 0)
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + thumbnailKey.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Song(" +
                "key='$key', " +
                "fileUrl='$fileUrl', " +
                "fileKey='$fileKey', " +
                "listens=$listens, " +
                "trendingListens=$trendingListens, " +
                "listOfUidDownVotes=$listOfUidDownVotes, " +
                "listOfUidUpVotes=$listOfUidUpVotes, " +
                "name='$name', " +
                "partOf=$partOf, " +
                "selectedCategory='$selectedCategory', " +
                "selectedCreator=$selectedCreator, " +
                "thumbnail='$thumbnail', " +
                "thumbnailKey='$thumbnailKey', " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt" +
                ")"
    }

    override fun resolveIdentifier(): String {
        return key
    }
}