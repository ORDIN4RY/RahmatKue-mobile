package ordinary.rahmatbakery.pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.PesananTerakhirAdapter
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class BerandaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PesananTerakhirAdapter
    private val listPesanan = mutableListOf<PesananTerakhir>()

    private val parentActivity: DashboardActivity?
        get() = activity as? DashboardActivity



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
        val btnCart = rootView.findViewById<ImageView>(R.id.icon_cart)
        val nameText = rootView.findViewById<TextView>(R.id.nickname)
        val pointText = rootView.findViewById<TextView>(R.id.user_point)

        val username = parentActivity?.profile?.username
        val point = parentActivity?.profile?.point

        nameText.setText("Hi, ${username?: "Pengguna"} !")
        pointText.setText("${point?: 0} Points")


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
        recyclerView = rootView.findViewById(R.id.rvPesananTerakhir)
        adapter = PesananTerakhirAdapter(listPesanan)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // nanti bagian ini diganti dengan data dari API
        dummyData()

        return rootView
    }

    private fun dummyData() {
        listPesanan.add(PesananTerakhir("1","Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("1","Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir("1","Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("1","Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir("1","Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("1","Kue Keju", "https://contoh.com/kue_keju.jpg"))
        adapter.notifyDataSetChanged()
    }

}