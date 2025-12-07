package ordinary.rahmatbakery.pelanggan.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from

import android.app.DatePickerDialog
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.Calendar

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.PesananCheckoutAdapter
import ordinary.rahmatbakery.pelanggan.model.*
import ordinary.rahmatbakery.util.AuthRepository
import java.text.NumberFormat
import java.util.Locale

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import coil.load
import ordinary.rahmatbakery.util.OrderStatus
import ordinary.rahmatbakery.util.hitungJarak
import java.util.ArrayList
import kotlin.math.ceil

import android.app.ProgressDialog
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import ordinary.rahmatbakery.model.Profile
import ordinary.rahmatbakery.pelanggan.helper.XenditHelper
import java.util.UUID

class CheckoutActivity(
    // Menggunakan AuthRepository untuk mendapatkan data user yang sedang login
    private val repo: AuthRepository = AuthRepository()
) : AppCompatActivity() {

    // Deklarasi semua komponen UI
    private lateinit var btnBack: ImageView
    private lateinit var cbDp: CheckBox
    private lateinit var cbLunas: CheckBox
    private lateinit var btnPesan: Button
    private lateinit var rvPesanan: RecyclerView
    private lateinit var cbAntar: CheckBox
    private lateinit var cbAmbil: CheckBox
    private lateinit var tvSubtotalPesanan: TextView
    private lateinit var tvTotalDiskon: TextView
    private lateinit var tvSubtotalPengiriman: TextView
    private lateinit var tvTotalPembayaran: TextView
    private lateinit var layoutAlamat: RelativeLayout
    private lateinit var layoutAlamatKosong: RelativeLayout
    private lateinit var tvNamaPelanggan: TextView
    private lateinit var tvNoHpPelanggan: TextView
    private lateinit var tvAlamatPelanggan: TextView
    private lateinit var etDate: EditText
    private lateinit var cardAlamat: CardView
    private lateinit var voucherCard: CardView
    private lateinit var gambarVoucher: ImageView
    private lateinit var namaVoucher: TextView
    private lateinit var deskripsiVoucher: TextView
    private lateinit var etCatatan: EditText


    // Variabel untuk menyimpan data dan state
    private var alamat: Alamat? = null
    private var voucher: Voucher? = null

    private var metodePembayaran = "Lunas"
    private var metodePengiriman = "diambil"

    private var subtotalPesanan = 0
    private var totalDiskon = 0
    private var subtotalPengiriman = 0
    private var totalPembayaran = 0
    private var dpMinimal = 0
    private var hargaOngkir = 0
    private var totalPotongan = 0
    private var kategoriPesananList: ArrayList<String?> = arrayListOf()

    private var selectedFinishDate: Calendar? = null

    private var keranjangTerpilih: List<Keranjang> = emptyList()

    private var progressDialog: ProgressDialog? = null
    private var currentTransactionId: String? = null


    private val selectAlamatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Cek apakah hasilnya OK dan ada data yang dikirim kembali
        if (result.resultCode == RESULT_OK) {
            // Ambil objek Alamat yang dipilih dari intent
            val selectedAlamat = result.data?.getParcelableExtra<Alamat>("SELECTED_ALAMAT")
            if (selectedAlamat != null) {
                this.alamat = selectedAlamat // Update variabel alamat di activity ini
                alamatAktif() // Panggil fungsi untuk me-refresh UI alamat
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi semua view dan listener
        initializeViews()
        setupListeners()

        // Memproses data keranjang dari intent
        if (ambilDataKeranjang()) {
            // Jika data valid, lanjutkan memuat data lain dan setup UI
            ambilAlamat()
            setupRecyclerView()
            calculateTotals() // Lakukan perhitungan awal
        }
    }

    private val pickVoucherLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                voucher = data.getParcelableExtra("VOUCHER")
                calculateTotals()
                updateTotalsUI()
            }
        }
    }

    private fun initializeViews() {
        rvPesanan = findViewById(R.id.rvPesanan)
        btnBack = findViewById(R.id.back)
        cbDp = findViewById(R.id.checkbox_dp)
        cbLunas = findViewById(R.id.checkbox_lunas)
        btnPesan = findViewById(R.id.btn_pesan)
        cbAntar = findViewById(R.id.cb_antar)
        cbAmbil = findViewById(R.id.cb_ambil)
        tvSubtotalPesanan = findViewById(R.id.subtotal_pesanan)
        tvTotalDiskon = findViewById(R.id.potongan_harga)
        tvSubtotalPengiriman = findViewById(R.id.subtotal_pengiriman)
        tvTotalPembayaran = findViewById(R.id.total_pembayaran)
        etDate = findViewById(R.id.etDate)
        cardAlamat = findViewById(R.id.card_alamat)
        voucherCard = findViewById(R.id.voucher_card)
        gambarVoucher = findViewById(R.id.gambar_voucher)
        namaVoucher = findViewById(R.id.nama_voucher)
        deskripsiVoucher = findViewById(R.id.deskripsi_voucher)
        etCatatan = findViewById(R.id.et_catatan)

        // View untuk alamat
        layoutAlamat = findViewById(R.id.data_pelanggan)
        layoutAlamatKosong = findViewById(R.id.alamat_kosong)
        tvNamaPelanggan = findViewById(R.id.nama_pelanggan)
        tvNoHpPelanggan = findViewById(R.id.no_hp_pelanggan)
        tvAlamatPelanggan = findViewById(R.id.alamat_pelanggan)

        // Set state default saat activity dibuka
        cbAmbil.isChecked = true
        cbLunas.isChecked = true
    }

    private fun ambilDataKeranjang(): Boolean {
        val selectedItemsJson = intent.getStringExtra("KERANJANG_JSON")
        if (selectedItemsJson == null) {
            Toast.makeText(this, "Tidak ada data keranjang", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        try {
            // Decode string JSON menjadi List<Keranjang>
            keranjangTerpilih = Json.decodeFromString(selectedItemsJson)
            keranjangTerpilih.forEach { item ->
                if (item.produk != null) {
                    kategoriPesananList += item.produk.kategori.nama
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error memproses data keranjang", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        if (keranjangTerpilih.isEmpty()) {
            Toast.makeText(this, "Tidak ada item untuk di-checkout", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        return true
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        cbAntar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbAmbil.isChecked = false
                metodePengiriman = "diantar"
                if (hargaOngkir <= 0) {
                    hitungOngkir()
                }
                subtotalPengiriman = hargaOngkir
            } else if (!cbAmbil.isChecked) {
                cbAntar.isChecked = true
                if(voucher?.jenis_voucher == "ongkir"){
                    voucher = null
                }
            }
            calculateTotals()
            updateTotalsUI()
        }

        cbAmbil.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbAntar.isChecked = false
                metodePengiriman = "diambil"
                subtotalPengiriman = 0
                voucher == null
            } else if (!cbAntar.isChecked) {
                cbAmbil.isChecked = true
            }
            calculateTotals()
            updateTotalsUI()
        }

        cbLunas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbDp.isChecked = false
                metodePembayaran = "Lunas"
            } else if (!cbDp.isChecked) {
                cbLunas.isChecked = true
            }
            calculateTotals()
        }

        cbDp.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cbLunas.isChecked = false
                metodePembayaran = "DP"
            } else if (!cbLunas.isChecked) {
                cbDp.isChecked = true
            }
            calculateTotals()
        }

        btnPesan.setOnClickListener {
//            val intent = Intent(this@CheckoutActivity, PembayaranQrisActivity::class.java)
//            startActivity(intent)
            submitPesanan()
        }

        etDate.setOnClickListener {
            showDatePickerDialog()
        }

        cardAlamat.setOnClickListener {
            val intent = Intent(this, AlamatActivity::class.java)
            // Kirim extra untuk menandakan kita masuk ke mode seleksi
            intent.putExtra("SELECTION_MODE", true)
            selectAlamatLauncher.launch(intent)
        }

        voucherCard.setOnClickListener {
            openVoucherSelection()
        }
    }

    private fun showDatePickerDialog() {
        // 1. Dapatkan tanggal hari ini sebagai default
        val calendar = Calendar.getInstance()

        // 2. Buat listener untuk menangani tanggal yang dipilih
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            // Simpan tanggal yang dipilih
            selectedFinishDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }

            // Format tanggal agar mudah dibaca (contoh: 25 Mei 2024)
            val format = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            etDate.setText(format.format(selectedFinishDate!!.time))
        }

        // 3. Buat DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this@CheckoutActivity,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // 4. ATUR TANGGAL MINIMUM (KUNCI UTAMA)
        // Buat instance Calendar baru untuk tanggal minimum
        val minDate = Calendar.getInstance()
        // Tambahkan 2 hari dari hari ini
        minDate.add(Calendar.DAY_OF_YEAR, 2)
        // Atur tanggal minimum pada DatePicker, dalam milidetik
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        // 5. Tampilkan dialog
        datePickerDialog.show()
    }

    private fun calculateTotals() {
        subtotalPesanan = 0
        totalDiskon = 0

        keranjangTerpilih.forEach { item ->
            val hargaAsli = item.produk?.harga ?: item.paket?.harga ?: 0
            val diskonPersen = item.produk?.diskon ?: 0

            subtotalPesanan += hargaAsli * item.jumlah
            if (diskonPersen > 0) {
                totalDiskon += (hargaAsli * diskonPersen / 100) * item.jumlah
            }
        }

        if (voucher != null) {
            if (voucher!!.jenis_voucher == "diskon") {
                val potongan = subtotalPesanan * voucher!!.maksimal_potongan!! / 100
                if (voucher!!.maksimal_potongan!! > subtotalPesanan || voucher!!.maksimal_potongan == 0) {
                    totalDiskon += potongan
                } else {
                    totalDiskon += voucher!!.maksimal_potongan!!
                }
            } else if (voucher!!.jenis_voucher == "ongkir") {
                val potonganOngkir = hargaOngkir * voucher!!.maksimal_potongan!! / 100
                if (voucher!!.maksimal_potongan!! > hargaOngkir || voucher!!.maksimal_potongan == 0) {
                    subtotalPengiriman -= potonganOngkir
                } else {
                    subtotalPengiriman -= voucher!!.maksimal_potongan!!
                }
                if(subtotalPengiriman < 0){
                    subtotalPengiriman = 0
                }
            }
        }

        totalPembayaran = subtotalPesanan - totalDiskon + subtotalPengiriman

        // HITUNG DP 50%
        if (cbLunas.isChecked) {
            dpMinimal = totalPembayaran
        } else {
            dpMinimal = (totalPembayaran / 2)
        }

        updateTotalsUI()
    }

    private fun updateTotalsUI() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        tvSubtotalPesanan.text = formatter.format(subtotalPesanan)
        tvTotalDiskon.text = "- ${formatter.format(totalDiskon)}"
        // Tampilkan Teks Diskon hanya jika ada diskon
        tvTotalDiskon.visibility = if (totalDiskon > 0) View.VISIBLE else View.GONE
        findViewById<RelativeLayout>(R.id.baris_diskon).visibility =
            if (totalDiskon > 0) View.VISIBLE else View.GONE
        val textOngkir = if (metodePengiriman == "diantar") hargaOngkir else subtotalPengiriman

        tvSubtotalPengiriman.text = formatter.format(textOngkir)
        tvTotalPembayaran.text = formatter.format(totalPembayaran)
        findViewById<TextView>(R.id.total_pembayaran_final).text = formatter.format(dpMinimal)


        if (voucher != null) {
            gambarVoucher.load(voucher!!.foto_voucher) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.error_image)
            }
            namaVoucher.text = voucher!!.nama_voucher
            deskripsiVoucher.text = voucher!!.deskripsi

            findViewById<RelativeLayout>(R.id.voucher_on).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.voucher_off).visibility = View.GONE
        } else {
            findViewById<RelativeLayout>(R.id.voucher_on).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.voucher_off).visibility = View.VISIBLE
        }

    }

