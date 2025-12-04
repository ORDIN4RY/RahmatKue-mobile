package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class PembayaranQrisActivity : AppCompatActivity() {
    private val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pembayaran_qris)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pesanan = intent.getParcelableExtra<Pesanan>(RincianPesananActivity.EXTRA_TRANSAKSI)

        val txtNomorPesanan = findViewById<TextView>(R.id.tvOrderNumber2)
        val txtWaktu = findViewById<TextView>(R.id.tvWaktu)
        val txtTotalPembayaran = findViewById<TextView>(R.id.tvTotal)
        val txtJumlahDibayar = findViewById<TextView>(R.id.jumlah_yg_dibayar)

        val total = pesanan?.totalHarga ?: 0

        txtNomorPesanan.text = pesanan?.nomorPesanan ?: "-"
        txtWaktu.text = pesanan?.createdAt?.substringBefore("T") ?: "-"
        txtTotalPembayaran.text = formatRupiah.format(total)
        txtJumlahDibayar.text = "Jumlah yang Harus Dibayar ${formatRupiah.format(total)}"

    }
}