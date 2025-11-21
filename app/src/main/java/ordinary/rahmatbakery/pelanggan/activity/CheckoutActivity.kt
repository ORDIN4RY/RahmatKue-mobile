package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.json.Json
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.PesananCheckoutAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang

class CheckoutActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnCheckout: Button
    private lateinit var cbDp: CheckBox
    private lateinit var cbLunas: CheckBox
    private lateinit var btnPesan: Button
    private lateinit var rvPesanan: RecyclerView
    private lateinit var btnAntar: CheckBox
    private lateinit var btnAmbil: CheckBox

    // sementara, bisa diganti
    private var voucherId = ""
    private var alamatId = ""
    private var metodePembayaran = ""
    private var metodePengiriman = ""
    private var tanggal = ""
    private var waktu = ""
    private var total = 0.0
    private var subtotalPesanan = 0.0
    private var subtotalPengiriman = 0.0
    private var potonganHarga = 0.0
    private var totalPembayaran = 0.0

    private var keranjangTerpilih: List<Keranjang> = emptyList() // Gunakan List, lebih fleksibel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)

        initializeViews()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val selectedItemsJson = intent.getStringExtra("KERANJANG_JSON")

        if (selectedItemsJson != null) {
            try {
                // Ubah kembali String JSON menjadi List<Keranjang>
                keranjangTerpilih = Json.decodeFromString<List<Keranjang>>(selectedItemsJson)
            } catch (e: Exception) {
                // Tangani jika terjadi error saat parsing JSON
                Toast.makeText(this, "Error memproses data keranjang", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }

        if (keranjangTerpilih.isEmpty()) {
            Toast.makeText(this, "Tidak ada item untuk di-checkout", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()

    }

    private fun initializeViews() {
        // --- BINDING SEMUA VIEW ---
        rvPesanan = findViewById(R.id.rvPesanan)
        btnBack = findViewById(R.id.back)
        btnCheckout = findViewById(R.id.btn_checkout)
        cbDp = findViewById(R.id.checkbox_dp)
        cbLunas = findViewById(R.id.checkbox_lunas)
        btnPesan = findViewById(R.id.btn_pesan)
        btnAntar = findViewById(R.id.cb_antar)
        btnAmbil = findViewById(R.id.cb_ambil)

        // --- SETUP LISTENER YANG TIDAK BERGANTUNG PADA DATA ---
        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        // Fungsi ini sekarang hanya bertanggung jawab untuk hal-hal yang butuh 'keranjangTerpilih'
        rvPesanan.layoutManager = LinearLayoutManager(this)
        val pesananAdapter = PesananCheckoutAdapter(keranjangTerpilih)
        rvPesanan.adapter = pesananAdapter
    }
}