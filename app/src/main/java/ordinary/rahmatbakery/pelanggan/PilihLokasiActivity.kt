package ordinary.rahmatbakery.pelanggan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ordinary.rahmatbakery.R
import org.json.JSONArray
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import java.net.URL
import java.net.URLEncoder

class PilihLokasiActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedLat: Double? = null
    private var selectedLon: Double? = null

    private val jatimBounds = LatLngBounds.Builder()
        .include(LatLng(-8.8, 111.0)) // barat daya
        .include(LatLng(-6.7, 115.0)) // timur laut
        .build()

    private val jemberCenter = LatLng(-8.1723, 113.6995)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContentView(R.layout.activity_pilih_lokasi)
        enableEdgeToEdge()

        val existingLat = intent.getDoubleExtra("latitude", Double.NaN)
        val existingLon = intent.getDoubleExtra("longitude", Double.NaN)

        mapView = findViewById(R.id.mapView)
        mapView.getMapAsync { map ->
            this.map = map
            loadMapTilerStyle()

            map.setLatLngBoundsForCameraTarget(jatimBounds)
            map.setMinZoomPreference(10.0)
            map.setMaxZoomPreference(18.0)

            if (!existingLat.isNaN() && !existingLon.isNaN()) {
                val point = LatLng(existingLat, existingLon)
                map.addMarker(org.maplibre.android.annotations.MarkerOptions().position(point))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16.0))
            }

            setupLocation()
            setupListeners()
        }

        findViewById<TextView>(R.id.btnSimpanLokasi).setOnClickListener {
            if (selectedLat != null && selectedLon != null) {
                val data = Intent().apply {
                    putExtra("latitude", selectedLat)
                    putExtra("longitude", selectedLon)
                }
                setResult(RESULT_OK, data)
                finish()
            } else {
                Toast.makeText(this, "Silakan pilih titik di peta dulu", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.my_location).setOnClickListener {
            pusatkanKeLokasi()
        }
    }

    private fun loadMapTilerStyle() {
        val maptilerUrl = "https://api.maptiler.com/maps/hybrid/style.json?key=6IB7xtqOGn7dOvhXeKI7"

        try {
            map.setStyle(maptilerUrl) { style ->
                Toast.makeText(this, "Peta MapTiler berhasil dimuat", Toast.LENGTH_SHORT).show()
                cacheOfflineMap()
            }

            mapView.postDelayed({
                if (!::map.isInitialized || map.style == null) {
                    Toast.makeText(this, "MapTiler gagal dimuat, beralih ke peta gratis (OSM)", Toast.LENGTH_LONG).show()
                    fallbackToOSM()
                }
            }, 3000)

        } catch (e: Exception) {
            Toast.makeText(this, "MapTiler error: ${e.message}. Menggunakan peta OSM.", Toast.LENGTH_LONG).show()
            fallbackToOSM()
        }
    }

    private fun fallbackToOSM() {
        map.setStyle("""
            {
              "version": 8,
              "sources": {
                "osm": {
                  "type": "raster",
                  "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
                  "tileSize": 256
                }
              },
              "layers": [
                {
                  "id": "osm",
                  "type": "raster",
                  "source": "osm"
                }
              ]
            }
        """.trimIndent())
    }

    private fun setupLocation() {
        if (!isLocationEnabled()) {
            showLocationDialog()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val pos = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14.5))
                selectedLat = pos.latitude
                selectedLon = pos.longitude
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(jemberCenter, 15.5))
                selectedLat = jemberCenter.latitude
                selectedLon = jemberCenter.longitude
                Toast.makeText(this, "Lokasi tidak ditemukan, menampilkan Jember", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLocationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Lokasi tidak aktif")
            .setMessage("Fitur lokasi diperlukan agar peta bisa menampilkan posisi Anda. Aktifkan sekarang?")
            .setPositiveButton("Ya") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun setupListeners() {
        map.addOnMapClickListener { point ->
            map.clear()
            map.addMarker(MarkerOptions().position(point))
            selectedLat = point.latitude
            selectedLon = point.longitude
            true
        }
    }

    private fun pusatkanKeLokasi() {
        if (selectedLat != null && selectedLon != null) {
            val pos = LatLng(selectedLat!!, selectedLon!!)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14.0))
        } else {
            Toast.makeText(this, "Belum ada lokasi dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun cacheOfflineMap() {
        val offlineManager = OfflineManager.getInstance(this)

        // area sekitar Jember
        val bounds = LatLngBounds.Builder()
            .include(LatLng(-8.25, 113.60))
            .include(LatLng(-8.10, 113.80))
            .build()

        val definition = OfflineTilePyramidRegionDefinition(
            "https://api.maptiler.com/maps/hybrid/style.json?key=6IB7xtqOGn7dOvhXeKI7",
            bounds,
            10.0, 17.0,  // level zoom yang disimpan
            this.resources.displayMetrics.density,
            true
        )

        val metadata = byteArrayOf() // optional metadata kosong

        offlineManager.createOfflineRegion(definition, metadata,
            object : OfflineManager.CreateOfflineRegionCallback {
                override fun onCreate(region: OfflineRegion) {
                    region.setDownloadState(OfflineRegion.STATE_ACTIVE)
                }

                override fun onError(error: String) {
                    Toast.makeText(this@PilihLokasiActivity, "Gagal membuat cache offline: $error", Toast.LENGTH_LONG).show()
                }

            })
    }


    override fun onStart() {
        super.onStart(); mapView.onStart()
    }

    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }
}