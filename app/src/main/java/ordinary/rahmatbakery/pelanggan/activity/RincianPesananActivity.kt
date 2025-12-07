package ordinary.rahmatbakery.pelanggan.activity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.RincianPesananAdapter
import ordinary.rahmatbakery.pelanggan.helper.XenditHelper
import ordinary.rahmatbakery.pelanggan.model.Alamat
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan
import ordinary.rahmatbakery.util.SupabaseManager
import java.text.NumberFormat
import java.util.*

class RincianPesananActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRANSAKSI = "extra_transaksi"
    }

    private lateinit var rvPesanan: RecyclerView
    private lateinit var txtTotalPesanan: TextView
    private lateinit var txtHargaTotalProduk: TextView
    private lateinit var txtSubTotalPesanan: TextView
    private lateinit var txtSubtotalPengiriman: TextView
    private lateinit var txtPembayaranAwal: TextView
    private lateinit var txtTitlePembayaranAwal: TextView
    private lateinit var txtKekuranganBayar: TextView
    private lateinit var txtPotonganHarga: TextView
    private lateinit var layoutPotongan: LinearLayout
    private lateinit var layoutPembayaranAwal: LinearLayout

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

    private lateinit var btnBayarSekarang: Button
    private lateinit var btnBatalkan: Button

    private var progressDialog: ProgressDialog? = null
    private var currentPesanan: Pesanan? = null

    val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rincian_pesanan)

        initViews()
        setupListeners()

        // Ambil data pesanan
        val pesanan: Pesanan? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRANSAKSI, Pesanan::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRANSAKSI)
        }

        if (pesanan == null) {
            finish()
            return
        }

        currentPesanan = pesanan
        displayPesananData(pesanan)
        setupRecyclerView(pesanan)

        // Load detail lengkap
        lifecycleScope.launch {
            val detail = loadDetailPesanan(pesanan.idTransaksi)
            detail?.let { fullData ->
                currentPesanan = fullData
                displayPesananData(fullData)
            }
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
        layoutPotongan = findViewById(R.id.layout_potongan)
        layoutPembayaranAwal = findViewById(R.id.layout_pembayaran_awal)

        txtNomorPesanan = findViewById(R.id.nomor_pesanan)
        txtTglPemesanan = findViewById(R.id.tgl_pemesanan)
        txtTglPesananJadi = findViewById(R.id.tgl_pesanan_jadi)
        txtMetodePembayaran = findViewById(R.id.metode_pembayaran)
        txtMetodePengambilan = findViewById(R.id.metode_pengantaran)
        txtStatus = findViewById(R.id.status)
        txtCatatan = findViewById(R.id.catatan)

        txtNamaPenerima = findViewById(R.id.nama_penerima)
        txtNoHpPenerima = findViewById(R.id.no_hp_penerima)
        txtAlamatPenerima = findViewById(R.id.alamat_penerima)

        btnBayarSekarang = findViewById(R.id.btn_bayar_sekarang)
        btnBatalkan = findViewById(R.id.btn_batalkan)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        btnBayarSekarang.setOnClickListener {
            currentPesanan?.let { pesanan ->
                showPaymentConfirmation(pesanan)
            }
        }

        btnBatalkan.setOnClickListener {
            currentPesanan?.let { pesanan ->
                showCancelConfirmation(pesanan)
            }
        }
    }

    private fun displayPesananData(pesanan: Pesanan) {
        // Status & Info Dasar
        txtNomorPesanan.text = pesanan.nomorPesanan ?: "-"
        txtStatus.text = pesanan.status
        txtMetodePengambilan.text = pesanan.metodePengambilan ?: "-"
        txtCatatan.text = if (pesanan.catatan.isNullOrBlank()) "-" else pesanan.catatan

        // Alamat
        txtNamaPenerima.text = pesanan.alamat?.nama ?: "-"
        txtNoHpPenerima.text = pesanan.alamat?.noHp ?: "-"
        txtAlamatPenerima.text = pesanan.alamat?.alamat ?: "-"

        // Tanggal
        txtTglPemesanan.text = pesanan.createdAt.substringBefore("T")
        txtTglPesananJadi.text = pesanan.tglPesananJadi.substringBefore("T")

        // Hitung total harga produk
        val totalHargaPaket = pesanan.paketItems.sumOf { it.subtotal }
        val totalHargaItem = pesanan.items.sumOf { it.subtotal }
        val totalHargaProduk = totalHargaPaket + totalHargaItem

        // Rincian Harga
        txtSubTotalPesanan.text = formatRupiah.format(totalHargaProduk)
        txtSubtotalPengiriman.text = formatRupiah.format(pesanan.ongkir ?: 0)
        txtHargaTotalProduk.text = formatRupiah.format(totalHargaProduk)

        // Potongan harga
        if (pesanan.potonganHarga!! > 0) {
            layoutPotongan.visibility = View.VISIBLE
            txtPotonganHarga.text = "- ${formatRupiah.format(pesanan.potonganHarga)}"
        } else {
            layoutPotongan.visibility = View.GONE
        }

        // Total Pesanan
        txtTotalPesanan.text = formatRupiah.format(pesanan.totalHarga)

        // Metode Pembayaran & DP
        if (pesanan.DP == pesanan.totalHarga) {
            txtMetodePembayaran.text = "Lunas"
            layoutPembayaranAwal.visibility = View.GONE
        } else {
            txtMetodePembayaran.text = "DP (50%)"
            layoutPembayaranAwal.visibility = View.VISIBLE

            val kekuranganBayar = pesanan.totalHarga - (pesanan.DP ?: 0)
            txtPembayaranAwal.text = formatRupiah.format(pesanan.DP ?: 0)
            txtKekuranganBayar.text = formatRupiah.format(kekuranganBayar)
        }

        // Tombol Bayar
        updateButtonVisibility(pesanan.status)
    }

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

    private fun updateButtonVisibility(status: String) {
        when (status) {
            "Menunggu Pembayaran" -> {
                btnBayarSekarang.visibility = View.VISIBLE
                btnBatalkan.visibility = View.VISIBLE
            }
            "Dibayar", "Sedang Diproses", "Selesai" -> {
                btnBayarSekarang.visibility = View.GONE
                btnBatalkan.visibility = View.GONE
            }
            else -> {
                btnBayarSekarang.visibility = View.GONE
                btnBatalkan.visibility = View.VISIBLE
            }
        }
    }

    private fun showPaymentConfirmation(pesanan: Pesanan) {
        val jumlahBayar = pesanan.DP ?: pesanan.totalHarga

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pembayaran")
            .setMessage(
                "Nomor Pesanan: ${pesanan.nomorPesanan}\n" +
                        "Total Pembayaran: ${formatRupiah.format(jumlahBayar)}\n\n" +
                        "Lanjutkan ke pembayaran?"
            )
            .setPositiveButton("Ya") { _, _ ->
                processPembayaran(pesanan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun processPembayaran(pesanan: Pesanan) {
        lifecycleScope.launch {
            try {
                showProgressDialog("Memproses pembayaran...")

                val jumlahBayar = pesanan.DP ?: pesanan.totalHarga

                // Cek apakah sudah ada invoice
                val existingInvoice = checkExistingInvoice(pesanan.idTransaksi)

                if (existingInvoice != null) {
                    // Sudah ada invoice, buka URL nya
                    hideProgressDialog()
                    openPaymentPage(existingInvoice)
                } else {
                    // Buat invoice baru
                    val invoice = XenditHelper.createInvoice(
                        externalId = pesanan.idTransaksi,
                        amount = jumlahBayar.toLong(),
                        payerEmail = null, // Email opsional untuk sandbox
                        description = "Pembayaran Pesanan #${pesanan.nomorPesanan} - Rahmat Bakery",
                        customerName = pesanan.alamat?.nama ?: "Customer",
                        customerPhone = pesanan.alamat?.noHp
                    )

                    if (invoice == null) {
                        hideProgressDialog()
                        showErrorDialog("Gagal membuat invoice pembayaran. Silakan coba lagi.")
                        return@launch
                    }

                    // Simpan invoice ke database
                    saveInvoiceToDatabase(pesanan.idTransaksi, invoice.id, invoice.invoiceUrl, jumlahBayar)

                    hideProgressDialog()
                    openPaymentPage(invoice.invoiceUrl)
                }

            } catch (e: Exception) {
                hideProgressDialog()
                Log.e("RincianPesanan", "Error processing payment", e)
                showErrorDialog("Gagal memproses pembayaran: ${e.message}")
            }
        }
    }

    private suspend fun checkExistingInvoice(transactionId: String): String? {
        return try {
            val response = SupabaseManager.client.postgrest.from("pembayaran")
                .select(columns = Columns.list("invoice_url", "status")) {
                    filter {
                        eq("id_transaksi", transactionId)
                        eq("status", "pending")
                    }
                }
                .decodeSingleOrNull<Map<String, String>>()

            response?.get("invoice_url")
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveInvoiceToDatabase(
        transactionId: String,
        invoiceId: String,
        invoiceUrl: String,
        nominal: Int
    ) {
        try {
            val pembayaranData = mapOf(
                "id_transaksi" to transactionId,
                "nominal" to nominal,
                "metode" to "xendit",
                "status" to "pending",
                "invoice_url" to invoiceUrl
            )

            SupabaseManager.client.postgrest.from("pembayaran").insert(pembayaranData)
        } catch (e: Exception) {
            Log.e("RincianPesanan", "Error saving invoice", e)
        }
    }

    private fun openPaymentPage(invoiceUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran")
            .setMessage(
                "Anda akan diarahkan ke halaman pembayaran Xendit.\n\n" +
                        "Silakan selesaikan pembayaran untuk melanjutkan pesanan."
            )
            .setPositiveButton("Bayar Sekarang") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(invoiceUrl))
                startActivity(intent)

                Toast.makeText(
                    this,
                    "Silakan selesaikan pembayaran di browser",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Nanti") { _, _ ->
                Toast.makeText(
                    this,
                    "Anda dapat membayar kapan saja dari halaman pesanan",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showCancelConfirmation(pesanan: Pesanan) {
        AlertDialog.Builder(this)
            .setTitle("Batalkan Pesanan")
            .setMessage(
                "Apakah Anda yakin ingin membatalkan pesanan ini?\n\n" +
                        "Nomor Pesanan: ${pesanan.nomorPesanan}"
            )
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                cancelOrder(pesanan)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun cancelOrder(pesanan: Pesanan) {
        lifecycleScope.launch {
            try {
                showProgressDialog("Membatalkan pesanan...")

                // Update status transaksi
                SupabaseManager.client.postgrest.from("transaksi")
                    .update(mapOf("status" to "Dibatalkan")) {
                        filter {
                            eq("id_transaksi", pesanan.idTransaksi)
                        }
                    }

                hideProgressDialog()

                AlertDialog.Builder(this@RincianPesananActivity)
                    .setTitle("Pesanan Dibatalkan")
                    .setMessage("Pesanan Anda telah dibatalkan.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()

            } catch (e: Exception) {
                hideProgressDialog()
                showErrorDialog("Gagal membatalkan pesanan: ${e.message}")
            }
        }
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
            Log.e("RincianPesanan", "Error loading pesanan: ${e.message}", e)
            null
        }
    }

    private fun showProgressDialog(message: String) {
        hideProgressDialog()
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}