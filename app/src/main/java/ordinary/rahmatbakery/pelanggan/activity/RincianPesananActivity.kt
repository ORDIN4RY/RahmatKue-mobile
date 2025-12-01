package ordinary.rahmatbakery.pelanggan.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan
import ordinary.rahmatbakery.pelanggan.adapter.RincianPesananAdapter
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import java.text.NumberFormat
import java.util.Locale
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.api.SupabaseManager
import kotlin.math.log

class RincianPesananActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSAKSI = "extra_transaksi"
    }

    private lateinit var rvPesanan: RecyclerView

    // Rincian harga
    private lateinit var txtTotalPesanan: TextView
    private lateinit var txtSubTotalPesanan: TextView
    private lateinit var txtSubtotalPengiriman: TextView
    private lateinit var txtPembayaranAwal: TextView
    private lateinit var txtPotonganHarga: TextView

    // Info transaksi
    private lateinit var txtNomorPesanan: TextView
    private lateinit var txtTglPemesanan: TextView
    private lateinit var txtTglPesananJadi: TextView
    private lateinit var txtMetodePembayaran: TextView
    private lateinit var txtMetodePengambilan: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtCatatan: TextView

    val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rincian_pesanan)

        initViews()

        // Ambil data parcelable
        val pesanan: Pesanan? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRANSAKSI, Pesanan::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRANSAKSI)
        }

        // Jika tidak ada data -> close activity
        if (pesanan == null) {
            finish()
            return
        }

        // Tampilkan semua data
        displayPesananData(pesanan)
        setupRecyclerView(pesanan)

        lifecycleScope.launch {
            val idTransaksi = pesanan?.idTransaksi ?: return@launch

            val detail = loadDetailPesanan(idTransaksi)

            detail?.let { fullData ->
                displayPesananData(fullData)    // ← nomor pesanan ditampilkan disini
            }
        }
    }

    private fun initViews() {
        rvPesanan = findViewById(R.id.rv_rincian_pesanan)

        txtTotalPesanan = findViewById(R.id.total_pesanan)
        txtSubTotalPesanan = findViewById(R.id.subtotal_pesanan)
        txtSubtotalPengiriman = findViewById(R.id.subtotal_pengiriman)
        txtPembayaranAwal = findViewById(R.id.pembayaran_awal)
        txtPotonganHarga = findViewById(R.id.potongan_harga)

        txtNomorPesanan = findViewById(R.id.nomor_pesanan)
        txtTglPemesanan = findViewById(R.id.tgl_pemesanan)
        txtTglPesananJadi = findViewById(R.id.tgl_pesanan_jadi)
        txtMetodePembayaran = findViewById(R.id.metode_pembayaran)
        txtMetodePengambilan = findViewById(R.id.metode_pengantaran)
        txtStatus = findViewById(R.id.status)
        txtCatatan = findViewById(R.id.catatan)
    }

    /** =============================
     *  TAMPILKAN DATA PESANAN
     *  ============================= */
    private fun displayPesananData(pesanan: Pesanan) {

        txtNomorPesanan.text = pesanan.nomorPesanan
        txtStatus.text = pesanan.status
        txtMetodePengambilan.text = pesanan.metodePengambilan ?: "-"


        txtTglPemesanan.text = pesanan.createdAt.substringBefore("T")
        txtTglPesananJadi.text = pesanan.tglPesananJadi.substringBefore("T")

        txtCatatan.text = if (pesanan.catatan.isNullOrBlank()) "-" else pesanan.catatan

        // Rincian harga
        txtSubTotalPesanan.text = formatRupiah.format(pesanan.totalHarga)
        txtSubtotalPengiriman.text = formatRupiah.format(pesanan.ongkir?: 0)
        txtPembayaranAwal.text = formatRupiah.format(pesanan.DP?: 0)
        txtPotonganHarga.text = formatRupiah.format(pesanan.potonganHarga?: 0)

        // Total akhir
        txtTotalPesanan.text = formatRupiah.format(pesanan.totalHarga)
    }

    /** =============================
     *  SETUP ITEM LIST
     *  ============================= */
    private fun setupRecyclerView(pesanan: Pesanan) {

        val itemProdukList = pesanan.items.map { item ->
            TampilanItemPesanan(
                nama = item.produk.namaProduk,
                jumlah = item.jumlah,
                subtotal = item.subtotal,
                foto = item.produk.fotoProduk,
                hargaSatuan = item.produk.harga,
            )
        }

        val itemPaketList = pesanan.paketItems.map { item ->
            TampilanItemPesanan(
                nama = item.paket.namaPaket,
                jumlah = item.jumlah,
                subtotal = item.subtotal,
                foto = item.paket.fotoPaket,
                hargaSatuan = item.paket.hargaPaket,
            )
        }

        val semuaItem = itemProdukList + itemPaketList
        val itemAdapter = RincianPesananAdapter()
        rvPesanan.layoutManager = LinearLayoutManager(this)
        rvPesanan.adapter = itemAdapter
        itemAdapter.submitList(semuaItem)

    }

    private suspend fun loadDetailPesanan(idTransaksi: String): Pesanan? {
        return try {
            SupabaseManager.client.postgrest.from("transaksi")
                .select(
                    Columns.raw(
                        """
                    id_transaksi,
                    total_harga,
                    status,
                    created_at,
                    metode_pengambilan,
                    waktu_selesai,
                    catatan,
                    ongkir,
                    nomor_pesanan,
                    dp_minimal,
                    potongan,

                    detail_transaksi_produk(
                        jumlah,
                        subtotal,
                        produk(
                            id_produk,
                            nama_produk,
                            harga,
                            foto_produk
                        )
                    ),

                    detail_transaksi_paket(
                        jumlah,
                        subtotal,
                        paket(
                            id_paket,
                            nama_paket,
                            harga_paket,
                            foto_paket
                        )
                    )
                    """
                    )
                ) {
                    filter { eq("id_transaksi", idTransaksi) }
                }
                .decodeSingle<Pesanan>()
        } catch (e: Exception) {
            Log.e("SUPABASE", "Error load pesanan: ${e.message}")
            null
        }
    }

}
