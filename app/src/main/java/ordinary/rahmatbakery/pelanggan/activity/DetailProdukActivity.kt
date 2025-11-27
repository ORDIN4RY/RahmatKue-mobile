package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Bundle
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
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
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
        val txtHarga: TextView = findViewById(R.id.txt_harga_detail)
        val txtDeskripsi: TextView = findViewById(R.id.txt_deskripsi_detail)
        val rvDetailPaket: RecyclerView = findViewById(R.id.rv_detail_paket)
        val btnBack: ImageView = findViewById(R.id.btn_back_detail)
        val containerBtn: LinearLayout = findViewById(R.id.btn_container)
        val btnAddKeranjang: TextView = findViewById(R.id.btn_add_keranjang)
        val btnCheckOut: TextView = findViewById(R.id.btn_check_out)
        val iconMinus: ImageView = findViewById(R.id.icon_minus)
        itemCounter = findViewById(R.id.input_count)
        val iconPlus: ImageView = findViewById(R.id.icon_plus)
        val txtTotalHarga: TextView = findViewById(R.id.txt_total_harga)




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
                crossfade(true) // animasi lembut saat gambar muncul
                placeholder(R.drawable.placeholder) // opsional: gambar sementara
                error(R.drawable.error_image)       // opsional: jika gagal load
            }
            txtHarga.text = formatRupiah.format(produk.harga)
            rvDetailPaket.visibility = RecyclerView.GONE

        } else if (tipe == "paket" && paket != null) {
            txtNama.text = paket.nama
            txtDeskripsi.text = paket.deskripsi
            imgFoto.load(paket.foto) {
                crossfade(true) // animasi lembut saat gambar muncul
                placeholder(R.drawable.placeholder) // opsional: gambar sementara
                error(R.drawable.error_image)       // opsional: jika gagal load
            }
            txtHarga.text = formatRupiah.format(paket.harga)

            rvDetailPaket.layoutManager = LinearLayoutManager(this)
            rvDetailPaket.adapter = DetailProdukAdapter(paket.detail)
        }

        if (from == "keranjang") {
            containerBtn.visibility = LinearLayout.GONE
            findViewById<LinearLayout>(R.id.counter).visibility = LinearLayout.GONE

        } else if (from == "menu") {
            containerBtn.visibility = LinearLayout.VISIBLE
            findViewById<LinearLayout>(R.id.counter).visibility = LinearLayout.VISIBLE
            btnAddKeranjang.setOnClickListener {
                if (tipe == "produk") {
                    if (itemCounter.text.toString().toInt() < produk!!.kategori.minPembelian) {
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
                        Toast.makeText(
                            this,
                            "Pembelian minimal paket adalah 1 pcs",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    tambahKeranjang(null, paket)
                }

            }
        }

        btnCheckOut.setOnClickListener {
            if (tipe == "produk") {
                if (itemCounter.text.toString().toInt() < produk!!.kategori.minPembelian) {
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
                    Toast.makeText(
                        this,
                        "Pembelian minimal paket adalah 1 pcs",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                langsungCheckOut(null, paket)
            }
        }
        var hargaProduk = 0

        if (tipe == "produk" && produk != null) {
            hargaProduk = produk.harga
        } else if (tipe == "paket" && paket != null) {
            hargaProduk = paket.harga
        }

        fun updateTotal() {
            val jumlah = itemCounter.text.toString().toInt()
            val total = hargaProduk * jumlah
            txtTotalHarga.text = formatRupiah.format(total)
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
                if (currentCount <= produk!!.kategori.minPembelian) {
                    Toast.makeText(this, "Minimal pembelian kategori ${produk.kategori.nama} adalah ${produk.kategori.minPembelian} pcs", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }else{
                if (currentCount <= 1){
                    Toast.makeText(this, "Minimal paket adalah 1 pcs", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            itemCounter.setText((currentCount - 1).toString())
        }


    }

    private fun tambahKeranjang(produk: Produk? = null, paket: Paket? = null) {
        lifecycleScope.launch {
            try {

                val item = KeranjangInsert(
                    idUser = SupabaseManager.client.auth.currentUserOrNull()?.id,
                    idProduk = produk?.id, // Akan menjadi null jika produk null
                    idPaket = paket?.id,     // Akan menjadi null jika paket null
                    jumlah = itemCounter.text.toString().toInt()
                )

                SupabaseManager.client.postgrest.from("keranjang")
                    .insert(item)


                Toast.makeText(
                    this@DetailProdukActivity,
                    "Berhasil menambahkan ke keranjang",
                    Toast.LENGTH_SHORT
                ).show()

                finish()


            } catch (e: Exception) {
                Toast.makeText(
                    this@DetailProdukActivity,
                    "galat: ${e.message}",
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
    }


}
