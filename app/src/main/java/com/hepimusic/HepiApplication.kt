package com.hepimusic

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.aws.AuthModeStrategyType
import com.amplifyframework.auth.AuthChannelEventName
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.InitializationStatus
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.DataStoreChannelEventName
import com.amplifyframework.datastore.events.NetworkStatusEvent
import com.amplifyframework.datastore.generated.model.AmplifyModelProvider
import com.amplifyframework.datastore.generated.model.Song
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.hepimusic.common.Constants
import com.hepimusic.common.Resource
import com.hepimusic.datasource.repositories.MediaItemTree
import com.hepimusic.datasource.repositories.MusicData
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HepiApplication: Application() {

    @Inject lateinit var application: Application
    private lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        preferences = this.getSharedPreferences("main", Context.MODE_PRIVATE)
        preferences.edit().putBoolean(Constants.DATASTORE_READY, false).apply()
        preferences.edit().putBoolean(Constants.INITIALIZATION_COMPLETE, false).apply()
        var initialConnectionEstablished = false
        try {
            /*Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))*/
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            val modelProvider = AmplifyModelProvider.getInstance()
            Amplify.addPlugin(AWSDataStorePlugin.builder().modelProvider(modelProvider).authModeStrategy(AuthModeStrategyType.MULTIAUTH).build())
            Amplify.addPlugin(AWSApiPlugin.builder().build())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")
            Amplify.Hub.subscribe(
                HubChannel.AUTH
            ) { event ->
                when (event.name) {
                    InitializationStatus.SUCCEEDED.name -> Log.i("AUTH HUB", "Auth Succesfully Initialized")
                    InitializationStatus.FAILED.name -> Log.i("AUTH HUB", "Auth Initialization failed")
                    else -> when (event.name) {
                        AuthChannelEventName.SIGNED_IN.name -> {
                            Log.i("AUTH HUB", "Auth sign in success")
                            Amplify.Auth.fetchUserAttributes(
                                { authUserAttrs ->
                                    if (preferences.getBoolean(Constants.AUTH_TYPE_SOCIAL, false)) {
                                        authUserAttrs.forEach {
                                            Log.i("AUTH HUB AUTH USER ATTRIBUTE", "KEY: ${it.key.keyString} VALUE: ${it.value}")
                                            if (it.key.keyString.equals("email")) {
                                                preferences.edit().putString(Constants.USERNAME, it.value).apply()
                                            }
                                        }
                                    }
                                },
                                {
                                    Log.i("FETCH USER ATTRIBUTES EXCEPTION", it.message.toString())
                                }
                            )
                        }
                        AuthChannelEventName.SIGNED_OUT.name -> {
                            Log.i("AUTH HUB", "Auth sign out event success")
                        }
                    }
                }

            }
            Amplify.Hub.subscribe(
                HubChannel.DATASTORE,
                {
                    it.name.equals(DataStoreChannelEventName.NETWORK_STATUS.toString())
                },
                {
                    val networkStatus = it.data as NetworkStatusEvent
                    MediaItemTree.initialConnectionEstablished = networkStatus.active
                    CoroutineScope(Dispatchers.Main).launch {
                        if (initialConnectionEstablished) {
                            Toast.makeText(
                                applicationContext,
                                "Network connection ${if (networkStatus.active) "restored" else "lost"}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e(
                                "Amplify Network Status",
                                "Network connection ${if (networkStatus.active) "restored" else "lost"}"
                            )
                        } else {
                            initialConnectionEstablished = true
                            if (!networkStatus.active) {
                                Toast.makeText(
                                    applicationContext,
                                    "Network connection ${if (networkStatus.active) "restored" else "lost"}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e(
                                    "Amplify Network Status",
                                    "Network connection ${if (networkStatus.active) "restored" else "lost"}"
                                )
                            }
                        }
                    }
                }
            )

            /*Amplify.API.query(
                ModelQuery.list(Song::class.java),
                { response ->
                    Log.i("MyAmplifyApp", response.data.items.count().toString())
                    for (song in response.data) {
                        Log.i("MyAmplifyApp", song.name)
                    }
                },
                { error -> Log.e("MyAmplifyApp", "Query failure", error) }
            )*/

            try {

                Amplify.DataStore.query(
                    Song::class.java,
                    { items ->
                        var count = 0
                        while (items.hasNext()) {
                            val item = items.next()
    //                            Log.i("Amplify", "Queried item: " + item.name)
                            count++
                        }
                        Log.i("Amplify", "Total items: $count")
                    },
                    { failure -> Log.e("Amplify", "Could not query DataStore", failure) }
                )

                /*Amplify.DataStore.observeQuery(
                    Song::class.java,
                    ObserveQueryOptions(),
                    {
                        Log.e("Amplify Datastore Cancelable", it.toString())
                    },
                    {
                        Log.e("Amplify ITEM CHANGED", it.items.size.toString())
                    },
                    { Log.e("Amplify DataStore Exception", it.message.toString()) },
                    { Log.e("Amplify COMPLETED", "OBSERVATION COMPLETE") }
                )*/
            } catch (e: Exception) {
                Log.e("Amplify DATASTORE EXCEPTION", e.message.toString())
            }

        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
    }

}