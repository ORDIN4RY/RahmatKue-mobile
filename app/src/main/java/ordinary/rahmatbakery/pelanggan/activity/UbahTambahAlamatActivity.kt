package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.Alamat
import ordinary.rahmatbakery.util.AuthRepository
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory

class UbahTambahAlamatActivity(
    private val repo: AuthRepository = AuthRepository()
) : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var cbAlamatUtama: SwitchCompat
    private lateinit var inputNama: EditText
    private lateinit var inputNoHp: EditText
    private lateinit var inputAlamat: EditText
    private lateinit var inputLatitude: EditText
    private lateinit var inputLongitude: EditText
    private lateinit var inputDetail: EditText

    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap

    private val jemberCenter = LatLng(-8.1722, 113.6870)

    private var alamat: Alamat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_ubah_tambah_alamat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        alamat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_ALAMAT", Alamat::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_ALAMAT")
        }

        if (alamat == null) {
            prepareAdd()
        } else {
            prepareEdit()
        }

        findViewById<TextView>(R.id.pilih_alamat).setOnClickListener {
            val intent = Intent(this, PilihLokasiActivity::class.java)
            lokasiResultLauncher.launch(intent)
        }

        mapView = findViewById(R.id.mapPreview)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapLibre ->
            map = mapLibre

            map.setStyle("https://tiles.openfreemap.org/styles/liberty")

            map.uiSettings.isScrollGesturesEnabled = false
            map.uiSettings.isZoomGesturesEnabled = false
            map.uiSettings.isTiltGesturesEnabled = false
            map.uiSettings.isRotateGesturesEnabled = false

            val position = CameraPosition.Builder()
                .target(jemberCenter)
                .zoom(15.0)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position))

            alamat?.let {
                val lat = it.latitude
                val lon = it.longitude
                if (lat != null && lon != null) {
                    val point = LatLng(lat, lon)
                    map.clear()
                    map.addMarker(MarkerOptions().position(point))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16.0))
                }
            }

            map.addOnMapClickListener { latLng ->
                val intent = Intent(this, PilihLokasiActivity::class.java).apply {
                    putExtra("latitude", inputLatitude.text.toString().toDoubleOrNull() ?: Double.NaN)
                    putExtra("longitude", inputLongitude.text.toString().toDoubleOrNull() ?: Double.NaN)
                }
                lokasiResultLauncher.launch(intent)
                true
            }
        }


    }

    private val lokasiResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("latitude", 0.0)
            val lon = result.data?.getDoubleExtra("longitude", 0.0)
            if (lat != null && lon != null) {
                inputLatitude.setText(lat.toString())
                inputLongitude.setText(lon.toString())
                val point = LatLng(lat, lon)
                map.clear()
                map.addMarker(MarkerOptions().position(point))
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 16.0))
            }
        }
    }

    private fun prepareAdd() {
        findViewById<TextView>(R.id.judul_alamat).setText("Tambah Alamat Baru")
        initListenerAdd()
        btnHapus.visibility = Button.GONE
    }

    private fun initListenerAdd() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            addAlamat()
        }
    }

    private fun addAlamat() {
        val nama = inputNama.text.toString().trim()
        val noHp = inputNoHp.text.toString().trim()
        val alamatRumah = inputAlamat.text.toString().trim()
        val isUtama = cbAlamatUtama.isChecked
        val lat = inputLatitude.text.toString().toDoubleOrNull() ?: Double.NaN
        val lon = inputLongitude.text.toString().toDoubleOrNull() ?: Double.NaN
        val det = inputDetail.text.toString().trim()

        if (nama.isEmpty() || noHp.isEmpty() || alamatRumah.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val currentUser = repo.getCurrentProfile()

                if (currentUser == null) {
                    Toast.makeText(
                        this@UbahTambahAlamatActivity,
                        "Gagal memuat sesi pengguna.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                SupabaseManager.client.postgrest.from("alamat").insert(
                    Alamat(
                        idUser = currentUser.id,
                        nama = nama,
                        noHp = noHp,
                        alamat = alamatRumah,
                        isUtama = isUtama,
                        latitude = lat,
                        longitude = lon,
                        detail = det
                    )

                )
                setResult(RESULT_OK)
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Alamat berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Galat: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.back)
        btnSimpan = findViewById(R.id.btn_simpan)
        btnHapus = findViewById(R.id.btn_hapus)
        cbAlamatUtama = findViewById(R.id.cb_alamat_utama)
        inputNama = findViewById(R.id.input_nama)
        inputAlamat = findViewById(R.id.input_alamat)
        inputNoHp = findViewById(R.id.input_no_hp)
        inputDetail = findViewById(R.id.input_patokan)
        inputLatitude = findViewById(R.id.input_latitude)
        inputLongitude = findViewById(R.id.input_longitude)
    }

    private fun initListenerEdit() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            updateAlamat()
        }

        btnHapus.setOnClickListener {
            deleteAlamat()
        }
    }

    private fun prepareEdit() {
        inputNama.setText(alamat?.nama)
        inputNoHp.setText(alamat?.noHp)
        inputAlamat.setText(alamat?.alamat)
        inputLatitude.setText(alamat?.latitude.toString())
        inputLongitude.setText(alamat?.longitude.toString())
        inputDetail.setText(alamat?.detail)
        cbAlamatUtama.isChecked = alamat?.isUtama ?: false
        btnHapus.visibility = Button.VISIBLE

        findViewById<TextView>(R.id.judul_alamat).setText("Perbarui Alamat")
        initListenerEdit()
    }

    private fun updateAlamat() {
        val nama = inputNama.text.toString().trim()
        val noHp = inputNoHp.text.toString().trim()
        val alamatRumah = inputAlamat.text.toString().trim()
        val lat = inputLatitude.text.toString().toDoubleOrNull() ?: Double.NaN
        val lon = inputLongitude.text.toString().toDoubleOrNull() ?: Double.NaN
        val det = inputDetail.text.toString().trim()

        if (nama.isEmpty() || noHp.isEmpty() || alamatRumah.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest.from("alamat").update(
                    {
                        set("nama_lengkap", nama)
                        set("no_hp_penerima", noHp)
                        set("alamat_rumah", alamatRumah)
                        set("alamat_utama", cbAlamatUtama.isChecked)
                        set("latitude", lat)
                        set("longitude", lon)
                        set("detail_lain", det)
                    }
                ) {
                    filter {
                        eq("id_alamat", alamat!!.id!!)
                    }
                }
                setResult(RESULT_OK)
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Alamat berhasil diperbarui",
                    Toast.LENGTH_SHORT
                ).show()
                finish() // Tutup activity setelah berhasil
            } catch (e: Exception) {
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Gagal memperbarui: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun deleteAlamat() {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest.from("alamat").delete {
                    filter {
                        eq("id_alamat", alamat!!.id!!)
                    }
                }
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Alamat berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Gagal menghapus: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}