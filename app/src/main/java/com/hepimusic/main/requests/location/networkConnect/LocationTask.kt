package com.hepimusic.main.requests.location.networkConnect

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LocationTask(private val listener: LocationListener) : AsyncTask<Void, Void, String>() {

    interface LocationListener {
        fun onLocationReceived(location: String, ip: String, loc: String)
    }

    override fun doInBackground(vararg params: Void?): String? {
        try {
            val url = URL("https://ipinfo.io/json")
            val urlConnection = url.openConnection() as HttpURLConnection

            try {
                val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }

                bufferedReader.close()
                return stringBuilder.toString()
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: IOException) {
            Log.e("LocationTask", "Error retrieving location data", e)
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null) {
            val json = JSONObject(result)
            val location = "${json.optString("city")}, ${json.optString("region")}, ${json.optString("country")}"
            val ip = json.optString("ip")
            val LatLng = json.optString("loc")
            listener.onLocationReceived(location, ip, LatLng)
        } else {
            Log.e("LocationTask", "No result received")
        }
    }
}

