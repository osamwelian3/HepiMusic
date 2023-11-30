package com.hepimusic.models

import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.annotations.Index
import com.amplifyframework.core.model.annotations.ModelConfig
import com.amplifyframework.core.model.annotations.ModelField
import com.amplifyframework.core.model.temporal.Temporal

@ModelConfig(pluralName = "Creators", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = ["key"])
data class Creator(
    @ModelField(targetType = "String", isRequired = true) val key: String,
    @ModelField(targetType = "String") val desc: String? = null,
    @ModelField(targetType = "String") val facebook: String? = null,
    @ModelField(targetType = "String") val instagram: String? = null,
    @ModelField(targetType = "String", isRequired = true) val name: String,
    @ModelField(targetType = "String") val thumbnail: String? = null,
    @ModelField(targetType = "String") val thumbnailKey: String? = null,
    @ModelField(targetType = "String") val twitter: String? = null,
    @ModelField(targetType = "String") val youtube: String? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val createdAt: Temporal.DateTime? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val updatedAt: Temporal.DateTime? = null
) : Model {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val creator = other as Creator

        return key == creator.key &&
                desc == creator.desc &&
                facebook == creator.facebook &&
                instagram == creator.instagram &&
                name == creator.name &&
                thumbnail == creator.thumbnail &&
                thumbnailKey == creator.thumbnailKey &&
                twitter == creator.twitter &&
                youtube == creator.youtube &&
                createdAt == creator.createdAt &&
                updatedAt == creator.updatedAt
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (desc?.hashCode() ?: 0)
        result = 31 * result + (facebook?.hashCode() ?: 0)
        result = 31 * result + (instagram?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + (thumbnailKey?.hashCode() ?: 0)
        result = 31 * result + (twitter?.hashCode() ?: 0)
        result = 31 * result + (youtube?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Creator(" +
                "key='$key', " +
                "desc='$desc', " +
                "facebook='$facebook', " +
                "instagram='$instagram', " +
                "name='$name', " +
                "thumbnail='$thumbnail', " +
                "thumbnailKey='$thumbnailKey', " +
                "twitter='$twitter', " +
                "youtube='$youtube', " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt" +
                ")"
    }

    override fun resolveIdentifier(): String {
        return key
    }
}
