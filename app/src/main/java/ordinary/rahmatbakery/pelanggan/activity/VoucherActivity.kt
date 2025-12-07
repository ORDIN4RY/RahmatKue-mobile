package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.RangeSlider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.admin.model.Profile
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import ordinary.rahmatbakery.pelanggan.adapter.VoucherSayaAdapter
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.model.Voucher

class VoucherActivity : AppCompatActivity() {

    private lateinit var btnFilterVoucherSaya: TextView
    private lateinit var btnFilterTukarVoucher: TextView
    private lateinit var btnFilter: ImageView
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

    // Filter state
    private var filterJenisVoucher: String? = null // "potongan" atau "ongkir"
    private var filterMinPoin: Int = 0
    private var filterMaxPoin: Int = 1000
    private var maxAvailablePoin: Int = 1000

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
        btnFilter = findViewById(R.id.btnFilter)

        voucherSayaAdapter = VoucherSayaAdapter(listVoucherSaya)
        tukarVoucherAdapter = TukarVoucherAdapter(listTukarVoucher)

        btnFilterVoucherSaya = findViewById(R.id.btnVoucherSaya)
        btnFilterTukarVoucher = findViewById(R.id.btnTukarVoucher)
        userPoint = findViewById(R.id.user_point)

        setupFilterButtons()
        setupSearchListener()
        setupFilterButton()
        loadUserPoint()

        applyFilter("voucherSaya")
    }

    override fun onResume() {
        super.onResume()
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

    private fun setupFilterButton() {
        btnFilter.setOnClickListener {
            if (currentFilter == "tukarVoucher") {
                showFilterBottomSheet()
            }
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter_voucher, null)

        // Jenis Voucher Filter
        val btnSemua = view.findViewById<TextView>(R.id.btnSemua)
        val btnPotongan = view.findViewById<TextView>(R.id.btnPotongan)
        val btnOngkir = view.findViewById<TextView>(R.id.btnOngkir)

        // Range Poin
        val rangeSlider = view.findViewById<RangeSlider>(R.id.rangeSliderPoin)
        val tvRangePoin = view.findViewById<TextView>(R.id.tvRangePoin)

        // Setup range slider
        rangeSlider.valueFrom = 0f
        rangeSlider.valueTo = maxAvailablePoin.toFloat()
        rangeSlider.values = listOf(filterMinPoin.toFloat(), filterMaxPoin.toFloat())

        rangeSlider.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            tvRangePoin.text = "${values[0].toInt()} - ${values[1].toInt()} Poin"
        }

        // Set initial button states
        updateJenisButtonStates(btnSemua, btnPotongan, btnOngkir)

        // Jenis filter click listeners
        btnSemua.setOnClickListener {
            filterJenisVoucher = null
            updateJenisButtonStates(btnSemua, btnPotongan, btnOngkir)
        }

        btnPotongan.setOnClickListener {
            filterJenisVoucher = "potongan"
            updateJenisButtonStates(btnSemua, btnPotongan, btnOngkir)
        }

        btnOngkir.setOnClickListener {
            filterJenisVoucher = "ongkir"
            updateJenisButtonStates(btnSemua, btnPotongan, btnOngkir)
        }

        // Reset & Apply buttons
        view.findViewById<TextView>(R.id.btnReset).setOnClickListener {
            filterJenisVoucher = null
            filterMinPoin = 0
            filterMaxPoin = maxAvailablePoin
            rangeSlider.values = listOf(0f, maxAvailablePoin.toFloat())
            updateJenisButtonStates(btnSemua, btnPotongan, btnOngkir)
        }

        view.findViewById<TextView>(R.id.btnApply).setOnClickListener {
            val values = rangeSlider.values
            filterMinPoin = values[0].toInt()
            filterMaxPoin = values[1].toInt()

            applyAdvancedFilter()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun updateJenisButtonStates(btnSemua: TextView, btnPotongan: TextView, btnOngkir: TextView) {
        btnSemua.isSelected = filterJenisVoucher == null
        btnPotongan.isSelected = filterJenisVoucher == "potongan"
        btnOngkir.isSelected = filterJenisVoucher == "ongkir"

        // Update background colors
        btnSemua.setBackgroundResource(if (filterJenisVoucher == null) R.drawable.btn_filter_produk else R.drawable.btn_filter_produk_outline)
        btnPotongan.setBackgroundResource(if (filterJenisVoucher == "potongan") R.drawable.btn_filter_produk else R.drawable.btn_filter_produk_outline)
        btnOngkir.setBackgroundResource(if (filterJenisVoucher == "ongkir") R.drawable.btn_filter_produk else R.drawable.btn_filter_produk_outline)

        // Update text colors
        val selectedColor = getColor(R.color.white)
        val unselectedColor = getColor(R.color.primary)

        btnSemua.setTextColor(if (filterJenisVoucher == null) selectedColor else unselectedColor)
        btnPotongan.setTextColor(if (filterJenisVoucher == "potongan") selectedColor else unselectedColor)
        btnOngkir.setTextColor(if (filterJenisVoucher == "ongkir") selectedColor else unselectedColor)
    }

    private fun applyAdvancedFilter() {
        filterData(etSearch.text.toString())
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
                var filtered = TukarVoucher.asSequence()

                // Apply search query
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.nama_voucher.contains(query, true)
                    }
                }

                // Apply jenis filter
                if (filterJenisVoucher != null) {
                    filtered = filtered.filter {
                        it.jenis_voucher == filterJenisVoucher
                    }
                }

                // Apply poin range filter
                filtered = filtered.filter {
                    val poin = it.poin_tukar ?: 0
                    poin in filterMinPoin..filterMaxPoin
                }

                listTukarVoucher.clear()
                listTukarVoucher.addAll(filtered.toList())
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
        etSearch.text.clear()

        // Reset advanced filters when switching tabs
        filterJenisVoucher = null
        filterMinPoin = 0
        filterMaxPoin = maxAvailablePoin

        updateButtonStates()

        when (filter) {
            "voucherSaya" -> {
                btnFilter.visibility = View.GONE
                rvVoucher.layoutManager = LinearLayoutManager(this)
                rvVoucher.adapter = voucherSayaAdapter
                loadVoucherSaya()
            }
            "tukarVoucher" -> {
                btnFilter.visibility = View.VISIBLE
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

                val allVouchers = SupabaseManager.client.postgrest["voucher"]
                    .select(Columns.raw("*")) {
                        filter {
                            eq("is_active", true)
                        }
                    }
                    .decodeList<Voucher>()

                val userVouchers = SupabaseManager.client.postgrest["user_voucher"]
                    .select(Columns.raw("id_voucher")) {
                        filter {
                            eq("id_user", userId)
                        }
                    }
                    .decodeList<Map<String, String>>()

                val ownedVoucherIds = userVouchers.mapNotNull { it["id_voucher"] }

                val availableVouchers = if (ownedVoucherIds.isEmpty()) {
                    allVouchers
                } else {
                    allVouchers.filter { voucher ->
                        !ownedVoucherIds.contains(voucher.id_voucher.toString())
                    }
                }

                // Calculate max available poin
                maxAvailablePoin = availableVouchers.maxOfOrNull { it.poin_tukar ?: 0 } ?: 1000
                filterMaxPoin = maxAvailablePoin

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

    fun refreshAfterExchange() {
        loadUserPoint()
        loadVoucherSaya()
        loadVoucher()
    }
}