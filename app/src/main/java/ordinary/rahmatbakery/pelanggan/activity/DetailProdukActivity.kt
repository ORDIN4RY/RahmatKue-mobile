package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.DetailProdukAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import ordinary.rahmatbakery.pelanggan.model.KeranjangInsert
import ordinary.rahmatbakery.pelanggan.model.Paket
import ordinary.rahmatbakery.pelanggan.model.Produk
import java.text.NumberFormat
import java.util.Locale

class DetailProdukActivity : AppCompatActivity() {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    private lateinit var itemCounter: EditText
    private lateinit var txtTotalValue: TextView
    private var hargaProduk = 0
    private var hargaSetelahDiskon = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_produk)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imgFoto: ImageView = findViewById(R.id.img_detail)
        val txtNama: TextView = findViewById(R.id.txt_nama_detail)
        val txtHargaAsli: TextView = findViewById(R.id.txt_harga_asli)
        val txtHarga: TextView = findViewById(R.id.txt_harga_detail)
        val txtHemat: TextView = findViewById(R.id.txt_hemat)
        val badgeDiskon: TextView = findViewById(R.id.badge_diskon)
        val txtDeskripsi: TextView = findViewById(R.id.txt_deskripsi_detail)
        val rvDetailPaket: RecyclerView = findViewById(R.id.rv_detail_paket)
        val titleIsiPaket: TextView = findViewById(R.id.title_isi_paket)
        val btnBack: ImageView = findViewById(R.id.btn_back_detail)
        val containerBtn: LinearLayout = findViewById(R.id.btn_container)
        val btnAddKeranjang: TextView = findViewById(R.id.btn_add_keranjang)
        val btnCheckOut: TextView = findViewById(R.id.btn_check_out)
        val iconMinus: ImageView = findViewById(R.id.icon_minus)
        val iconPlus: ImageView = findViewById(R.id.icon_plus)
        itemCounter = findViewById(R.id.input_count)
        txtTotalValue = findViewById(R.id.txt_total_value)

        btnBack.setOnClickListener { finish() }

        // Ambil data dari intent
        val tipe = intent.getStringExtra("TIPE")
        val produk = intent.getParcelableExtra<Produk>("PRODUK")
        val paket = intent.getParcelableExtra<Paket>("PAKET")
        val from = intent.getStringExtra("FROM")

        if (tipe == "produk" && produk != null) {
            txtNama.text = produk.nama
            txtDeskripsi.text = produk.deskripsi
            imgFoto.load(produk.gambar) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.error_image)
            }

            hargaProduk = produk.harga
            hargaSetelahDiskon = produk.harga

            // Handle diskon
            var persenDiskon = 0
            var nominalHemat = 0

            if (produk.tipe_diskon != null && produk.diskon != null) {
                if (produk.tipe_diskon == "persen") {
                    persenDiskon = produk.diskon
                    hargaSetelahDiskon = produk.harga - (produk.harga * produk.diskon / 100)
                    nominalHemat = produk.harga - hargaSetelahDiskon
                } else {
                    hargaSetelahDiskon = produk.harga - produk.diskon
                    nominalHemat = produk.diskon
                    persenDiskon = ((produk.diskon.toFloat() / produk.harga.toFloat()) * 100).toInt()
                }

                // Show discount elements
                badgeDiskon.visibility = View.VISIBLE
                badgeDiskon.text = "-${persenDiskon}%"

                txtHargaAsli.visibility = View.VISIBLE
                txtHargaAsli.text = formatRupiah.format(produk.harga)
                txtHargaAsli.paintFlags = txtHargaAsli.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                txtHarga.text = formatRupiah.format(hargaSetelahDiskon)

                txtHemat.visibility = View.VISIBLE
                txtHemat.text = "Hemat ${formatRupiah.format(nominalHemat)}"
            } else {
                txtHarga.text = formatRupiah.format(produk.harga)
            }

            rvDetailPaket.visibility = View.GONE
            titleIsiPaket.visibility = View.GONE
            itemCounter.setText(produk.kategori.minPembelian.toString())

        } else if (tipe == "paket" && paket != null) {
            txtNama.text = paket.nama
            txtDeskripsi.text = paket.deskripsi
            imgFoto.load(paket.foto) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.error_image)
            }

            hargaProduk = paket.harga
            hargaSetelahDiskon = paket.harga

            // Handle diskon
            var persenDiskon = 0
            var nominalHemat = 0

            if (paket.tipe_diskon != null && paket.diskon != null) {
                if (paket.tipe_diskon == "persen") {
                    persenDiskon = paket.diskon
                    hargaSetelahDiskon = paket.harga - (paket.harga * paket.diskon / 100)
                    nominalHemat = paket.harga - hargaSetelahDiskon
                } else {
                    hargaSetelahDiskon = paket.harga - paket.diskon
                    nominalHemat = paket.diskon
                    persenDiskon = ((paket.diskon.toFloat() / paket.harga.toFloat()) * 100).toInt()
                }

                // Show discount elements
                badgeDiskon.visibility = View.VISIBLE
                badgeDiskon.text = "-${persenDiskon}%"

                txtHargaAsli.visibility = View.VISIBLE
                txtHargaAsli.text = formatRupiah.format(paket.harga)
                txtHargaAsli.paintFlags = txtHargaAsli.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                txtHarga.text = formatRupiah.format(hargaSetelahDiskon)

                txtHemat.visibility = View.VISIBLE
                txtHemat.text = "Hemat ${formatRupiah.format(nominalHemat)}"
            } else {
                txtHarga.text = formatRupiah.format(paket.harga)
            }

            rvDetailPaket.visibility = View.VISIBLE
            titleIsiPaket.visibility = View.VISIBLE
            rvDetailPaket.layoutManager = LinearLayoutManager(this)
            rvDetailPaket.adapter = DetailProdukAdapter(paket.detail)
            itemCounter.setText("1")
        }

        if (from == "keranjang") {
            containerBtn.visibility = View.GONE
            findViewById<LinearLayout>(R.id.counter).visibility = View.GONE
        } else if (from == "menu") {
            containerBtn.visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.counter).visibility = View.VISIBLE

            btnAddKeranjang.setOnClickListener {
                if (tipe == "produk") {
                    if (itemCounter.text.toString().toInt() < produk!!.kategori.minPembelian!!) {
                        Toast.makeText(
                            this,
                            "Pembelian minimal ${produk.kategori.nama} adalah ${produk.kategori.minPembelian} pcs",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    tambahKeranjang(produk, null)
                } else if (tipe == "paket") {
                    if (itemCounter.text.toString().toInt() < 1) {
                        Toast.makeText(this, "Pembelian minimal paket adalah 1 pcs", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    tambahKeranjang(null, paket)
                }
            }
        }

        btnCheckOut.setOnClickListener {
            if (tipe == "produk") {
                if (itemCounter.text.toString().toInt() < produk!!.kategori.minPembelian!!) {
                    Toast.makeText(
                        this,
                        "Pembelian minimal ${produk.kategori.nama} adalah ${produk.kategori.minPembelian} pcs",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                langsungCheckOut(produk, null)
            } else if (tipe == "paket") {
                if (itemCounter.text.toString().toInt() < 1) {
                    Toast.makeText(this, "Pembelian minimal paket adalah 1 pcs", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                langsungCheckOut(null, paket)
            }
        }

        updateTotal()

        iconPlus.setOnClickListener {
            val currentCount = itemCounter.text.toString().toInt()
            itemCounter.setText((currentCount + 1).toString())
            updateTotal()
        }

        iconMinus.setOnClickListener {
            val currentCount = itemCounter.text.toString().toInt()

            if (tipe == "produk") {
                if (currentCount <= produk!!.kategori.minPembelian!!) {
                    Toast.makeText(
                        this,
                        "Minimal pembelian ${produk.kategori.nama} adalah ${produk.kategori.minPembelian} pcs",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            } else {
                if (currentCount <= 1) {
                    Toast.makeText(this, "Minimal paket adalah 1 pcs", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            itemCounter.setText((currentCount - 1).toString())
            updateTotal()
        }

        itemCounter.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (itemCounter.text.toString() == "") {
                    itemCounter.setText("1")
                }
                updateTotal()
            }
        })
    }

    private fun updateTotal() {
        try {
            val jumlahString = itemCounter.text.toString()
            val jumlah = if (jumlahString.isBlank()) 0 else jumlahString.toInt()
            val total = hargaSetelahDiskon * jumlah
            txtTotalValue.text = formatRupiah.format(total)
        } catch (e: NumberFormatException) {
            txtTotalValue.text = formatRupiah.format(0)
        }
    }

    private fun tambahKeranjang(produk: Produk? = null, paket: Paket? = null) {
        lifecycleScope.launch {
            try {
                val item = KeranjangInsert(
                    idUser = SupabaseManager.client.auth.currentUserOrNull()?.id,
                    idProduk = produk?.id,
                    idPaket = paket?.id,
                    jumlah = itemCounter.text.toString().toInt()
                )

                SupabaseManager.client.postgrest.from("keranjang").insert(item)

                Toast.makeText(
                    this@DetailProdukActivity,
                    "Berhasil menambahkan ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailProdukActivity,
                    "Gagal: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun langsungCheckOut(produk: Produk? = null, paket: Paket? = null) {
        val itemLangsungCheckout = listOf(
            Keranjang(
                id = java.util.UUID.randomUUID().toString(),
                jumlah = itemCounter.text.toString().toInt(),
                tipe = if (produk != null) "produk" else "paket",
                produk = produk,
                paket = paket,
                terpilih = true
            )
        )

        val intent = Intent(this, CheckoutActivity::class.java)
        val selectedItemsJson = Json.encodeToString(itemLangsungCheckout)
        intent.putExtra("KERANJANG_JSON", selectedItemsJson)
        startActivity(intent)
        finish()
    }
}