package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuProdukAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuPaketAdapter
import ordinary.rahmatbakery.pelanggan.model.Kategori
import ordinary.rahmatbakery.pelanggan.model.Produk
import ordinary.rahmatbakery.pelanggan.model.Paket
import ordinary.rahmatbakery.pelanggan.model.Wadah

class MenuFragment : Fragment() {

    private lateinit var rvProduct: RecyclerView
    private lateinit var adapterProduct: MenuProdukAdapter
    private val listMenu = mutableListOf<Produk>()

    private lateinit var adapterPaket: MenuPaketAdapter
    private val listPaket = mutableListOf<Paket>()

    private lateinit var rvKategori: RecyclerView
    private lateinit var adapterKategori: KategoriAdapter
    private val listKategori = mutableListOf<Kategori>()

    // Filter buttons
    private lateinit var btnFilterProduk: TextView
    private lateinit var btnFilterPaket: TextView
    private lateinit var btnFilterCustom: TextView

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
        btnFilterCustom = rootView.findViewById(R.id.btn_custom)

        // Inisialisasi RecyclerView Produk
        rvProduct = rootView.findViewById(R.id.rvProduct)
        adapterProduct = MenuProdukAdapter(listMenu)
        adapterPaket = MenuPaketAdapter(listPaket)

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

        // Load data awal
        loadKategori()
        applyFilter("produk")

        return rootView
    }


    private fun setupFilterButtons() {
        btnFilterProduk.setOnClickListener {
            applyFilter("produk")
        }

        btnFilterPaket.setOnClickListener {
            applyFilter("paket")
        }

        btnFilterCustom.setOnClickListener {
            applyFilter("custom")
        }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter

        // Update button states (visual feedback)
        updateButtonStates()

        when (filter) {
            "produk" -> {
                loadProduct(currentKateg)
                rvProduct.adapter = adapterProduct
            }

            "paket" -> {
                loadPaket()
                rvProduct.adapter = adapterPaket
            }

            "custom" -> {
                // Untuk custom (kotak), akan diimplementasikan nanti
                listMenu.clear()
                listPaket.clear()
                adapterProduct.notifyDataSetChanged()
                adapterPaket.notifyDataSetChanged()
                // TODO: Implementasi custom box nanti
            }
        }
    }

    private fun applyKategFilter(newKateg: String) {
        if (currentKateg != newKateg) {
            currentKateg = newKateg
        } else {
            currentKateg = ""
        }
        loadProduct(currentKateg)
        rvProduct.adapter = adapterProduct

        adapterKategori.updateSelected(currentKateg)
    }

    private fun updateButtonStates() {
        // Reset semua button ke state default
        btnFilterProduk.isSelected = false
        btnFilterPaket.isSelected = false
        btnFilterCustom.isSelected = false

        // Set button yang aktif
        when (currentFilter) {
            "produk" -> btnFilterProduk.isSelected = true
            "paket" -> btnFilterPaket.isSelected = true
            "custom" -> btnFilterCustom.isSelected = true
        }
    }

    private fun loadKategori() {
        lifecycleScope.launch {
            try {
                // Panggil fungsi RPC yang sudah kita buat
                val kategories = SupabaseManager.client.postgrest.rpc(
                    function = "get_kategori_with_products"
                ).decodeList<Kategori>() // Gunakan data class Kategori yang sudah ada

                // Logika selanjutnya tetap sama
                if (kategories.isNotEmpty()) {
                    listKategori.clear()
                    listKategori.addAll(kategories)
                    adapterKategori.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Mungkin tampilkan Toast jika gagal
            }
        }
    }

    private fun loadProduct(kategori: String) {
        lifecycleScope.launch {
            try {
                val produk = SupabaseManager.client.from("produk")
                    .select(
                        Columns.raw(
                            """
                        id:id_produk,
                        nama:nama_produk,
                        varian,
                        kategori:id_kategori(
                            id:id_kategori,
                            nama:nama_kategori,
                            minimal_pembelian
                        ),
                        deskripsi,
                        gambar:foto_produk,
                        harga
                        """.trimIndent()
                        )
                    ) {
                        if (kategori.isNotEmpty()) {
                            filter { eq("id_kategori", kategori) }
                        }
                    }
                    .decodeList<Produk>()

                listMenu.clear()
                listMenu.addAll(produk)
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
                val paket = SupabaseManager.client.from("paket")
                    .select(
                        Columns.raw(
                            """
                        id:id_paket,
                        nama:nama_paket,
                        deskripsi,
                        foto:foto_paket,
                        harga:harga_paket,

                        wadah:wadah(
                            id:id_wadah,
                            nama:nama_wadah,
                            deskripsi,
                            foto:foto_wadah,
                            kapasitas,
                            harga:harga_wadah,
                            varian
                        ),

                        detail:detail_paket(
                            produk:id_produk(
                                id:id_produk,
                                nama:nama_produk,
                                varian,
                                kategori:id_kategori(
                                    id:id_kategori,
                                    nama:nama_kategori,
                                    minimal_pembelian
                                ),
                                deskripsi,
                                gambar:foto_produk,
                                harga
                            )
                        )
                        """.trimIndent()
                        )
                    )
                    .decodeList<Paket>()

                listPaket.clear()
                listPaket.addAll(paket)
                rvKategori.visibility = View.GONE
                adapterPaket.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



//    private fun loadCustom() {
//        lifecycleScope.launch {
//            try {
//                val wadah = SupabaseManager.client.from("wadah")
//                    .select(
//                        Columns.raw(
//                            """
//                        id:id_wadah,
//                        nama:nama_wadah,
//                        deskripsi,
//                        foto:foto_wadah,
//                        kapasitas,
//                        harga:harga_wadah,
//                        varian
//                        """.trimIndent()
//                        )
//                    )
//                    .decodeList<Wadah>()
//
//                // ganti adapter Product menjadi adapter Custom
//                listMenu.clear()
//                listPaket.clear()
//
//                // jika kamu punya adapter khusus custom → pasang di sini
//                // rvProduct.adapter = adapterCustom
//
//                // sementara tampilkan dengan adapterProduct agar cepat
//                // (silakan ganti jika sudah ada adapter Custom)
//                // listMenuCustom.addAll(wadah)
//
//                rvKategori.visibility = View.GONE
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }


}