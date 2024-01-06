package com.custom.amplify

import androidx.room.withTransaction
import com.amplifyframework.api.ApiException
import com.amplifyframework.api.graphql.GraphQLOperation
import com.amplifyframework.api.graphql.GraphQLRequest
import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.api.graphql.PaginatedResult
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.Model
import com.amplifyframework.datastore.generated.model.Category
import com.amplifyframework.datastore.generated.model.RequestSong
import com.amplifyframework.datastore.generated.model.Song
import com.custom.amplify.data.SongEntity
import com.custom.amplify.database.MyDatastoreDatabase
import com.custom.amplify.mappers.toSongEntity
import com.hepimusic.models.mappers.toSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
class DataStoreCategory(
    val database: MyDatastoreDatabase
) {
    val dao = database.datastoreDao()

    fun <R> query(
        graphQlRequest: GraphQLRequest<R>,
        onResponse: (GraphQLResponse<R>) -> Unit,
        onFailure: (ApiException) -> Unit
    ): GraphQLOperation<R>? {
        val operation = Amplify.API.query(
            graphQlRequest,
            {
                it.data
                onResponse.invoke(it)
            },
            {
                onFailure.invoke(it)
            }
        )

        return operation
    }

    fun <R> querySong(graphQlRequest: GraphQLRequest<R>, onResponse: (data: Any?) -> Unit, onFailure: (ApiException) -> Unit) {
        Amplify.API.query(
            graphQlRequest,
            { graphQlResponse ->
                if (!graphQlResponse.hasErrors() && graphQlResponse.hasData()) {
                    if (graphQlRequest.responseType.typeName == "com.amplifyframework.api.graphql.PaginatedResult<com.amplifyframework.datastore.generated.model.RequestSong>") {
                        CoroutineScope(Dispatchers.IO).launch {
                            val songs = graphQlResponse.data as PaginatedResult<Song>
                            val fetchedData =
                            database.withTransaction {
                                dao.upsertAllSongs(songs = songs.map { it.toSongEntity() })
                            }

                            onResponse.invoke(songs.map { it.toSong() })
                        }
                    }

                    if (graphQlRequest.responseType.typeName == "com.amplifyframework.datastore.generated.model.RequestSong") {

                    }
                }
                if (graphQlResponse.hasErrors()) {
                    for (error in graphQlResponse.errors) {
                        onFailure.invoke(ApiException(error.message, error.message))
                    }
                }
            },
            {
                onFailure.invoke(it)
            }
        )
    }
}