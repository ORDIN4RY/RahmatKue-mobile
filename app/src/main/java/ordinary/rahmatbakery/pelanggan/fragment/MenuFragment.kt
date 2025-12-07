package ordinary.rahmatbakery.pelanggan.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuProdukAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuPaketAdapter
import ordinary.rahmatbakery.pelanggan.model.*

class MenuFragment : Fragment() {

    private lateinit var rvProduct: RecyclerView
    private lateinit var adapterProduct: MenuProdukAdapter
    private val listMenu = mutableListOf<Produk>()
    private lateinit var adapterPaket: MenuPaketAdapter
    private val listPaket = mutableListOf<Paket>()

    private lateinit var rvKategori: RecyclerView
    private lateinit var adapterKategori: KategoriAdapter
    private val listKategori = mutableListOf<Kategori>()

    private val originalListMenu = mutableListOf<Produk>()
    private val originalListPaket = mutableListOf<Paket>()

    private lateinit var etSearch: EditText
    // Filter buttons
    private lateinit var btnFilterProduk: TextView
    private lateinit var btnFilterPaket: TextView
    private var currentFilter = "produk" // default filter
    private var currentKateg = "" // default kategori filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_menu, container, false)

        // Inisialisasi Filter Buttons
        btnFilterProduk = rootView.findViewById(R.id.btn_satuan)
        btnFilterPaket = rootView.findViewById(R.id.btn_paket)

        // Inisialisasi RecyclerView Produk
        rvProduct = rootView.findViewById(R.id.rvProduct)
        adapterProduct = MenuProdukAdapter(listMenu)
        adapterPaket = MenuPaketAdapter(listPaket)

        // search
        etSearch = rootView.findViewById(R.id.etSearch)

        rvProduct.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        // Inisialisasi RecyclerView Kategori
        rvKategori = rootView.findViewById(R.id.rvKategori)
        adapterKategori = KategoriAdapter(listKategori, currentKateg) { kategori ->
            applyKategFilter(kategori.id)
        }
        rvKategori.adapter = adapterKategori
        rvKategori.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Setup filter buttons
        setupFilterButtons()
        setupSearchListener()

        // Load data awal
        loadKategori()
        applyFilter("produk")

        return rootView
    }

    override fun onResume() {
        super.onResume()
        // Refresh data saat kembali ke fragment
        when (currentFilter) {
            "produk" -> loadProduct(currentKateg)
            "paket" -> loadPaket()
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterData(s.toString())
            }
        })
    }

    private fun filterData(query: String) {
        when (currentFilter) {
            "produk" -> {
                val filteredList = if (query.isBlank()) {
                    originalListMenu
                } else {
                    originalListMenu.filter { produk ->
                        produk.nama.contains(query, ignoreCase = true)
                    }
                }
                listMenu.clear()
                listMenu.addAll(filteredList)
                adapterProduct.notifyDataSetChanged()
            }
            "paket" -> {
                val filteredList = if (query.isBlank()) {
                    originalListPaket
                } else {
                    originalListPaket.filter { paket ->
                        paket.nama.contains(query, ignoreCase = true)
                    }
                }
                listPaket.clear()
                listPaket.addAll(filteredList)
                adapterPaket.notifyDataSetChanged()
            }
        }
    }

    private fun setupFilterButtons() {
        btnFilterProduk.setOnClickListener {
            applyFilter("produk")
        }

        btnFilterPaket.setOnClickListener {
            applyFilter("paket")
        }

    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateButtonStates()
        etSearch.text.clear()

        when (filter) {
            "produk" -> {
                loadProduct(currentKateg)
                rvProduct.adapter = adapterProduct
            }

            "paket" -> {
                loadPaket()
                rvProduct.adapter = adapterPaket
            }

        }
    }

    private fun applyKategFilter(newKateg: String) {
        if (currentKateg != newKateg) {
            currentKateg = newKateg
        } else {
            currentKateg = ""
        }

        etSearch.text.clear()
        loadProduct(currentKateg)
        rvProduct.adapter = adapterProduct

        adapterKategori.updateSelected(currentKateg)
    }

    private fun updateButtonStates() {
        btnFilterProduk.isSelected = false
        btnFilterPaket.isSelected = false

        when (currentFilter) {
            "produk" -> btnFilterProduk.isSelected = true
            "paket" -> btnFilterPaket.isSelected = true
        }
    }

    private fun loadKategori() {
        lifecycleScope.launch {
            try {
                val kategories = SupabaseManager.client.postgrest.rpc(
                    function = "get_kategori_with_products"
                ).decodeList<Kategori>()

                if (kategories.isNotEmpty()) {
                    listKategori.clear()
                    listKategori.addAll(kategories)
                    adapterKategori.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProduct(kategori: String) {
        lifecycleScope.launch {
            try {
                val produk = SupabaseManager.client.postgrest
                    .rpc("get_produk_with_promo")
                    .decodeList<Produk>()

                val filtered = if (kategori.isNotEmpty()) {
                    produk.filter { it.kategori.id == kategori }
                } else produk

                originalListMenu.clear()
                originalListMenu.addAll(filtered)

                listMenu.clear()
                listMenu.addAll(originalListMenu)
                rvKategori.visibility = View.VISIBLE
                adapterProduct.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadPaket() {
        lifecycleScope.launch {
            try {
                val paket = SupabaseManager.client.postgrest
                    .rpc("get_paket_with_promo")
                    .decodeList<Paket>()

                originalListPaket.clear()
                originalListPaket.addAll(paket)

                rvKategori.visibility = View.GONE
                listPaket.clear()
                listPaket.addAll(originalListPaket)
                adapterPaket.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}