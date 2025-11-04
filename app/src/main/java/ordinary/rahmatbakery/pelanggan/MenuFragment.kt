package ordinary.rahmatbakery.pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuProdukAdapter
import ordinary.rahmatbakery.pelanggan.model.Kategori
import ordinary.rahmatbakery.pelanggan.model.Produk

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuFragment : Fragment() {

    private lateinit var rvProduct: RecyclerView
    private lateinit var adapterProduct: MenuProdukAdapter
    private val listMenu = mutableListOf<Produk>()

    private lateinit var rvKategori: RecyclerView
    private lateinit var adapterKategori: KategoriAdapter
    private val listKategori = mutableListOf<Kategori>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_menu, container, false)

        // Ambil tombol dari layout fragment
        val btnNotif = rootView.findViewById<ImageView>(R.id.icon_notif)
        val btnCart = rootView.findViewById<ImageView>(R.id.icon_cart)

        // Set aksi klik
        btnNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifActivity::class.java)
            startActivity(intent)
        }
        btnCart.setOnClickListener {
            val intent = Intent(requireContext(), KeranjangActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi RecyclerView
        // Produk
        rvProduct = rootView.findViewById(R.id.rvProduct)
        adapterProduct = MenuProdukAdapter(listMenu)
        rvProduct.adapter = adapterProduct
        rvProduct.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        // Kategori
        rvKategori = rootView.findViewById(R.id.rvKategori)
        adapterKategori = KategoriAdapter(listKategori)
        rvKategori.adapter = adapterKategori
        rvKategori.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // nanti bagian ini diganti dengan data dari API
        loadProduct("")
        loadKategori()

        return rootView
    }

    fun loadKategori() {
        lifecycleScope.launch {
            val kategories = SupabaseManager.client.from("kategori")
                .select(columns = Columns.raw(
                    """id : id_kategori,
                nama : nama_kategori""".trimIndent()
                )) // Select all columns, or specify with Columns.list("name", "country_id")
                .decodeList<Kategori>()

            if(!kategories.isEmpty()){
                for (kategori in kategories) {
                    listKategori.add(Kategori(kategori.id,kategori.nama))
                }
                adapterKategori.notifyDataSetChanged()
            }
        }

    }

    fun loadProduct(kategori : String) {
        lifecycleScope.launch {
        val produk = SupabaseManager.client.from("produk")
            .select(columns = Columns.raw(
                """
                id : id_produk,
                nama : nama_produk,
                deskripsi : deskripsi,
                gambar : foto_produk,
                harga : harga
                
                """.trimIndent()
            )) // Select all columns, or specify with Columns.list("name", "country_id")
            .decodeList<Produk>()

            if(!produk.isEmpty()){
                for (menuProduk in produk) {
                    listMenu.add(Produk(menuProduk.id,menuProduk.nama, menuProduk.deskripsi,menuProduk.gambar, menuProduk.harga))
                }
                adapterProduct.notifyDataSetChanged()
            }
        }

    }

}