//    private fun submitPesanan() {
//        lifecycleScope.launch {
//            // PERUBAHAN VALIDASI: Alamat selalu dibutuhkan
//            if (alamat == null) {
//                Toast.makeText(
//                    this@CheckoutActivity,
//                    "Alamat diperlukan untuk semua pesanan.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            if (selectedFinishDate == null) {
//                Toast.makeText(
//                    this@CheckoutActivity,
//                    "Silakan pilih tanggal selesai pesanan.",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//
//            val tanggalSelesaiISO =
//                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
//                    selectedFinishDate!!.time
//                )
//
//            val currentUser = repo.getCurrentProfile()
//            if (currentUser == null) {
//                Toast.makeText(
//                    this@CheckoutActivity,
//                    "Sesi tidak valid, silakan login ulang",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@launch
//            }
//
//            // PERUBAHAN: Menggunakan status awal yang konsisten
//            val statusAwal = OrderStatus.MENUNGGU_PEMBAYARAN
//
//            // PERUBAHAN: Siapkan data transaksi dengan dp_minimal
//            val transaksiData = TransaksiInsert(
//                idUser = currentUser.id,
//                idAlamat = alamat!!.id!!,
//                totalHarga = totalPembayaran,
//                dpMinimal = dpMinimal,
//                status = statusAwal,
//                idVoucher = voucher?.id_voucher,
//                metodePengiriman = metodePengiriman,
//                waktuSelesai = tanggalSelesaiISO,
//                catatan = etCatatan.text.toString().trim(),
//                potongan = totalDiskon,
//                ongkir = subtotalPengiriman
//            )
//
//            try {
//                // Insert ke tabel 'transaksi'
//                val transaksiBaru = SupabaseManager.client.from("transaksi")
//                    .insert(transaksiData) { select() }
//                    .decodeSingle<Map<String, JsonElement>>()
//                val idTransaksiBaru = transaksiBaru["id_transaksi"]?.jsonPrimitive?.content
//                    ?: throw IllegalStateException("Gagal mendapatkan ID transaksi baru dari respons")
//
//                // Siapkan dan insert detail produk
//                val detailProdukList = keranjangTerpilih.filter { it.produk != null }.map { item ->
//                    val hargaAsli = item.produk!!.harga
//                    val diskon = item.produk.diskon ?: 0
//                    val subtotal = (hargaAsli - (hargaAsli * diskon / 100)) * item.jumlah
//                    DetailTransaksiProdukInsert(
//                        idTransaksiBaru,
//                        item.produk.id,
//                        item.jumlah,
//                        subtotal
//                    )
//                }
//                if (detailProdukList.isNotEmpty()) {
//                    SupabaseManager.client.from("detail_transaksi_produk").insert(detailProdukList)
//                }
//
//                // Siapkan dan insert detail paket
//                val detailPaketList = keranjangTerpilih.filter { it.paket != null }.map { item ->
//                    val subtotal = item.paket!!.harga * item.jumlah
//                    DetailTransaksiPaketInsert(
//                        idTransaksiBaru,
//                        item.paket.id!!,
//                        item.jumlah,
//                        subtotal
//                    )
//                }
//                if (detailPaketList.isNotEmpty()) {
//                    SupabaseManager.client.from("detail_transaksi_paket").insert(detailPaketList)
//                }
//
//                // Hapus dari keranjang
//                val idItemKeranjang = keranjangTerpilih.mapNotNull { it.id }
//                if (idItemKeranjang.isNotEmpty()) {
//                    SupabaseManager.client.from("keranjang")
//                        .delete { filter { isIn("id_keranjang", idItemKeranjang) } }
//                }
//
//                setResult(RESULT_OK)
//                Toast.makeText(this@CheckoutActivity, "Pesanan berhasil dibuat!", Toast.LENGTH_LONG)
//                    .show()
//                finish()
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(
//                    this@CheckoutActivity,
//                    "Gagal membuat pesanan: ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//                tvAlamatPelanggan.text = e.message
//            }
//        }
//    }


    private fun submitPesanan() {
        lifecycleScope.launch {
            // Validasi alamat
            if (alamat == null) {
                Toast.makeText(
                    this@CheckoutActivity,
                    "Alamat diperlukan untuk semua pesanan.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Validasi tanggal selesai
            if (selectedFinishDate == null) {
                Toast.makeText(
                    this@CheckoutActivity,
                    "Silakan pilih tanggal selesai pesanan.",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Validasi user
            val currentUser = repo.getCurrentProfile()
            if (currentUser == null) {
                Toast.makeText(
                    this@CheckoutActivity,
                    "Sesi tidak valid, silakan login ulang",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            // Konfirmasi pesanan
            showConfirmationDialog(currentUser)
        }
    }

    private fun ambilAlamat() {
        lifecycleScope.launch {
            val currentUser = repo.getCurrentProfile() ?: return@launch
            try {
                val alamatUtama = SupabaseManager.client.from("alamat")
                    .select { filter { eq("id_user", currentUser.id); eq("alamat_utama", true) } }
                    .decodeSingleOrNull<Alamat>()

                if (alamatUtama != null) {
                    alamat = alamatUtama
                    alamatAktif()
                } else {
                    alamat = null
                    alamatNonaktif()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                alamatNonaktif()
            }
        }
    }

    private fun alamatAktif() {
        layoutAlamat.visibility = View.VISIBLE
        layoutAlamatKosong.visibility = View.GONE
        tvNamaPelanggan.text = alamat?.nama
        tvNoHpPelanggan.text = alamat?.noHp
        tvAlamatPelanggan.text = alamat?.alamat


        hitungOngkir()
    }

    private fun alamatNonaktif() {
        layoutAlamat.visibility = View.GONE
        layoutAlamatKosong.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        rvPesanan.layoutManager = LinearLayoutManager(this)
        val pesananAdapter = PesananCheckoutAdapter(keranjangTerpilih)
        rvPesanan.adapter = pesananAdapter
    }

    private fun hitungOngkir() {
        if (alamat == null || alamat?.latitude == null || alamat?.longitude == null) {
            // Jika tidak ada alamat atau koordinat, pastikan ongkir 0 dan update UI
            hargaOngkir = 0
            if (cbAntar.isChecked) {
                subtotalPengiriman = 0
                calculateTotals() // Update UI untuk menampilkan ongkir Rp0
            }
            return
        }

        hitungJarak(
            alamat!!.latitude!!,
            alamat!!.longitude!!,
        ) { route, error ->

            runOnUiThread {
                if (error != null) {
                    println("Error menghitung jarak: $error")
                    Toast.makeText(this, "Gagal menghitung ongkir.", Toast.LENGTH_SHORT).show()
                    hargaOngkir = 0
                } else if (route != null) {
                    // ongkir dihitung setelah 10km, dan tiap +1 km nambah seribu
                    var ongkirSementara = 0
                    val jarak = route.distanceKm
                    if (jarak >= 10) {
                        val lebihKm = ceil(jarak - 10)
                        // Contoh: Jarak 12.3km -> lebihKm = 3.0 -> ongkir = 10000 + 3000 = 13000
                        ongkirSementara = (lebihKm * 1000).toInt()
                    }

                    println("Jarak: $jarak km, Ongkir dihitung: $ongkirSementara")
                    hargaOngkir = ongkirSementara
                }

                if (metodePengiriman == "diantar") {
                    subtotalPengiriman = hargaOngkir
                    // Panggil kembali calculateTotals() untuk menghitung ulang semuanya
                    // dan memperbarui UI dengan nilai ongkir yang baru.
                    calculateTotals()
                }
            }

        }
    }

    private fun openVoucherSelection() {
        val intent = Intent(this, PilihVoucher::class.java)
        intent.putExtra("metode_pengambilan", metodePengiriman)
        intent.putExtra("subtotal_pesanan", subtotalPesanan.toLong())
        intent.putStringArrayListExtra("kategori_pesanan", kategoriPesananList)

        pickVoucherLauncher.launch(intent)
    }

    private fun showConfirmationDialog(currentUser: Profile) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0

        val metodePembayaranText = if (metodePembayaran == "DP") "DP (50%)" else "Lunas"
        val metodePengirimanText = if (metodePengiriman == "diantar") "Diantar" else "Diambil"

        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Pesanan")
            .setMessage(
                "Total Pembayaran: ${formatter.format(dpMinimal)}\n" +
                        "Metode: $metodePembayaranText\n" +
                        "Pengambilan: $metodePengirimanText\n\n" +
                        "Lanjutkan ke pembayaran?"
            )
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    prosesCheckoutDanPembayaran(currentUser)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    /**
     * Proses checkout dan pembayaran dengan Xendit
     */
    private suspend fun prosesCheckoutDanPembayaran(currentUser: Profile) {
        try {
            showProgressDialog("Membuat pesanan...")

            // 1. Buat transaksi di database
            val idTransaksiBaru = createTransaction(currentUser)
            currentTransactionId = idTransaksiBaru

            // 2. Buat invoice Xendit
            showProgressDialog("Memproses pembayaran...")

            val nomorPesanan = generateOrderNumber()
            val catatan = etCatatan.text.toString().trim()

            val invoice = XenditHelper.createInvoice(
                externalId = idTransaksiBaru,
                amount = dpMinimal.toLong(),
                payerEmail = currentUser.email, // Bisa null untuk sandbox
                description = "Pembayaran Pesanan #$nomorPesanan - Rahmat Bakery",
                customerName = alamat?.nama ?: currentUser.username ?: "Customer",
                customerPhone = alamat?.noHp
            )

            if (invoice == null) {
                hideProgressDialog()
                showErrorDialog("Gagal membuat invoice pembayaran. Silakan coba lagi.")
                // Rollback: Hapus transaksi yang sudah dibuat
                rollbackTransaction(idTransaksiBaru)
                return
            }

            // 3. Simpan data pembayaran
            savePembayaran(idTransaksiBaru, invoice.id, invoice.invoiceUrl)

            // 4. Update nomor pesanan
            updateNomorPesanan(idTransaksiBaru, nomorPesanan)

            // 5. Hapus dari keranjang
            deleteFromKeranjang()

            hideProgressDialog()

            // 6. Buka halaman pembayaran Xendit
            openPaymentPage(invoice.invoiceUrl, nomorPesanan)

        } catch (e: Exception) {
            hideProgressDialog()
            e.printStackTrace()
            showErrorDialog("Gagal membuat pesanan: ${e.message}")
        }
    }

    /**
     * Buat transaksi di database
     */
    private suspend fun createTransaction(currentUser: Profile): String {
        val tanggalSelesaiISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            .format(selectedFinishDate!!.time)

        val transaksiData = TransaksiInsert(
            idUser = currentUser.id,
            idAlamat = alamat!!.id!!,
            totalHarga = totalPembayaran,
            dpMinimal = dpMinimal,
            status = OrderStatus.MENUNGGU_PEMBAYARAN,
            idVoucher = voucher?.id_voucher,
            metodePengiriman = metodePengiriman,
            waktuSelesai = tanggalSelesaiISO,
            catatan = etCatatan.text.toString().trim(),
            potongan = totalDiskon,
            ongkir = subtotalPengiriman
        )

        // Insert transaksi
        val transaksiBaru = SupabaseManager.client.from("transaksi")
            .insert(transaksiData) { select() }
            .decodeSingle<Map<String, JsonElement>>()

        val idTransaksiBaru = transaksiBaru["id_transaksi"]?.jsonPrimitive?.content
            ?: throw IllegalStateException("Gagal mendapatkan ID transaksi")

        // Insert detail produk
        val detailProdukList = keranjangTerpilih.filter { it.produk != null }.map { item ->
            val hargaAsli = item.produk!!.harga
            val diskon = item.produk.diskon ?: 0
            val subtotal = (hargaAsli - (hargaAsli * diskon / 100)) * item.jumlah
            DetailTransaksiProdukInsert(
                idTransaksiBaru,
                item.produk.id,
                item.jumlah,
                subtotal
            )
        }
        if (detailProdukList.isNotEmpty()) {
            SupabaseManager.client.from("detail_transaksi_produk").insert(detailProdukList)
        }

        // Insert detail paket
        val detailPaketList = keranjangTerpilih.filter { it.paket != null }.map { item ->
            val subtotal = item.paket!!.harga * item.jumlah
            DetailTransaksiPaketInsert(
                idTransaksiBaru,
                item.paket.id!!,
                item.jumlah,
                subtotal
            )
        }
        if (detailPaketList.isNotEmpty()) {
            SupabaseManager.client.from("detail_transaksi_paket").insert(detailPaketList)
        }

        return idTransaksiBaru
    }

    /**
     * Simpan data pembayaran ke database
     */
    private suspend fun savePembayaran(
        transactionId: String,
        invoiceId: String,
        invoiceUrl: String
    ) {
        val pembayaranData = PembayaranInsert(
            idTransaksi = transactionId,
            nominal = dpMinimal,
            metode = "xendit",
            status = "pending",
            invoiceUrl = invoiceUrl
        )

        SupabaseManager.client.from("pembayaran").insert(pembayaranData)
    }

    /**
     * Update nomor pesanan
     */
    private suspend fun updateNomorPesanan(transactionId: String, nomorPesanan: String) {
        SupabaseManager.client.from("transaksi")
            .update(mapOf("nomor_pesanan" to nomorPesanan)) {
                filter {
                    eq("id_transaksi", transactionId)
                }
            }
    }

    /**
     * Hapus item dari keranjang
     */
    private suspend fun deleteFromKeranjang() {
        val idItemKeranjang = keranjangTerpilih.mapNotNull { it.id }
        if (idItemKeranjang.isNotEmpty()) {
            SupabaseManager.client.from("keranjang")
                .delete { filter { isIn("id_keranjang", idItemKeranjang) } }
        }
    }

    /**
     * Rollback transaksi jika pembayaran gagal
     */
    private suspend fun rollbackTransaction(transactionId: String) {
        try {
            SupabaseManager.client.from("transaksi")
                .delete { filter { eq("id_transaksi", transactionId) } }
        } catch (e: Exception) {
            Log.e("CheckoutActivity", "Failed to rollback transaction", e)
        }
    }

    /**
     * Buka halaman pembayaran Xendit
     */
    private fun openPaymentPage(invoiceUrl: String, nomorPesanan: String) {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran Siap")
            .setMessage(
                "Pesanan #$nomorPesanan berhasil dibuat!\n\n" +
                        "Anda akan diarahkan ke halaman pembayaran Xendit. " +
                        "Silakan selesaikan pembayaran dalam 24 jam."
            )
            .setPositiveButton("Bayar Sekarang") { _, _ ->
                // Buka browser dengan URL invoice Xendit
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(invoiceUrl))
                startActivity(intent)

                // Tutup CheckoutActivity dan kembali ke home
                setResult(RESULT_OK)
                finish()

                Toast.makeText(
                    this,
                    "Silakan selesaikan pembayaran di browser",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Nanti") { _, _ ->
                setResult(RESULT_OK)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Generate nomor pesanan unik
     */
    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        return "RB${timestamp.toString().takeLast(8)}"
    }

    /**
     * Tampilkan progress dialog
     */
    private fun showProgressDialog(message: String) {
        hideProgressDialog()
        progressDialog = ProgressDialog(this).apply {
            setMessage(message)
            setCancelable(false)
            show()
        }
    }

    /**
     * Sembunyikan progress dialog
     */
    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    /**
     * Tampilkan error dialog
     */
    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}