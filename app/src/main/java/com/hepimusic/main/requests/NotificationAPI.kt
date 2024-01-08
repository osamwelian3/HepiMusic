package com.hepimusic.main.requests

import com.hepimusic.common.Constants.ACCESS_TOKEN
import com.hepimusic.common.Constants.CONTENT_TYPE
import com.hepimusic.common.Constants.PROJECT_ID
import com.hepimusic.common.Constants.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationAPI {

    @Headers("Content-Type:$CONTENT_TYPE")
    @POST("/v1/projects/${PROJECT_ID}/messages:send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>

    @GET("/json")
    suspend fun getLocation(): Response<ResponseBody>
}