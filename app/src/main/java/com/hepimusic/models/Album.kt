package com.hepimusic.models

import com.amplifyframework.core.model.temporal.Temporal

import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.annotations.Index
import com.amplifyframework.core.model.annotations.ModelConfig
import com.amplifyframework.core.model.annotations.ModelField
import com.amplifyframework.core.model.query.predicate.QueryField

@ModelConfig(pluralName = "Albums", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = ["key"])
data class Album(
    @ModelField(targetType = "String", isRequired = true) val key: String,
    @ModelField(targetType = "String", isRequired = true) val name: String,
    @ModelField(targetType = "String") val thumbnail: String? = null,
    @ModelField(targetType = "String") val thumbnailKey: String? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val createdAt: Temporal.DateTime? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val updatedAt: Temporal.DateTime? = null
) : Model {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val album = other as Album

        return key == album.key &&
                name == album.name &&
                thumbnail == album.thumbnail &&
                thumbnailKey == album.thumbnailKey &&
                createdAt == album.createdAt &&
                updatedAt == album.updatedAt
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + (thumbnailKey?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Album(" +
                "key='$key', " +
                "name='$name', " +
                "thumbnail=$thumbnail, " +
                "thumbnailKey=$thumbnailKey, " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt" +
                ")"
    }

    override fun resolveIdentifier(): String {
        return key
    }
}
