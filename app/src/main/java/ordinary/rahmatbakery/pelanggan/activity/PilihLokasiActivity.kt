package ordinary.rahmatbakery.pelanggan.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import ordinary.rahmatbakery.R
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.util.Locale

class PilihLokasiActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder
    private lateinit var tvAlamat: TextView

    private var selectedLat: Double? = null
    private var selectedLon: Double? = null
    private var selectedAddress: String = ""
    private var selectedKecamatan: String = ""
    private var selectedKabupaten: String = ""
    private var selectedProvinsi: String = ""

    private val jatimBounds = LatLngBounds.Builder()
        .include(LatLng(-8.8, 111.0))
        .include(LatLng(-6.7, 115.0))
        .build()

    private val jemberCenter = LatLng(-8.1723, 113.6995)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale("id", "ID"))

        setContentView(R.layout.activity_pilih_lokasi)
        enableEdgeToEdge()

        tvAlamat = findViewById(R.id.tvAlamatTerpilih)

        val existingLat = intent.getDoubleExtra("latitude", Double.NaN)
        val existingLon = intent.getDoubleExtra("longitude", Double.NaN)

        mapView = findViewById(R.id.mapView)
        mapView.getMapAsync { map ->
            this.map = map
            loadMapTilerStyle()

            map.setLatLngBoundsForCameraTarget(jatimBounds)
            map.setMinZoomPreference(15.0)
            map.setMaxZoomPreference(20.0)

            if (!existingLat.isNaN() && !existingLon.isNaN()) {
                val point = LatLng(existingLat, existingLon)
                selectedLat = point.latitude
                selectedLon = point.longitude
                map.addMarker(MarkerOptions().position(point))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18.0))
                getAddressFromLocation(point.latitude, point.longitude)
            } else {
                setupLocation()
            }

            setupListeners()
        }

        findViewById<TextView>(R.id.btnSimpanLokasi).setOnClickListener {
            if (selectedLat != null && selectedLon != null) {
                val data = Intent().apply {
                    putExtra("latitude", selectedLat)
                    putExtra("longitude", selectedLon)
                    putExtra("address", selectedAddress)
                    putExtra("kecamatan", selectedKecamatan)
                    putExtra("kabupaten", selectedKabupaten)
                    putExtra("provinsi", selectedProvinsi)
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

    private fun getAddressFromLocation(lat: Double, lon: Double) {
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Ambil komponen alamat
                val jalan = address.thoroughfare ?: ""
                val subLocality = address.subLocality ?: ""
                selectedKecamatan = address.subAdminArea ?: ""
                selectedKabupaten = address.locality ?: address.adminArea ?: ""
                selectedProvinsi = address.adminArea ?: ""

                // Format alamat lengkap
                val addressParts = mutableListOf<String>()
                if (jalan.isNotEmpty()) addressParts.add(jalan)
                if (subLocality.isNotEmpty()) addressParts.add(subLocality)

                selectedAddress = if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(", ")
                } else {
                    "Alamat tidak ditemukan"
                }

                // Tampilkan di TextView
                val fullAddress = buildString {
                    append(selectedAddress)
                    if (selectedKecamatan.isNotEmpty()) append("\n$selectedKecamatan")
                    if (selectedKabupaten.isNotEmpty()) append(", $selectedKabupaten")
                    if (selectedProvinsi.isNotEmpty()) append(", $selectedProvinsi")
                }

                tvAlamat.text = fullAddress
            } else {
                tvAlamat.text = "Alamat tidak ditemukan"
                selectedAddress = ""
            }
        } catch (e: Exception) {
            tvAlamat.text = "Gagal mendapatkan alamat: ${e.message}"
            selectedAddress = ""
        }
    }

    private fun loadMapTilerStyle() {
        val maptilerUrl = "https://tiles.openfreemap.org/styles/liberty"

        try {
            map.setStyle(maptilerUrl) { style ->
                // Map loaded successfully
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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18.0))
                selectedLat = pos.latitude
                selectedLon = pos.longitude

                map.addMarker(MarkerOptions().position(pos))
                getAddressFromLocation(pos.latitude, pos.longitude)
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
            .setNegativeButton("Tidak"){ _, _ ->
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(jemberCenter, 15.0))
            }
            .show()
    }

    private fun setupListeners() {
        map.addOnMapClickListener { point ->
            map.clear()
            map.addMarker(MarkerOptions().position(point))
            selectedLat = point.latitude
            selectedLon = point.longitude
            getAddressFromLocation(point.latitude, point.longitude)
            true
        }
    }

    private fun pusatkanKeLokasi() {
        if (selectedLat != null && selectedLon != null) {
            val pos = LatLng(selectedLat!!, selectedLon!!)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18.0))
        } else {
            setupLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
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