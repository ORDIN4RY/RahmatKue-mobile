package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.Alamat
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
import ordinary.rahmatbakery.util.SupabaseManager
import kotlin.math.log

class RincianPesananActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSAKSI = "extra_transaksi"
    }

    private lateinit var rvPesanan: RecyclerView

    // Rincian harga
    private lateinit var txtTotalPesanan: TextView
    private lateinit var txtHargaTotalProduk: TextView
    private lateinit var txtSubTotalPesanan: TextView
    private lateinit var txtSubtotalPengiriman: TextView
    private lateinit var txtPembayaranAwal: TextView
    private lateinit var txtTitlePembayaranAwal: TextView
    private lateinit var txtKekuranganBayar: TextView
    private lateinit var txtPotonganHarga: TextView

    private lateinit var txtNomorPesanan: TextView
    private lateinit var txtTglPemesanan: TextView
    private lateinit var txtTglPesananJadi: TextView
    private lateinit var txtMetodePembayaran: TextView
    private lateinit var txtMetodePengambilan: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtCatatan: TextView
    private lateinit var txtNamaPenerima: TextView
    private lateinit var txtNoHpPenerima: TextView
    private lateinit var txtAlamatPenerima: TextView

    private lateinit var btnBayarSekarang: TextView

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

        findViewById<ImageView>(R.id.back).setOnClickListener{
            this.finish()
        }
    }

    private fun initViews() {
        rvPesanan = findViewById(R.id.rv_rincian_pesanan)

        txtTotalPesanan = findViewById(R.id.total_pesanan)
        txtHargaTotalProduk = findViewById(R.id.total_harga_produk)
        txtSubTotalPesanan = findViewById(R.id.subtotal_pesanan)
        txtSubtotalPengiriman = findViewById(R.id.subtotal_pengiriman)
        txtPembayaranAwal = findViewById(R.id.pembayaran_awal)
        txtPotonganHarga = findViewById(R.id.potongan_harga)
        txtKekuranganBayar = findViewById(R.id.kekurangan_bayar)
        txtTitlePembayaranAwal = findViewById(R.id.title_pembayaran_awal)

        txtNomorPesanan = findViewById(R.id.nomor_pesanan)
        txtTglPemesanan = findViewById(R.id.tgl_pemesanan)
        txtTglPesananJadi = findViewById(R.id.tgl_pesanan_jadi)
        txtMetodePembayaran = findViewById(R.id.metode_pembayaran)
        txtMetodePengambilan = findViewById(R.id.metode_pengantaran)
        txtStatus = findViewById(R.id.status)
        txtCatatan = findViewById(R.id.catatan)
        btnBayarSekarang = findViewById(R.id.btn_bayar_sekarang)

        txtNamaPenerima = findViewById(R.id.nama_penerima)
        txtNoHpPenerima = findViewById(R.id.no_hp_penerima)
        txtAlamatPenerima = findViewById(R.id.alamat_penerima)

    }

    /** =============================
     *  TAMPILKAN DATA PESANAN
     *  ============================= */
    private fun displayPesananData(pesanan: Pesanan) {

        txtNomorPesanan.text = pesanan.nomorPesanan
        txtStatus.text = pesanan.status
        txtMetodePengambilan.text = pesanan.metodePengambilan ?: "-"
        txtCatatan.text = pesanan.catatan

            txtNamaPenerima.text = pesanan.alamat?.nama
            txtNoHpPenerima.text = pesanan.alamat?.noHp
            txtAlamatPenerima.text = pesanan.alamat?.alamat

        txtTglPemesanan.text = pesanan.createdAt.substringBefore("T")
        txtTglPesananJadi.text = pesanan.tglPesananJadi.substringBefore("T")

        txtCatatan.text = if (pesanan.catatan.isNullOrBlank()) "-" else pesanan.catatan

        // Rincian harga
        val kekuranganBayar = pesanan.totalHarga - (pesanan.DP ?: 0)

        txtSubtotalPengiriman.text = formatRupiah.format(pesanan.ongkir ?: 0)
        if (pesanan.DP == pesanan.totalHarga){
            txtPembayaranAwal.text =("Rp. -")
        }else {
            txtPembayaranAwal.visibility= View.VISIBLE
            txtPembayaranAwal.text = formatRupiah.format(pesanan.DP ?: 0)
            txtTitlePembayaranAwal.visibility= View.VISIBLE
            txtKekuranganBayar.visibility= View.VISIBLE
            txtKekuranganBayar.text ="Kekurangan : ${formatRupiah.format(kekuranganBayar)}"
        }
        if(pesanan.potonganHarga==0){
            txtPotonganHarga.text = ("Rp. -")
        }else {
            txtPotonganHarga.text = formatRupiah.format(pesanan.potonganHarga)
        }
        // Total akhir
        val totalHarga = formatRupiah.format(pesanan.totalHarga)
        txtTotalPesanan.text = "Total Pesanan : ${totalHarga}"
        val totalHargaPaket = pesanan.paketItems.sumOf { it.subtotal }
        val totalHargaItem = pesanan.items.sumOf { it.subtotal }
        val totalHargaProduk = totalHargaPaket + totalHargaItem

        txtSubTotalPesanan.text = formatRupiah.format(totalHargaProduk)
        if (pesanan.totalHarga == pesanan.DP){
            txtMetodePembayaran.text ="Lunas"
        }else{
            txtMetodePembayaran.text ="DP"
        }
        txtHargaTotalProduk.text = formatRupiah.format(totalHargaProduk)

        if (pesanan.status == "Menunggu Pembayaran") {
            btnBayarSekarang.visibility = View.VISIBLE
            btnBayarSekarang.setOnClickListener {
                val intent = Intent(this@RincianPesananActivity, PembayaranQrisActivity::class.java)
                intent.putExtra(RincianPesananActivity.EXTRA_TRANSAKSI, pesanan)
                startActivity(intent)
            }
        }
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
            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

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
                    
                   alamat(  
                id_alamat, 
                nama_lengkap,
                no_hp_penerima,
                alamat_rumah,
                id_user,         
                alamat_utama
                ),
                    
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
            e.printStackTrace()
            null
        }
    }

}
