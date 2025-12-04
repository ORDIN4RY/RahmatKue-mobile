package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.model.Profile
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import ordinary.rahmatbakery.pelanggan.adapter.VoucherSayaAdapter
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher

class VoucherActivity : AppCompatActivity() {

    private lateinit var btnFilterVoucherSaya: TextView
    private lateinit var btnFilterTukarVoucher: TextView
    private lateinit var userPoint: TextView
    private lateinit var voucherSayaAdapter: VoucherSayaAdapter
    private lateinit var tukarVoucherAdapter: TukarVoucherAdapter
    private lateinit var rvVoucher: RecyclerView
    private var currentFilter = "voucherSaya"
    private lateinit var etSearch: EditText

    // Data asli dari server
    private val VoucherSaya = mutableListOf<UserVoucher>()
    private val TukarVoucher = mutableListOf<Voucher>()

    // Data yang ditampilkan (setelah filter)
    private val listVoucherSaya = mutableListOf<UserVoucher>()
    private val listTukarVoucher = mutableListOf<Voucher>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.voucher)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvVoucher = findViewById(R.id.rvVoucher)
        etSearch = findViewById(R.id.etSearch)

        voucherSayaAdapter = VoucherSayaAdapter(listVoucherSaya)
        tukarVoucherAdapter = TukarVoucherAdapter(listTukarVoucher)

        btnFilterVoucherSaya = findViewById(R.id.btnVoucherSaya)
        btnFilterTukarVoucher = findViewById(R.id.btnTukarVoucher)
        userPoint = findViewById(R.id.user_point)

        setupFilterButtons()
        setupSearchListener()
        loadUserPoint()

        applyFilter("voucherSaya")
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke activity
        loadUserPoint()
        if (currentFilter == "voucherSaya") {
            loadVoucherSaya()
        } else {
            loadVoucher()
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterData(query: String) {
        when (currentFilter) {
            "voucherSaya" -> {
                val filtered = if (query.isBlank()) VoucherSaya
                else VoucherSaya.filter {
                    it.voucher.nama_voucher.contains(query, true)
                }

                listVoucherSaya.clear()
                listVoucherSaya.addAll(filtered)
                voucherSayaAdapter.notifyDataSetChanged()
            }

            "tukarVoucher" -> {
                val filtered = if (query.isBlank()) TukarVoucher
                else TukarVoucher.filter {
                    it.nama_voucher.contains(query, true)
                }

                listTukarVoucher.clear()
                listTukarVoucher.addAll(filtered)
                tukarVoucherAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupFilterButtons() {
        btnFilterVoucherSaya.setOnClickListener { applyFilter("voucherSaya") }
        btnFilterTukarVoucher.setOnClickListener { applyFilter("tukarVoucher") }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        etSearch.text.clear() // Clear search when switching filter
        updateButtonStates()

        when (filter) {
            "voucherSaya" -> {
                rvVoucher.layoutManager = LinearLayoutManager(this)
                rvVoucher.adapter = voucherSayaAdapter
                loadVoucherSaya()
            }
            "tukarVoucher" -> {
                rvVoucher.layoutManager = GridLayoutManager(this, 2)
                rvVoucher.adapter = tukarVoucherAdapter
                loadVoucher()
            }
        }
    }

    private fun updateButtonStates() {
        btnFilterVoucherSaya.isSelected = currentFilter == "voucherSaya"
        btnFilterTukarVoucher.isSelected = currentFilter == "tukarVoucher"
    }

    private fun loadVoucherSaya() {
        lifecycleScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (userId == null) return@launch

                val result = SupabaseManager.client.postgrest["user_voucher"]
                    .select(Columns.raw("""
                    *,
                    id_user_voucher,
                    id_voucher,
                    id_user,
                    status,
                    voucher:id_voucher (
                        id_voucher,
                        nama_voucher,
                        kode_voucher,
                        tgl_mulai,
                        tgl_berakhir,
                        deskripsi,
                        poin_tukar,
                        foto
                    )
                """.trimIndent())) {
                        filter {
                            eq("id_user", userId)
                        }
                    }
                    .decodeList<UserVoucher>()

                VoucherSaya.clear()
                VoucherSaya.addAll(result)

                listVoucherSaya.clear()
                listVoucherSaya.addAll(result)

                voucherSayaAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadVoucher() {
        lifecycleScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (userId == null) return@launch

                // Ambil semua voucher aktif
                val allVouchers = SupabaseManager.client.postgrest["voucher"]
                    .select(Columns.raw("*")) {
                        filter {
                            eq("is_active", true)
                        }
                    }
                    .decodeList<Voucher>()

                // Ambil voucher yang sudah dimiliki user
                val userVouchers = SupabaseManager.client.postgrest["user_voucher"]
                    .select(Columns.raw("id_voucher")) {
                        filter {
                            eq("id_user", userId)
                        }
                    }
                    .decodeList<Map<String, String>>()

                val ownedVoucherIds = userVouchers.mapNotNull { it["id_voucher"] }

                // Filter voucher yang belum dimiliki
                val availableVouchers = if (ownedVoucherIds.isEmpty()) {
                    allVouchers
                } else {
                    allVouchers.filter { voucher ->
                        // Cek apakah voucher.id_voucher ada di ownedVoucherIds
                        !ownedVoucherIds.contains(voucher.id_voucher.toString())
                    }
                }

                TukarVoucher.clear()
                TukarVoucher.addAll(availableVouchers)

                listTukarVoucher.clear()
                listTukarVoucher.addAll(availableVouchers)

                tukarVoucherAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadUserPoint() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    val result = SupabaseManager.client.postgrest
                        .from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingle<Profile>()

                    val point = result.point
                    userPoint.text = "${point} Points"

                } catch (e: Exception) {
                    e.printStackTrace()
                    userPoint.text = "error"
                }
            }
        }
    }

    // Fungsi untuk refresh data setelah menukar voucher
    fun refreshAfterExchange() {
        loadUserPoint()
        loadVoucherSaya()
        loadVoucher()
    }
}