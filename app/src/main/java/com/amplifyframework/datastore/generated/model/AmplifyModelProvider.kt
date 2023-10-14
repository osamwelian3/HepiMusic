package com.amplifyframework.datastore.generated.model

import com.amplifyframework.util.Immutable
import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.ModelProvider
import com.hepimusic.models.Album
import com.hepimusic.models.Category
import com.hepimusic.models.Creator
import com.hepimusic.models.Song
import com.hepimusic.models.User
import java.util.Arrays
import java.util.HashSet
/**
 * Contains the set of model classes that implement [Model]
 * interface.
 */
class AmplifyModelProvider private constructor() : ModelProvider {
    override fun models(): Set<Class<out Model>> {
        val modifiableSet: MutableSet<Class<out Model>> = HashSet(
            Arrays.asList(
                Song::class.java,
                Creator::class.java,
                Category::class.java,
                Album::class.java,
                User::class.java
            )
        )
        return Immutable.of(modifiableSet)
    }

    override fun version(): String {
        return AMPLIFY_MODEL_VERSION
    }

    companion object {
        private const val AMPLIFY_MODEL_VERSION = "985c6f907fc2930d59a0e8f0b7bffede"
        private var amplifyGeneratedModelInstance: AmplifyModelProvider? = null
        @JvmStatic
        fun getInstance(): AmplifyModelProvider {
            if (amplifyGeneratedModelInstance == null) {
                amplifyGeneratedModelInstance = AmplifyModelProvider()
            }
            return amplifyGeneratedModelInstance!!
        }
    }
}
