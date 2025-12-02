package ordinary.rahmatbakery.pelanggan.activity

import android.media.MediaRouter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.VoucherActivity
import ordinary.rahmatbakery.pelanggan.adapter.VoucherSayaAdapter
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher
import ordinary.rahmatbakery.model.Profile
import coil.load
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.ktor.websocket.Frame
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.Kategori
import ordinary.rahmatbakery.pelanggan.model.KategoriVoucher
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.compareTo

class DetailVoucherActivity : AppCompatActivity() {


    var profile: Profile? = null
    var kategoriVoucher: KategoriVoucher? = null

    val poinUser = profile?.point

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
        val userVoucher = intent.getParcelableExtra<UserVoucher>("data_voucher_saya")
        val tukarVoucher = intent.getParcelableExtra<Voucher>("data_tukar_voucher")

        val btnTukarVoucher = findViewById<TextView>(R.id.btn_tukar_voucher)
        syaratKetentuan = findViewById(R.id.syarat_ketentuan)


        if (userVoucher != null) {
            // 🔥 Jika data berasal dari Voucher Saya
            setupVoucherSaya(userVoucher)
        } else if (tukarVoucher != null) {
            // 🔥 Jika data berasal dari Tukar Voucher
            setupTukarVoucher(tukarVoucher)
        } else {
               finish()
        }

    loadUserPoint()
        val poinUser = loadUserPoint()
    }

    private fun setupVoucherSaya(data: UserVoucher) {
        val voucher = data.voucher
        val status = getStatusMasaBerlaku(data.voucher.tgl_mulai, data.voucher.tgl_berakhir)
        val tvStatus = findViewById<TextView>(R.id.masa_berlaku)
        val statusTanggal = getStatusMasaBerlaku(
            voucher.tgl_mulai,
            voucher.tgl_berakhir
        )

        // Contoh implementasi isi data
        findViewById<TextView>(R.id.nama_voucher).text = voucher.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = voucher.deskripsi
        findViewById<TextView>(R.id.masa_berlaku).text =
            formatMasaBerlaku(data.voucher.tgl_mulai, data.voucher.tgl_berakhir)
        findViewById<TextView>(R.id.jumlah_poin).visibility = View.GONE
        findViewById<TextView>(R.id.poin).visibility = View.GONE


        findViewById<ImageView>(R.id.gambar_voucher).load(voucher.foto_voucher)
        findViewById<TextView>(R.id.btn_tukar_voucher).visibility = View.GONE


        when (statusTanggal) {
            0 -> tvStatus.setTextColor(getColor(R.color.status_belum_aktif))     // Belum aktif
            1 -> tvStatus.setTextColor(getColor(R.color.status_aktif))    // Aktif
            2 -> tvStatus.setTextColor(getColor(R.color.status_expired))      // Expired
        }

        // Jika voucher sudah digunakan → override warna/status
        if (data.status == "sudah_digunakan") {
            tvStatus.text = "Sudah Digunakan"
            tvStatus.setTextColor(getColor(R.color.maroon))
        }

        loadKategoriVoucher(data.id_voucher) { kategoriList ->
            syaratKetentuan.text = """
            1. Minimal pembelian: Rp. ${data.voucher.minimal_pembelian}
            2. Potongan: ${data.voucher.persentase_potongan}% (maks: Rp. ${data.voucher.maksimal_potongan})
            3. Berlaku hanya untuk kategori ${kategoriList.joinToString(", ")}
            4. Tidak dapat digabung dengan promo lain
        """.trimIndent()
        }
    }


    private fun getStatusMasaBerlaku(tglMulai: String, tglBerakhir: String): Int {
        if (tglMulai.isBlank() || tglBerakhir.isBlank()) return 2 // expired

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val mulai: Date = format.parse(tglMulai)
        val berakhir: Date = format.parse(tglBerakhir)
        val today = Date()

        return when {
            today.before(mulai) -> 0   // Belum aktif
            today.after(berakhir) -> 2 // Expired
            else -> 1                  // Aktif
        }
    }

    private fun formatMasaBerlaku(tglMulai: String, tglBerakhir: String): String {
        val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val mulai = inFormat.parse(tglMulai)
        val akhir = inFormat.parse(tglBerakhir)

        return "${outFormat.format(mulai)} - ${outFormat.format(akhir)}"
    }

    private fun setupTukarVoucher(data: Voucher) {

        val status = getStatusMasaBerlaku(data.tgl_mulai, data.tgl_berakhir)
        val tvStatus = findViewById<TextView>(R.id.masa_berlaku)
        val statusTanggal = getStatusMasaBerlaku(
            data.tgl_mulai,
            data.tgl_berakhir
        )


        // Isi data untuk voucher yang bisa ditukar
        findViewById<TextView>(R.id.nama_voucher).text = data.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = data.deskripsi
        findViewById<TextView>(R.id.jumlah_poin).text = data.poin_tukar.toString()

        findViewById<TextView>(R.id.masa_berlaku).text =
            formatMasaBerlaku(data.tgl_mulai, data.tgl_berakhir)
        findViewById<TextView>(R.id.btn_tukar_voucher).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.gambar_voucher).load(data.foto_voucher)

        loadKategoriVoucher(data.id_voucher) { kategoriNames ->
            val kategoriText = if (kategoriNames.isEmpty()) {
                "Semua"
            } else {
                kategoriNames.joinToString(", ")
            }
            syaratKetentuan.text = """
            1. Minimal pembelian: Rp. ${data.minimal_pembelian}
            2. Potongan: ${data.persentase_potongan}% (maks: Rp. ${data.maksimal_potongan})
            3. Berlaku hanya untuk kategori $kategoriText
            4. Tidak dapat digabung dengan promo lain
        """.trimIndent()
        }
        val btnTukarVoucher = findViewById<TextView>(R.id.btn_tukar_voucher)

        if (poinUser != null) {
            if (poinUser < data.poin_tukar?:0){
                btnTukarVoucher.isEnabled=false

            }
        }
    }
    private fun loadKategoriVoucher(voucherId: String, callback: (List<String>) -> Unit) {
        lifecycleScope.launch {
            try {
                val kategoriIds = SupabaseManager.client.postgrest
                    .from("voucher_kategori")
                    .select(Columns.raw("id_kategori")){
                        filter { eq("id_voucher", voucherId) }
                    }
                    .decodeList<KategoriVoucher>()
                val kategoriNames = kategoriIds.map { it.kategori.nama  }
//                val kategoriNames = mutableListOf<String>()
//                for (item in kategoriIds) {
//                    val kategori = SupabaseManager.client.postgrest
//                        .from("kategori")
//                        .select(Columns.raw("nama_kategori")){
//                        filter {  eq("id_kategori", item.id_)}}
//                        .decodeSingle<Kategori>()
//                    kategoriNames.add(kategori.nama)
//                }

                callback(kategoriNames)

            } catch (e: Exception) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }

    private fun loadUserPoint() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val result = SupabaseManager.client.postgrest
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", userId)   // ✔ sama persis format seperti loadPesanan
                            }
                        }
                        .decodeSingle<Profile>()

                    val point = result.point


                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

