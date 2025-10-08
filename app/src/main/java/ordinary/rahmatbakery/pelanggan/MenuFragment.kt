package ordinary.rahmatbakery.pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.MenuProdukAdapter
import ordinary.rahmatbakery.pelanggan.adapter.PesananTerakhirAdapter
import ordinary.rahmatbakery.pelanggan.model.Kategori
import ordinary.rahmatbakery.pelanggan.model.MenuProduk

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
    private val listMenu = mutableListOf<MenuProduk>()

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

        // Set aksi klik
        btnNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifActivity::class.java)
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
        dummyProduct()
        dummyKategori()

        return rootView
    }

    private fun dummyProduct() {
        listMenu.add(MenuProduk("Roti Coklat", "https://contoh.com/roti_coklat.jpg",12000))
        listMenu.add(MenuProduk("Kue Keju", "https://contoh.com/kue_keju.jpg",12000))
        listMenu.add(MenuProduk("Roti Coklat", "https://contoh.com/roti_coklat.jpg",12000))
        listMenu.add(MenuProduk("Kue Keju", "https://contoh.com/kue_keju.jpg",12000))
        listMenu.add(MenuProduk("Roti Coklat", "https://contoh.com/roti_coklat.jpg",12000))
        listMenu.add(MenuProduk("Kue Keju", "https://contoh.com/kue_keju.jpg",12000))
        adapterProduct.notifyDataSetChanged()
    }
    private fun dummyKategori() {
        listKategori.add(Kategori(1, "kategori 1"))
        listKategori.add(Kategori(1, "kategori 2"))
        listKategori.add(Kategori(1, "kategori 1"))
        listKategori.add(Kategori(1, "kategori 2"))
        listKategori.add(Kategori(1, "kategori 1"))
        listKategori.add(Kategori(1, "kategori 2"))
        adapterKategori.notifyDataSetChanged()
    }

}