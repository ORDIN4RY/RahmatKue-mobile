package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns // Pastikan ini di-import
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.KategoriVoucher
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher
import ordinary.rahmatbakery.model.Profile
import java.text.SimpleDateFormat
import java.util.*

class DetailVoucherActivity : AppCompatActivity() {

    private var profile: Profile? = null
    private var poinUser: Int = 0
    private var originalVoucherList: MutableList<Voucher> = mutableListOf()
    private lateinit var syaratKetentuan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_voucher)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_voucher)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        syaratKetentuan = findViewById(R.id.syarat_ketentuan)

        val userVoucher = intent.getParcelableExtra<UserVoucher>("data_voucher_saya")
        val tukarVoucher = intent.getParcelableExtra<Voucher>("data_tukar_voucher")

        if (userVoucher != null) {
            setupVoucherSaya(userVoucher)
        } else if (tukarVoucher != null) {
            originalVoucherList = mutableListOf(tukarVoucher) // simpan list voucher yang bisa ditukar
            setupTukarVoucher(tukarVoucher)
        } else {
            finish()
        }

        loadUserPoint() // load poin user
    }

    private fun setupVoucherSaya(data: UserVoucher) {
        val voucher = data.voucher
        val tvStatus = findViewById<TextView>(R.id.masa_berlaku)
        val statusTanggal = getStatusMasaBerlaku(voucher.tgl_mulai, voucher.tgl_berakhir)

        findViewById<TextView>(R.id.nama_voucher).text = voucher.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = voucher.deskripsi
        findViewById<TextView>(R.id.masa_berlaku).text = formatMasaBerlaku(voucher.tgl_mulai, voucher.tgl_berakhir)
        findViewById<TextView>(R.id.jumlah_poin).visibility = View.GONE
        findViewById<TextView>(R.id.poin).visibility = View.GONE
        findViewById<ImageView>(R.id.gambar_voucher).load(voucher.foto_voucher)
        findViewById<TextView>(R.id.btn_tukar_voucher).visibility = View.GONE

        when (statusTanggal) {
            0 -> tvStatus.setTextColor(getColor(R.color.status_belum_aktif))
            1 -> tvStatus.setTextColor(getColor(R.color.status_aktif))
            2 -> tvStatus.setTextColor(getColor(R.color.status_expired))
        }

        if (data.status == "sudah_digunakan") {
            tvStatus.text = "Sudah Digunakan"
            tvStatus.setTextColor(getColor(R.color.maroon))
        }
    }

    private fun setupTukarVoucher(data: Voucher) {
        val tvStatus = findViewById<TextView>(R.id.masa_berlaku)
        val statusTanggal = getStatusMasaBerlaku(data.tgl_mulai, data.tgl_berakhir)

        findViewById<TextView>(R.id.nama_voucher).text = data.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = data.deskripsi
        findViewById<TextView>(R.id.jumlah_poin).text = data.poin_tukar.toString()
        findViewById<TextView>(R.id.masa_berlaku).text = formatMasaBerlaku(data.tgl_mulai, data.tgl_berakhir)
        findViewById<TextView>(R.id.btn_tukar_voucher).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.gambar_voucher).load(data.foto_voucher)

        loadKategoriVoucher(data.id_voucher) { kategoriNames ->
            val kategoriText = if (kategoriNames.isEmpty()) "Semua" else kategoriNames.joinToString(", ")
            syaratKetentuan.text = """
                1. Minimal pembelian: Rp. ${data.minimal_pembelian}
                2. Potongan: ${data.persentase_potongan}% (maks: Rp. ${data.maksimal_potongan})
                3. Berlaku hanya untuk kategori $kategoriText
                4. Tidak dapat digabung dengan promo lain
            """.trimIndent()
        }

        val btnTukarVoucher = findViewById<TextView>(R.id.btn_tukar_voucher)


        btnTukarVoucher.setOnClickListener {
            redeemVoucher(data.id_voucher)
        }
    }

    private fun loadKategoriVoucher(voucherId: String, callback: (List<String>) -> Unit) {
        lifecycleScope.launch {
            try {
                // PERBAIKAN 1: Menggunakan Columns.raw() untuk mengatasi error type mismatch
                val kategoriIds = SupabaseManager.client.postgrest
                    .from("voucher_kategori")
                    .select(Columns.raw("id_kategori")) { // <-- PERBAIKAN DI SINI
                        filter { eq("id_voucher", voucherId) }
                    }
                    .decodeList<KategoriVoucher>()

                // Asumsi: KategoriVoucher memiliki properti 'kategori' yang memiliki 'nama'
                // Jika ini menyebabkan error, struktur model data Anda mungkin perlu disesuaikan
                // atau cara query-nya harus diubah (misal dengan join seperti saran sebelumnya).
                val kategoriNames = kategoriIds.map { it.kategori.nama }
                callback(kategoriNames)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }

    private fun loadUserPoint() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return
        lifecycleScope.launch {
            try {
                val result = SupabaseManager.client.postgrest
                    .from("profiles")
                    .select {
                        filter { eq("id", userId) }
                    }
                    .decodeSingle<Profile>()

                poinUser = result.point ?: 0
                refreshVoucherList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun redeemVoucher(voucherId: String) {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Toast.makeText(this, "Anda harus login untuk menukar voucher.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                val params = buildJsonObject {
                    put("p_user_id", userId)
                    put("p_voucher_id", voucherId)
                }

                val result = SupabaseManager.client.postgrest
                    .rpc("redeem_voucher_uuid", params)
                    .decodeAs<Map<String, String>>()

                val success = result["success"]?.toBoolean() ?: false
                val message = result["message"] ?: "Terjadi kesalahan."

                Toast.makeText(this@DetailVoucherActivity, message, Toast.LENGTH_LONG).show()

                if (success) {
                    // Update poin user
                    loadUserPoint()
                    // Hapus voucher dari list dan refresh UI
                    removeVoucherFromTukarList(voucherId)

                    // Pindah ke VoucherActivity
                    val intent = Intent(this@DetailVoucherActivity, VoucherActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@DetailVoucherActivity,
                    "Gagal menukar voucher: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun removeVoucherFromTukarList(voucherId: String) {
        originalVoucherList = originalVoucherList.filter { it.id_voucher != voucherId }.toMutableList()
        refreshVoucherList()
    }

    private fun refreshVoucherList() {
        val voucher = originalVoucherList.firstOrNull() ?: return
        val btnTukarVoucher = findViewById<TextView>(R.id.btn_tukar_voucher)

        // PERBAIKAN 2: Menggunakan elvis operator (?:) untuk menangani nullable Int
        btnTukarVoucher.isEnabled = poinUser >= (voucher.poin_tukar ?: 0) // <-- PERBAIKAN DI SINI
    }

    private fun getStatusMasaBerlaku(tglMulai: String, tglBerakhir: String): Int {
        if (tglMulai.isBlank() || tglBerakhir.isBlank()) return 2
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val mulai: Date = format.parse(tglMulai)
        val berakhir: Date = format.parse(tglBerakhir)
        val today = Date()
        return when {
            today.before(mulai) -> 0
            today.after(berakhir) -> 2
            else -> 1
        }
    }

    private fun formatMasaBerlaku(tglMulai: String, tglBerakhir: String): String {
        val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return "${outFormat.format(inFormat.parse(tglMulai))} - ${outFormat.format(inFormat.parse(tglBerakhir))}"
    }
}
