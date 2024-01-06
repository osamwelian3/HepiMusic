package com.hepimusic.main.requests

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.hepimusic.R
import com.hepimusic.common.Constants.BASE_URL
import com.hepimusic.common.Constants.FCM_BASE_URL
import com.hepimusic.common.Constants.SERVER_KEY
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class RetrofitInstance() {

    companion object {
        var token = ""

        private val client by lazy {
            OkHttpClient.Builder()
                .addInterceptor(Interceptor { chain ->
                    chain.run {
                        proceed(
                            request()
                                .newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                        )
                    }
                })
                .build()
        }

        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(FCM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        val api by lazy {
            retrofit.create(NotificationAPI::class.java)
        }
    }
}