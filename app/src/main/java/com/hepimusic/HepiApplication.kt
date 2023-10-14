package com.hepimusic

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.session.MediaBrowser
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.hepimusic.models.Song
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HepiApplication: Application() {

    override fun onCreate() {
        super.onCreate()
//        MediaBrowserManager.initialize(this)
//        MediaBrowserManager.run(this, this)
        try {
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")

            Amplify.API.query(
                ModelQuery.list(Song::class.java),
                { response ->
                    Log.i("MyAmplifyApp", response.data.items.count().toString())
                    for (song in response.data) {
                        Log.i("MyAmplifyApp", song.name)
                    }
                },
                { error -> Log.e("MyAmplifyApp", "Query failure", error) }
            )

            /*Amplify.DataStore.query(Song::class.java, {
                Log.i("MyAmplifyApp has next true", it.hasNext().toString())
                it.forEachRemaining {song ->
                    Log.i("MyAmplifyApp Datastore", song.name)
                }
            },
                {error -> Log.e("MyAmplifyApp", "Query failure", error)})*/
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
    }

}

interface BrowserConnectionCallBack {
    fun onMediaBrowserConnected(browser: MediaBrowser)
}