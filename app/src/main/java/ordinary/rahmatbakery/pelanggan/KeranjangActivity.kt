package ordinary.rahmatbakery.pelanggan

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.KeranjangAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import ordinary.rahmatbakery.pelanggan.model.MenuProduk
import java.text.NumberFormat
import java.util.Locale

class KeranjangActivity : AppCompatActivity() {


    private val listKeranjang = mutableListOf<Keranjang>()
    private lateinit var adapterKeranjang: KeranjangAdapter
    private lateinit var btnHapus: ImageView
    private lateinit var btnCheckout: Button
    private lateinit var txtTotalHarga: TextView
    private lateinit var btnBack : ImageView


    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_keranjang)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keranjang)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnHapus = findViewById(R.id.btn_hapus) // Ganti dengan ID tombol hapus
        btnCheckout = findViewById(R.id.btn_checkout) // Ganti dengan ID tombol checkout
        txtTotalHarga = findViewById(R.id.txt_total_harga_semua) // Ganti dengan ID TextView total


        loadKeranjang()
        setupRecyclerView()

        btnHapus.setOnClickListener {
            val selectedItems = adapterKeranjang.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Anda yakin ingin menghapus ${selectedItems.size} item yang dipilih?")
                    .setPositiveButton("Ya, Hapus") { _, _ ->
                        adapterKeranjang.removeSelectedItems()
                        updateTotalHargaUI()
                        // (Opsional) Lakukan proses hapus di database/API di sini
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                Toast.makeText(this, "Tidak ada item yang dipilih", Toast.LENGTH_SHORT).show()
            }
        }

        btnCheckout.setOnClickListener {
            val selectedItems = adapterKeranjang.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                // Lanjutkan ke proses checkout dengan data 'selectedItems'
                // val intent = Intent(this, CheckoutActivity::class.java)
                // intent.putParcelableArrayListExtra("ITEMS_TO_CHECKOUT", ArrayList(selectedItems))
                // startActivity(intent)
                Toast.makeText(this, "Melanjutkan checkout untuk ${selectedItems.size} item", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Pilih item untuk di-checkout", Toast.LENGTH_SHORT).show()
            }
        }
        btnBack = findViewById(R.id.back)
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rvKeranjang) // Ganti ID
        adapterKeranjang = KeranjangAdapter(listKeranjang)
        recyclerView.adapter = adapterKeranjang

        // Implementasikan listener dari adapter
        adapterKeranjang.setOnItemInteractionListener(object : KeranjangAdapter.OnItemInteractionListener {
            override fun onDataChanged() {
                // Setiap kali ada perubahan (jumlah atau checkbox), hitung ulang total harga
                updateTotalHargaUI()
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    }
    private fun updateTotalHargaUI() {
        // Hitung total harga hanya dari item yang dicentang
        val totalHarga = adapterKeranjang.getSelectedItems().sumOf { it.produk.productPrice * it.jumlah }
        txtTotalHarga.text = "Total Harga: \n${formatRupiah.format(totalHarga)}"
    }

    fun loadKeranjang(){
        lifecycleScope.launch {
            val produk = SupabaseManager.client.from("produk")
                .select(columns = Columns.raw(
                    """id : id_produk,
                productName : nama_produk,
                productImg : foto_produk,
                productPrice : harga""".trimIndent()
                )) // Select all columns, or specify with Columns.list("name", "country_id")
                .decodeList<MenuProduk>()

            if(!produk.isEmpty()){
                var i = 1
                for (menuProduk in produk) {
                    listKeranjang.add(Keranjang("${i++}",menuProduk))
                }
                for (menuProduk in produk) {
                    listKeranjang.add(Keranjang("${i++}",menuProduk))
                }
                adapterKeranjang.notifyDataSetChanged()
            }
        }
    }
}