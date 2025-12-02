package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher
import ordinary.rahmatbakery.pelanggan.adapter.MenuPaketAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuProdukAdapter
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import ordinary.rahmatbakery.pelanggan.adapter.VoucherSayaAdapter
import ordinary.rahmatbakery.pelanggan.model.Paket
import ordinary.rahmatbakery.pelanggan.model.Produk

class VoucherActivity : AppCompatActivity() {

    private lateinit var btnFilterVoucherSaya: TextView
    private lateinit var btnFilterTukarVoucher: TextView
    private lateinit var userPoint: TextView
    private lateinit var voucherSayaAdapter: VoucherSayaAdapter
    private lateinit var tukarVoucherAdapter: TukarVoucherAdapter
    private lateinit var rvVoucher: RecyclerView
    private var currentFilter = "voucherSaya"
    private lateinit var etSearch: EditText

    private val VoucherSaya = mutableListOf<UserVoucher>()
    private val TukarVoucher = mutableListOf<Voucher>()

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
        updateButtonStates()

        when (filter) {
            "voucherSaya" -> {
                rvVoucher.layoutManager= LinearLayoutManager(this)
                rvVoucher.adapter = voucherSayaAdapter
                loadVoucherSaya()
            }
            "tukarVoucher" -> {
                rvVoucher.layoutManager= GridLayoutManager(this,2)
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
                """.trimIndent()))
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
                val result = SupabaseManager.client.postgrest["voucher"]
                    .select(Columns.raw("*"))
                    .decodeList<Voucher>()

                TukarVoucher.clear()
                TukarVoucher.addAll(result)

                listTukarVoucher.clear()
                listTukarVoucher.addAll(result)

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
                            eq("id", userId)   // ✔ sama persis format seperti loadPesanan
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

}
