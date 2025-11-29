package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.pelanggan.activity.DashboardActivity
import ordinary.rahmatbakery.pelanggan.adapter.PesananTerakhirAdapter
import ordinary.rahmatbakery.pelanggan.adapter.CarouselAdapter
import ordinary.rahmatbakery.pelanggan.model.Carousel
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir
import android.widget.ImageView
import androidx.core.content.ContextCompat
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime

class BerandaFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private var realtimeChannel: RealtimeChannel? = null

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (::carouselAdapter.isInitialized && carouselAdapter.itemCount > 0) {
                val nextItem =
                    (viewPager.currentItem + 1) % carouselAdapter.itemCount
                viewPager.setCurrentItem(nextItem, true)
            }
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

        val nameText = rootView.findViewById<TextView>(R.id.nickname)
        val pointText = rootView.findViewById<TextView>(R.id.user_point)

        val username = parentActivity?.profile?.username
        val point = parentActivity?.profile?.point

        nameText.text = "Hi, ${username?.let { ambilNamaPendek(it) } ?: "Pengguna"} !"
        pointText.text = "${point ?: 0} Points"

        recyclerView = rootView.findViewById(R.id.rvPesananTerakhir)
        pesananAdapter = PesananTerakhirAdapter(listPesanan)
        recyclerView.adapter = pesananAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        dummyData()

        viewPager = rootView.findViewById(R.id.carousel_view_pager)
        indicatorContainer = rootView.findViewById(R.id.carousel_indicator_container)
        progressBar = rootView.findViewById(R.id.progress_bar)

        carouselAdapter = CarouselAdapter(emptyList())
        viewPager.adapter = carouselAdapter

        progressBar.visibility = View.VISIBLE
        viewPager.visibility = View.GONE

//        fetchAndSubscribeCarouselData()

        return rootView
    }

    private fun dummyData() {
        listPesanan.add(PesananTerakhir("1", "Roti Coklat", "https://contoh.com/roti_coklat.jpg"))
        listPesanan.add(PesananTerakhir("2", "Kue Keju", "https://contoh.com/kue_keju.jpg"))
        listPesanan.add(PesananTerakhir("3", "Roti Tawar", "https://contoh.com/roti_tawar.jpg"))
        listPesanan.add(PesananTerakhir("4", "Nastar", "https://contoh.com/nastar.jpg"))
        pesananAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
    }

//    private fun fetchAndSubscribeCarouselData() {
//        lifecycleScope.launch {
//            try {
//                val initial = SupabaseManager.client.postgrest["carousel_items"]
//                    .select()
//                    .decodeList<Carousel>()
//
//                val items = initial.map {
//                    Carousel(
//                        id = it.id,
//                        image_url = it.image_url
//                    )
//                }
//
//                setupCarousel(items)
//
//            } catch (e: Exception) {
//                Log.e("Supabase", "Fetch initial failed: ${e.message}")
//            } finally {
//                progressBar.visibility = View.GONE
//            }
//        }
//
//        // === REALTIME ===
//        lifecycleScope.launch {
//            realtimeChannel =
//                SupabaseManager.client.realtime.channel("carousel_updates")
//
//            realtimeChannel?.postgresChanges(
//                event = PostgresAction.  ,
//                schema = "public",
//                table = "carousel_items"
//            ) { payload ->
//                when (payload) {
//                    is PostgresAction.Insert,
//                    is PostgresAction.Update,
//                    is PostgresAction.Delete -> refetchCarouselData()
//                }
//            }
//
//            realtimeChannel?.subscribe()
//        }
//    }

    private fun refetchCarouselData() {
        lifecycleScope.launch {
            try {
                val data = SupabaseManager.client.postgrest["carousel_items"]
                    .select()
                    .decodeList<Carousel>()

                val items = data.map {
                    Carousel(it.id, it.image_url)
                }

                setupCarousel(items)
            } catch (e: Exception) {
                Log.e("Supabase", "Refetch error: ${e.message}")
            }
        }
    }

    private fun setupCarousel(items: List<Carousel>) {
        carouselAdapter.updateData(items)

        progressBar.visibility = View.GONE
        viewPager.visibility = View.VISIBLE

        setupIndicators(items.size)
        viewPager.setCurrentItem(0, false)

        handler.removeCallbacks(autoScrollRunnable)
        handler.postDelayed(autoScrollRunnable, 3000)
    }

    override fun onResume() {
        super.onResume()
        if (::carouselAdapter.isInitialized && carouselAdapter.itemCount > 0) {
            handler.postDelayed(autoScrollRunnable, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch {
            realtimeChannel?.unsubscribe()
        }
        handler.removeCallbacksAndMessages(null)
    }

    private fun setupIndicators(count: Int) {
        indicatorContainer.removeAllViews()

        for (i in 0 until count) {
            val indicator = ImageView(requireContext())
            indicator.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 0, 8, 0) }

            indicator.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.indicator_inactive
                )
            )

            indicatorContainer.addView(indicator)
        }

        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        val max = indicatorContainer.childCount - 1
        val safePos = position.coerceAtMost(max)

        for (i in 0 until indicatorContainer.childCount) {
            val iv = indicatorContainer.getChildAt(i) as ImageView
            iv.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (i == safePos) R.drawable.indicator_active
                    else R.drawable.indicator_inactive
                )
            )
        }
    }

    // ========== FUNGSI AMBIL NAMA ==========
    private fun ambilNamaPendek(nama: String): String {
        val clean = nama.trim()
        val parts = clean.split("\\s+".toRegex())
        return if (parts.size >= 2) parts[0] else clean
    }
}
