package ordinary.rahmatbakery.util

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import org.json.JSONObject

data class GeoRoute(
    val distanceKm: Double,
    val durationMinutes: Double
)

fun hitungJarak(
    latUser: Double,
    lngUser: Double,
    callback: (GeoRoute?, String?) -> Unit
) {
    val client = OkHttpClient()

    val url = "https://api.geoapify.com/v1/routing?" +
            "waypoints=$latUser,$lngUser|${AlamatToko.LATITUDE},${AlamatToko.LONGITUDE}" +
            "&mode=drive&apiKey=22780bf3195b489395eb00263abac5b7"

    println("URL: $url")

    val request = Request.Builder()
        .url(url)
        .get()
        .build()

    Thread {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    callback(null, "Gagal: ${response.message}")
                    return@use
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)

                val features = json.getJSONArray("features")
                if (features.length() == 0) {
                    callback(null, "Tidak ada rute")
                    return@use
                }

                val props = features.getJSONObject(0)
                    .getJSONObject("properties")

                val distance = props.getDouble("distance") // meter
                val time = props.getDouble("time")         // detik

                val distanceKm = distance / 1000.0
                val durationMinutes = time / 60.0

                callback(
                    GeoRoute(distanceKm, durationMinutes),
                    null
                )
            }
        } catch (e: Exception) {
            callback(null, e.localizedMessage)
        }
    }.start()
}
