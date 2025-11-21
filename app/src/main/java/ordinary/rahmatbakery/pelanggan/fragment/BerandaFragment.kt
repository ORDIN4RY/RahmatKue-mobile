package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import android.widget.LinearLayout
import ordinary.rahmatbakery.pelanggan.activity.DashboardActivity
import ordinary.rahmatbakery.pelanggan.adapter.PesananTerakhirAdapter
import ordinary.rahmatbakery.pelanggan.adapter.CarouselAdapter
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class BerandaFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter


    private val handler = Handler(Looper.getMainLooper())

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val nextItem = (viewPager.currentItem + 1) % carouselAdapter.itemCount
            viewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 3000)
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var pesananAdapter: PesananTerakhirAdapter
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

        val rootView = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Ambil view
        val nameText = rootView.findViewById<TextView>(R.id.nickname)
        val pointText = rootView.findViewById<TextView>(R.id.user_point)

        val username = parentActivity?.profile?.username
        val point = parentActivity?.profile?.point

        nameText.text = "Hi, ${username ?: "Pengguna"} !"
        pointText.text = "${point ?: 0} Points"

        // RecyclerView pesanan terakhir
        recyclerView = rootView.findViewById(R.id.rvPesananTerakhir)
        pesananAdapter = PesananTerakhirAdapter(listPesanan)
        recyclerView.adapter = pesananAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        dummyData()

        // ===== CAROUSEL AREA =====
        viewPager = rootView.findViewById(R.id.carousel_view_pager)

        val carouselItems = listOf(
            R.drawable.carousel_1
        )

        carouselAdapter = CarouselAdapter(carouselItems)
        viewPager.adapter = carouselAdapter

        return rootView
    }

    private fun dummyData() {
        listPesanan.add(PesananTerakhir("1", "Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("1", "Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir("1", "Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("1", "Kue Keju", "https://contoh.com/kue_keju.jpg"))
        pesananAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
