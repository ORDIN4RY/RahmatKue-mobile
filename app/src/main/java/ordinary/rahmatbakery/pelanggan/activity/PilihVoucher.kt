package ordinary.rahmatbakery.pelanggan.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.VoucherAdapter
import ordinary.rahmatbakery.pelanggan.model.Voucher

class PilihVoucher : AppCompatActivity() {

    private lateinit var rvVoucher: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var voucherAdapter: VoucherAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    // Data yang diterima dari CheckoutActivity
    private var metodePengambilan: String = ""
    private var subtotalPesanan: Long = 0
    private var kategoriPesanan: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pilih_voucher)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil data dari intent
        metodePengambilan = intent.getStringExtra("metode_pengambilan") ?: ""
        subtotalPesanan = intent.getLongExtra("subtotal_pesanan", 0)
        kategoriPesanan = intent.getStringArrayListExtra("kategori_pesanan") ?: emptyList()

        initViews()
        setupRecyclerView()
        loadVouchers()
    }

    private fun initViews() {
        rvVoucher = findViewById(R.id.rvVoucher)
        backButton = findViewById(R.id.back)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        rvVoucher.layoutManager = LinearLayoutManager(this)
    }

    private fun loadVouchers() {
        lifecycleScope.launch {
            try {
                showLoading(true)

                // Ambil user ID dari Supabase Auth
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

                if (userId == null) {
                    Toast.makeText(
                        this@PilihVoucher,
                        "User tidak terautentikasi",
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    showEmpty(true)
                    return@launch
                }

                // Ambil voucher user dari Supabase
                val voucherList =
                    SupabaseManager.client.postgrest.rpc("get_user_available_vouchers", mapOf("uid" to userId)) {
                    }
                        .decodeList<Voucher>()

                if (voucherList.isEmpty()) {
                    showEmpty(true)
                    showLoading(false)
                    return@launch
                }

                // Validasi dan urutkan voucher
                val validatedVouchers = voucherList.map { voucher ->
                    voucher.copy(alasanTidakBisa = validateVoucher(voucher))
                }

                // Urutkan: voucher yang bisa digunakan tampil lebih dulu
                val sortedVouchers = validatedVouchers.sortedWith(
                    compareBy(
                        { it.alasanTidakBisa.isNotEmpty() }, // false (bisa dipakai) dulu
                        { !it.isActive }, // voucher aktif dulu
                        { it.minimal_pembelian ?: 0 } // minimal pembelian terendah dulu
                    )
                )

                // Setup adapter
                voucherAdapter = VoucherAdapter(sortedVouchers) { voucher ->
                    if (voucher.alasanTidakBisa.isEmpty()) {
                        selectVoucher(voucher)
                    } else {
                        Toast.makeText(
                            this@PilihVoucher,
                            voucher.alasanTidakBisa,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                rvVoucher.adapter = voucherAdapter
                showEmpty(false)
                showLoading(false)

            } catch (e: Exception) {
                Toast.makeText(
                    this@PilihVoucher,
                    "Gagal memuat voucher: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
                showLoading(false)
                showEmpty(true)
            }
        }
    }

    private fun validateVoucher(voucher: Voucher): String {
        // Cek apakah voucher aktif
        if (!voucher.isActive) {
            return "Voucher tidak aktif"
        }

        // Cek minimal pembelian
        if (subtotalPesanan < (voucher.minimal_pembelian?.toLong() ?: 0L)) {
            return "Minimal pembelian Rp ${formatRupiah(voucher.minimal_pembelian?.toLong() ?: 0L)}"
        }

        // Cek jenis voucher berdasarkan metode pengambilan
        when (voucher.jenis_voucher) {
            "diskon_ongkir" -> {
                if (metodePengambilan != "diantar") {
                    return "Hanya untuk pesanan diantar"
                }
            }

            "khusus_ambil" -> {
                if (metodePengambilan != "diambil") {
                    return "Hanya untuk pesanan diambil"
                }
            }
        }

        // Cek kategori
        if (voucher.kategoriList.isNotEmpty()) {
            val hasMatchingCategory = voucher.kategoriList.any { voucherKategori ->
                kategoriPesanan.contains(voucherKategori)
            }

            if (!hasMatchingCategory) {
                return "Tidak berlaku untuk kategori produk Anda"
            }
        }

        // Semua validasi lolos
        return ""
    }

    private fun selectVoucher(voucher: Voucher) {
        val intent = Intent()
        intent.putExtra("VOUCHER", voucher)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvVoucher.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmpty(show: Boolean) {
        emptyText.visibility = if (show) View.VISIBLE else View.GONE
        rvVoucher.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun formatRupiah(amount: Long): String {
        return String.format("%,d", amount).replace(',', '.')
    }
}