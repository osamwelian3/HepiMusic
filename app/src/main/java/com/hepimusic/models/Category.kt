package com.hepimusic.models

import com.amplifyframework.core.model.temporal.Temporal

import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.annotations.Index
import com.amplifyframework.core.model.annotations.ModelConfig
import com.amplifyframework.core.model.annotations.ModelField
import com.amplifyframework.core.model.query.predicate.QueryField

@ModelConfig(pluralName = "Categories", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = ["key"])
data class Category(
    @ModelField(targetType = "String", isRequired = true) val key: String,
    @ModelField(targetType = "String", isRequired = true) val name: String,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val createdAt: Temporal.DateTime? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val updatedAt: Temporal.DateTime? = null
) : Model {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val category = other as Category

        return key == category.key &&
                name == category.name &&
                createdAt == category.createdAt &&
                updatedAt == category.updatedAt
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Category(" +
                "key='$key', " +
                "name='$name', " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt" +
                ")"
    }

    override fun resolveIdentifier(): String {
        return key
    }
}
