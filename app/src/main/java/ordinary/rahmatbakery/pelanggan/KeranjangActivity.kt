package ordinary.rahmatbakery.pelanggan

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
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

    private var realtimeChannel: RealtimeChannel? = null
    private var realtimeJob: Job? = null

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
            Toast.makeText(this, "Checkout ${selected.size} item", Toast.LENGTH_SHORT).show()
            // TODO: lanjutkan ke halaman checkout
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

//    private fun loadKeranjang() {
//        lifecycleScope.launch {
//            try {
//                val data = SupabaseManager.client.from("keranjang").select(
//                    Columns.raw(
//                        """
//                        id:id_keranjang,
//                        jumlah,
//                        produk:id_produk(id:id_produk, nama:nama_produk, deskripsi, gambar:foto_produk, harga),
//                        paket:id_paket(
//                            id:id_paket,
//                            nama:nama_paket,
//                            deskripsi,
//                            foto:foto_paket,
//                            harga:harga_paket,
//                            detail:detail_paket(
//                                jumlah,
//                                produk:id_produk(id:id_produk, nama:nama_produk, deskripsi, gambar:foto_produk, harga)
//                            )
//                        )
//                        """
//                    )
//                ).decodeList<Keranjang>()
//
//                listKeranjang.clear()
//                listKeranjang.addAll(
//                    data.map { item ->
//                        val tipe = if (item.produk != null) "produk" else "paket"
//                        item.copy(tipe = tipe)
//                    }
//                )
//                adapterKeranjang.notifyDataSetChanged()
//
//                updateTotalHargaUI()
//            } catch (e: Exception) {
//                Toast.makeText(this@KeranjangActivity, "Gagal memuat: ${e.message}", Toast.LENGTH_LONG).show()
//                findViewById<EditText>(R.id.tes).setText(e.message)
//            }
//        }
//    }

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

                val data = SupabaseManager.client.from("keranjang").select(
                    Columns.raw(
                        """
                        id:id_keranjang,
                        jumlah,
                        id_produk,
                        id_paket,
                        produk:id_produk(id:id_produk, nama:nama_produk, deskripsi, gambar:foto_produk, harga),
                        paket:id_paket(
                            id:id_paket, 
                            nama:nama_paket, 
                            deskripsi, 
                            foto:foto_paket, 
                            harga:harga_paket, 
                            detail:detail_paket(
                                jumlah, 
                                produk:id_produk(id:id_produk, nama:nama_produk, deskripsi, gambar:foto_produk, harga)
                            )
                        )
                        """
                    )
                ) {
                    // TAMBAHKAN FILTER INI
                    filter {
                        eq("id_user", userId)
                    }
                }.decodeList<Keranjang>()

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
                // Baris ini sepertinya untuk debug, bisa dihapus jika ada EditText 'tes'
                val tes = findViewById<EditText>(R.id.tes)
                tes.setText(e.message)
                tes.visibility = View.VISIBLE
            }
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


    override fun onStart() {
        super.onStart()

        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return

        val channel = SupabaseManager.client.channel("keranjang-$userId") {
        }

        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "keranjang"
            filter("id_user", FilterOperator.EQ, userId)
        }

        realtimeJob = lifecycleScope.launch {
            changeFlow.collect { action ->
                when (action) {
                    is PostgresAction.Insert,
                    is PostgresAction.Update,
                    is PostgresAction.Delete -> {
                        kotlinx.coroutines.delay(200)
                        loadKeranjang()
                    }

                    else -> Unit
                }
            }
        }

        lifecycleScope.launch {
            channel.subscribe()
        }

        realtimeChannel = channel
    }


    override fun onStop() {
        super.onStop()
        realtimeJob?.cancel()
        lifecycleScope.launch {
            realtimeChannel?.unsubscribe()
        }
    }
}