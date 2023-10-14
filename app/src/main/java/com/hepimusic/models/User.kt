package com.hepimusic.models

import com.amplifyframework.core.model.temporal.Temporal

import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.annotations.Index
import com.amplifyframework.core.model.annotations.ModelConfig
import com.amplifyframework.core.model.annotations.ModelField
import com.amplifyframework.core.model.query.predicate.QueryField
import java.util.UUID

@ModelConfig(pluralName = "Users", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = ["id"])
data class User(
    @ModelField(targetType = "ID", isRequired = true) val id: String,
    @ModelField(targetType = "String") val name: String? = null,
    @ModelField(targetType = "String") val email: String? = null,
    @ModelField(targetType = "String") val phoneNumber: String? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val createdAt: Temporal.DateTime? = null,
    @ModelField(targetType = "AWSDateTime", isReadOnly = true) val updatedAt: Temporal.DateTime? = null
) : Model {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val user = other as User

        return id == user.id &&
                name == user.name &&
                email == user.email &&
                phoneNumber == user.phoneNumber &&
                createdAt == user.createdAt &&
                updatedAt == user.updatedAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "User(" +
                "id='$id', " +
                "name='$name', " +
                "email='$email', " +
                "phoneNumber='$phoneNumber', " +
                "createdAt=$createdAt, " +
                "updatedAt=$updatedAt" +
                ")"
    }

    override fun resolveIdentifier(): String {
        return id
    }
}

// Additional helper function to create a User instance with just the ID populated
fun createUserWithId(id: String): User {
    return User(id = id)
}
