package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher
import ordinary.rahmatbakery.model.Profile
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailVoucherActivity : AppCompatActivity() {

    private var profile: Profile? = null
    private var poinUser: Int = 0
    private var originalVoucherList: MutableList<Voucher> = mutableListOf()
    private lateinit var containerSK: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_voucher)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_voucher)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerSK = findViewById(R.id.container_syarat_ketentuan)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        val userVoucher = intent.getParcelableExtra<UserVoucher>("data_voucher_saya")
        val tukarVoucher = intent.getParcelableExtra<Voucher>("data_tukar_voucher")

        if (userVoucher != null) {
            setupVoucherSaya(userVoucher)
        } else if (tukarVoucher != null) {
            originalVoucherList = mutableListOf(tukarVoucher)
            setupTukarVoucher(tukarVoucher)
        } else {
            finish()
        }

        loadUserPoint()
    }

    private fun setupVoucherSaya(data: UserVoucher) {
        val voucher = data.voucher
        val tvStatus = findViewById<TextView>(R.id.masa_berlaku)
        val statusTanggal = getStatusMasaBerlaku(voucher.tgl_mulai, voucher.tgl_berakhir)

        findViewById<TextView>(R.id.nama_voucher).text = voucher.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = voucher.deskripsi
        findViewById<ImageView>(R.id.gambar_voucher).load(voucher.foto_voucher)
        findViewById<TextView>(R.id.btn_tukar_voucher).visibility = View.GONE

        // Show info cards for owned vouchers too
        findViewById<LinearLayout>(R.id.container_info_cards).visibility = View.VISIBLE
        findViewById<TextView>(R.id.jumlah_poin).visibility = View.GONE
        findViewById<TextView>(R.id.poin).visibility = View.GONE

        // Setup info cards dengan data voucher
        setupInfoCards(voucher)

        // Set masa berlaku
        findViewById<TextView>(R.id.masa_berlaku).text = formatMasaBerlaku(voucher.tgl_mulai, voucher.tgl_berakhir)

        when (statusTanggal) {
            0 -> tvStatus.setTextColor(getColor(R.color.status_belum_aktif))
            1 -> tvStatus.setTextColor(getColor(R.color.status_aktif))
            2 -> tvStatus.setTextColor(getColor(R.color.status_expired))
        }

        if (data.status == "sudah_digunakan") {
            tvStatus.text = "Sudah Digunakan"
            tvStatus.setTextColor(getColor(R.color.maroon))
        }

        // Setup syarat ketentuan untuk voucher saya
        setupSyaratKetentuan(voucher)
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

        when (statusTanggal) {
            0 -> tvStatus.setTextColor(getColor(R.color.status_belum_aktif))
            1 -> tvStatus.setTextColor(getColor(R.color.status_aktif))
            2 -> tvStatus.setTextColor(getColor(R.color.status_expired))
        }

        // Setup info cards
        setupInfoCards(data)

        // Setup syarat ketentuan
        setupSyaratKetentuan(data)

        val btnTukarVoucher = findViewById<TextView>(R.id.btn_tukar_voucher)
        btnTukarVoucher.setOnClickListener {
            redeemVoucher(data.id_voucher)
        }
    }

    private fun setupInfoCards(voucher: Voucher) {
        val containerInfoCards = findViewById<LinearLayout>(R.id.container_info_cards)
        containerInfoCards.visibility = View.VISIBLE

        // Potongan harga
        val tvPotongan = findViewById<TextView>(R.id.tv_potongan_value)
        val tvMaksPotongan = findViewById<TextView>(R.id.tv_maks_potongan)
        tvPotongan.text = "${voucher.persentase_potongan}%"

        if (voucher.maksimal_potongan != null && voucher.maksimal_potongan!! > 0) {
            tvMaksPotongan.text = "Maks: ${formatRupiah(voucher.maksimal_potongan!!)}"
            tvMaksPotongan.visibility = View.VISIBLE
        } else {
            tvMaksPotongan.visibility = View.GONE
        }

        // Minimal pembelian
        val tvMinBelanja = findViewById<TextView>(R.id.tv_min_belanja_value)
        tvMinBelanja.text = formatRupiah(voucher.minimal_pembelian ?: 0)

        // Kategori
        val tvKategori = findViewById<TextView>(R.id.tv_kategori_value)
        val kategori = if (!voucher.kategoriList.isEmpty()) {
            voucher.kategoriList.joinToString(", ")
        } else {
            "Semua Kategori"
        }
        tvKategori.text = kategori
    }

    private fun setupSyaratKetentuan(voucher: Voucher) {
        containerSK.removeAllViews()

        val listSK = listOf(
            "Masa Berlaku" to "Voucher berlaku mulai ${formatTanggalIndo(voucher.tgl_mulai)} sampai ${formatTanggalIndo(voucher.tgl_berakhir)}",
            "Penggunaan" to "Voucher hanya dapat digunakan sekali per transaksi dan tidak dapat digabung dengan voucher lain",
            "Penukaran Poin" to "Diperlukan ${voucher.poin_tukar} poin untuk menukar voucher ini. Poin akan langsung dipotong saat penukaran",
            "Ketentuan Khusus" to "Voucher tidak dapat ditukar dengan uang tunai atau dikembalikan setelah ditukarkan",
            "Pembatalan" to "Rahmat Bakery berhak membatalkan voucher jika terjadi penyalahgunaan, kecurangan, atau pelanggaran ketentuan"
        )

        for ((index, item) in listSK.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_syarat, null)

            val tvNumber = itemView.findViewById<TextView>(R.id.tvNumber)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvSub = itemView.findViewById<TextView>(R.id.tvSub)

            tvNumber.text = "${index + 1}."
            tvTitle.text = item.first
            tvSub.text = item.second

            containerSK.addView(itemView)
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }

    private fun formatTanggalIndo(tanggal: String): String {
        val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return outFormat.format(inFormat.parse(tanggal))
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
                    loadUserPoint()
                    removeVoucherFromTukarList(voucherId)

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

        btnTukarVoucher.isEnabled = poinUser >= (voucher.poin_tukar ?: 0)

        if (!btnTukarVoucher.isEnabled) {
            btnTukarVoucher.alpha = 0.5f
            btnTukarVoucher.text = "Poin Tidak Cukup"
        } else {
            btnTukarVoucher.alpha = 1f
            btnTukarVoucher.text = "Tukarkan Voucher"
        }
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