package ordinary.rahmatbakery.pelanggan.activity

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
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.VoucherActivity
import ordinary.rahmatbakery.pelanggan.adapter.VoucherSayaAdapter
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher
import coil.load
class DetailVoucherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_voucher)
        val userVoucher = intent.getParcelableExtra<UserVoucher>("data_voucher_saya")
        val tukarVoucher = intent.getParcelableExtra<Voucher>("data_tukar_voucher")

        if (userVoucher != null) {
            // 🔥 Jika data berasal dari Voucher Saya
            setupVoucherSaya(userVoucher)
        } else if (tukarVoucher != null) {
            // 🔥 Jika data berasal dari Tukar Voucher
            setupTukarVoucher(tukarVoucher)
        } else {
            // 🔥 Jika tidak ada data → error
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupVoucherSaya(data: UserVoucher) {
        val voucher = data.voucher

        // Contoh implementasi isi data
        findViewById<TextView>(R.id.nama_voucher).text = voucher.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = voucher.deskripsi
        findViewById<TextView>(R.id.status_voucher).text = data.status
        findViewById<TextView>(R.id.status_voucher).visibility = View.VISIBLE
        findViewById<TextView>(R.id.jumlah_poin).visibility = View.GONE
        findViewById<TextView>(R.id.poin).visibility = View.GONE


        findViewById<ImageView>(R.id.gambar_voucher).load(voucher.foto_voucher)

        // Logika tambahan:
        // 🔥 Jika voucher sudah digunakan
        if (data.status == "sudah_digunakan") {
            findViewById<TextView>(R.id.status_voucher).setTextColor(getColor(R.color.maroon))
        }
    }

    private fun setupTukarVoucher(data: Voucher) {
        // Isi data untuk voucher yang bisa ditukar
        findViewById<TextView>(R.id.nama_voucher).text = data.nama_voucher
        findViewById<TextView>(R.id.deskripsi_voucher).text = data.deskripsi
        findViewById<TextView>(R.id.jumlah_poin).text = data.poin_tukar.toString()
        findViewById<TextView>(R.id.status_voucher).visibility= View.GONE

        findViewById<ImageView>(R.id.gambar_voucher).load(data.foto_voucher)

        // Logika tambahan:
        // 🔥 Jika poin user kurang → disable tombol
        /*
        val poinUser = 200
        if (poinUser < data.poin_tukar) {
            findViewById<Button>(R.id.btnTukar).isEnabled = false
        }
        */
    }
    }
