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
import ordinary.rahmatbakery.pelanggan.adapter.PesananTerakhirAdapter
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class BerandaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PesananTerakhirAdapter
    private val listPesanan = mutableListOf<PesananTerakhir>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Ambil tombol dari layout fragment
        val btnNotif = rootView.findViewById<ImageView>(R.id.icon_notif)

        // Set aksi klik
        btnNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi RecyclerView
        recyclerView = rootView.findViewById(R.id.rvPesananTerakhir)
        adapter = PesananTerakhirAdapter(listPesanan)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // nanti bagian ini diganti dengan data dari API
        dummyData()

        return rootView
    }

    private fun dummyData() {
        listPesanan.add(PesananTerakhir(1,"Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir(1,"Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir(1,"Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir(1,"Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir(1,"Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir(1,"Kue Keju", "https://contoh.com/kue_keju.jpg"))
        adapter.notifyDataSetChanged()
    }

}