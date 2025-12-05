package ordinary.rahmatbakery.pelanggan.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc

import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.KeranjangAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import java.text.NumberFormat
import java.util.Locale

class KeranjangActivity : AppCompatActivity() {

    private val listKeranjang = mutableListOf<Keranjang>()
    private lateinit var adapterKeranjang: KeranjangAdapter
    private lateinit var btnHapus: ImageView
    private lateinit var btnCheckout: Button
    private lateinit var txtTotalHarga: TextView
    private lateinit var btnBack: ImageView
    private lateinit var cbPilihSemua: CheckBox
    private var isUpdatingSelectAll = false

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
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

        btnHapus = findViewById(R.id.btn_hapus)
        btnCheckout = findViewById(R.id.btn_checkout)
        txtTotalHarga = findViewById(R.id.txt_total_harga_semua)
        btnBack = findViewById(R.id.back)
        cbPilihSemua = findViewById(R.id.cb_pilih_semua)


        setupRecyclerView()
        loadKeranjang()

        btnBack.setOnClickListener { finish() }

        cbPilihSemua.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingSelectAll) return@setOnCheckedChangeListener

            listKeranjang.forEach { it.terpilih = isChecked }
            adapterKeranjang.notifyDataSetChanged()
            updateTotalHargaUI()
        }

        btnHapus.setOnClickListener {
            val selected = adapterKeranjang.getSelectedItems()
            if (selected.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Hapus ${selected.size} item yang dipilih?")
                    .setPositiveButton("Ya") { _, _ ->
                        lifecycleScope.launch {
                            selected.forEach {
                                SupabaseManager.client.from("keranjang")
                                    .delete {
                                        filter {
                                            eq("id_keranjang", it.id)
                                        }
                                    }
                            }
                            adapterKeranjang.removeSelectedItems()
                            updateTotalHargaUI()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else Toast.makeText(this, "Belum ada item dipilih", Toast.LENGTH_SHORT).show()
        }


        btnCheckout.setOnClickListener {
            val selected = adapterKeranjang.getSelectedItems()
            if (selected.isEmpty()) {
                Toast.makeText(this, "Pilih item untuk checkout", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
         val intent = Intent(this, CheckoutActivity::class.java)
            val selectedItemsJson = Json.encodeToString(selected)

            // 2. Kirim String JSON tersebut melalui Intent
            intent.putExtra("KERANJANG_JSON", selectedItemsJson)

            COResultLauncher.launch(intent)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val rv: RecyclerView = findViewById(R.id.rvKeranjang)
        adapterKeranjang = KeranjangAdapter(listKeranjang) { item, newJumlah ->
            updateJumlahDiSupabase(item.id, newJumlah)
        }
        rv.adapter = adapterKeranjang
        rv.layoutManager = LinearLayoutManager(this)

        adapterKeranjang.setOnItemInteractionListener(object :
            KeranjangAdapter.OnItemInteractionListener {
            override fun onDataChanged() {
                updateTotalHargaUI()
                updateCheckboxPilihSemua()
            }
        })
    }

    private fun updateTotalHargaUI() {
        val total = listKeranjang.filter { it.terpilih }.sumOf { item ->
            when (item.tipe) {
                "produk" -> item.produk?.harga?.times(item.jumlah) ?: 0
                "paket" -> item.paket?.harga?.times(item.jumlah) ?: 0
                else -> 0
            }
        }
        txtTotalHarga.text = "Total harga:\n ${formatRupiah.format(total)}"
    }

    private fun updateCheckboxPilihSemua() {
        val allSelected = listKeranjang.isNotEmpty() && listKeranjang.all { it.terpilih }

        isUpdatingSelectAll = true
        cbPilihSemua.isChecked = allSelected
        isUpdatingSelectAll = false
    }

    private fun loadKeranjang() {
        lifecycleScope.launch {
            try {
                // Dapatkan ID pengguna yang sedang login
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    Toast.makeText(
                        this@KeranjangActivity,
                        "Sesi berakhir, silakan login kembali",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val data = SupabaseManager.client.postgrest.rpc(
                    "get_keranjang_with_promo",
                    mapOf("p_user" to userId)
                ).decodeList<Keranjang>()


                listKeranjang.clear()
                listKeranjang.addAll(
                    data.map { item ->
                        val tipe = if (item.produk != null) "produk" else "paket"
                        item.copy(tipe = tipe)
                    }
                )
                adapterKeranjang.notifyDataSetChanged()

                updateTotalHargaUI()
                updateCheckboxPilihSemua() // Panggil ini agar checkbox utama ikut update
            } catch (e: Exception) {
                Toast.makeText(
                    this@KeranjangActivity,
                    "Gagal memuat: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                // cuma buat debugging kalau ada error
                val tes = findViewById<EditText>(R.id.tes)
                tes.setText(e.message)
                tes.visibility = View.VISIBLE
            }
        }
    }

    private val COResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Cek apakah hasilnya OK (yang akan kita atur dari UbahTambahAlamatActivity)
        if (result.resultCode == RESULT_OK) {
            loadKeranjang() // Panggil kembali fungsi untuk me-refresh data dari Supabase
        }
    }


    private fun updateJumlahDiSupabase(idKeranjang: String, newJumlah: Int) {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.from("keranjang")
                    .update(mapOf("jumlah" to newJumlah)) {
                        filter {
                            eq("id_keranjang", idKeranjang)
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(
                    this@KeranjangActivity,
                    "Gagal update jumlah: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}