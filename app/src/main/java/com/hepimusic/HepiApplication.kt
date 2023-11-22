package com.hepimusic

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.plugins.core.data.AWSCognitoIdentityPoolConfiguration
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.core.category.CategoryConfiguration
import com.amplifyframework.core.category.CategoryType
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.ObserveQueryOptions
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.generated.model.AmplifyModelProvider
import com.amplifyframework.datastore.generated.model.Song
import com.amplifyframework.logging.AndroidLoggingPlugin
import com.amplifyframework.logging.LogLevel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltAndroidApp
class HepiApplication: Application() {

    @Inject lateinit var application: Application

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        try {
            /*Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))*/
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            val modelProvider = AmplifyModelProvider.getInstance()
            Amplify.addPlugin(AWSDataStorePlugin.builder().modelProvider(modelProvider).build())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
            /*val resID = R.raw.amplifyconfiguration
            val inputStream = applicationContext.resources.openRawResource(resID)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val configuration = AmplifyConfiguration.fromJson(jsonObject)
            Amplify.configure(configuration, applicationContext)*/
            Log.i("MyAmplifyApp", "Initialized Amplify")
            Amplify.Auth.getPlugin(Amplify.Auth.plugins.joinToString { it.pluginKey }).initialize(applicationContext)

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