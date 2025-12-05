package ordinary.rahmatbakery.pelanggan.fragment

import android.content.Intent
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
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.pelanggan.activity.DashboardActivity
import ordinary.rahmatbakery.pelanggan.adapter.LastOrderAdapter
import ordinary.rahmatbakery.pelanggan.adapter.CarouselAdapter
import ordinary.rahmatbakery.util.SupabaseManager
import android.widget.ImageView
import android.widget.RelativeLayout
import coil.load
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.RealtimeChannel
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.activity.DetailPromoActivity
import ordinary.rahmatbakery.pelanggan.adapter.MenuTerbaruAdapter
import ordinary.rahmatbakery.pelanggan.adapter.PromoAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import ordinary.rahmatbakery.pelanggan.model.Produk
import ordinary.rahmatbakery.pelanggan.model.Promo

class BerandaFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var carouselAdapter: CarouselAdapter
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private var realtimeChannel: RealtimeChannel? = null
    private val listLastOrder = mutableListOf<Keranjang>()
    private lateinit var lastOrderAdapter: LastOrderAdapter

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
    private lateinit var cardMenuTerbaru: CardView
    private lateinit var cardPromoTerbaru: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var rvPromoTerbaru: ImageView
    private lateinit var promoAdapter: PromoAdapter
    private val listPromo = mutableListOf<Promo>()
    private lateinit var rvMenuTerbaru: ImageView
    private lateinit var menuTerbaruAdapter: MenuTerbaruAdapter
    private val listProduk = mutableListOf<Produk>()

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
        rvPromoTerbaru = rootView.findViewById(R.id.iv_promo_terbaru)
        cardPromoTerbaru = rootView.findViewById(R.id.card_promo_terbaru)

        rvMenuTerbaru = rootView.findViewById(R.id.iv_menu_terbaru)
        cardMenuTerbaru = rootView.findViewById(R.id.card_menu_terbaru)

         val nameText = rootView.findViewById<TextView>(R.id.nickname)
        val pointText = rootView.findViewById<TextView>(R.id.user_point)

        val username = parentActivity?.profile?.username
        val point = parentActivity?.profile?.point

        nameText.text = "Hi, ${username?.let { ambilNamaPendek(it) } ?: "Pengguna"} !"
        pointText.text = "${point ?: 0} Points"

        recyclerView = rootView.findViewById(R.id.rvPesananTerakhir)
        lastOrderAdapter = LastOrderAdapter(listLastOrder)
        recyclerView.adapter = lastOrderAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        viewPager = rootView.findViewById(R.id.carousel_view_pager)
        indicatorContainer = rootView.findViewById(R.id.carousel_indicator_container)
        progressBar = rootView.findViewById(R.id.progress_bar)

        carouselAdapter = CarouselAdapter(emptyList())
        viewPager.adapter = carouselAdapter

        progressBar.visibility = View.VISIBLE
        viewPager.visibility = View.GONE

//        fetchAndSubscribeCarouselData()
        loadSinglePromoTerbaru()
        loadLastOrder()
        refetchCarouselData()
        loadSingleProdukTerbaru()
        fetchAndSubscribeCarouselData()
        return rootView
    }

    private fun loadSingleProdukTerbaru() {
        lifecycleScope.launch {
            try {
                // Ambil satu produk terbaru dari tabel 'produk'
                val produkTerbaru = SupabaseManager.client.postgrest["produk"]
                    .select (Columns.list(
                        "id: id_produk",
                        "nama : nama_produk",
                        "gambar : foto_produk",
                        "kategori:id_kategori(id : id_kategori, nama:nama_kategori,minimal_pembelian)",
                        "varian",
                        "deskripsi",
                        "harga",
                        "diskon",
                        "created_at",
                         )){
                        order("created_at", Order.DESCENDING)
                        limit(1)
                        single()
                    }.decodeAs<Produk>()// Ambil item pertama, atau null jika kosong

                // Jika produk berhasil didapatkan
                if (produkTerbaru != null) {
                    Log.d("PRODUK_TERBARU", "Berhasil memuat produk: ${produkTerbaru.nama}")

                    // Tampilkan CardView dan muat gambarnya
                    cardMenuTerbaru.visibility = View.VISIBLE
                    rvMenuTerbaru.load(produkTerbaru.gambar) { // 'gambar' sesuai nama properti di model Produk
                        crossfade(true)
                        placeholder(R.drawable.placeholder)
                        error(R.drawable.error_image)
                    }

                    // Atur aksi klik untuk membuka halaman detail
                    cardMenuTerbaru.setOnClickListener {
                        // Anda perlu membuat DetailProdukActivity
                        val intent = Intent(context, DetailProdukActivity::class.java)
                        // Kirim objek produk ke activity detail
                        intent.putExtra("data_produk", produkTerbaru)
                        startActivity(intent)
                    }
                } else {
                    // Jika tidak ada produk yang ditemukan
                    Log.w("PRODUK_TERBARU", "Tidak ada produk yang bisa ditampilkan.")
                    cardMenuTerbaru.visibility = View.GONE // Sembunyikan card
                }

            } catch (e: Exception) {
                // Jika terjadi error saat mengambil data
                Log.e("PRODUK_TERBARU", "Gagal memuat produk tunggal: ${e.message}", e)
                cardMenuTerbaru.visibility = View.GONE // Sembunyikan card
            }
        }
    }
    private fun loadSinglePromoTerbaru() {
        lifecycleScope.launch {
            try {
                val promo = SupabaseManager.client.postgrest["promo"]
                    .select {
                        order("created_at", Order.DESCENDING) // Urutkan berdasarkan tanggal dibuat
                        limit(1) // Ambil hanya 1 data
                        single() // Ambil sebagai satu objek, bukan list
                    }.decodeAs<Promo>() // Gunakan decodeAs, bukan decodeList

                rvPromoTerbaru.load(promo.fotoSquare) {
                    crossfade(true)
                    error(R.drawable.error_image)
                }

                // Tambahkan OnClickListener untuk membuka detail
                cardPromoTerbaru.setOnClickListener {
                    val intent = Intent(requireContext(), DetailPromoActivity::class.java)
                    intent.putExtra("data_promo_terbaru", promo)
                    startActivity(intent)
                }

            } catch (e: Exception) {
                Log.e("BerandaFragment", "Gagal memuat promo tunggal: ${e.message}")
                cardPromoTerbaru.visibility = View.GONE // Sembunyikan jika gagal
            }
        }
    }
    private fun loadLastOrder(){

        lifecycleScope.launch {
            try {
                val lastOrder = SupabaseManager.client.postgrest.rpc(
                    "get_last_order_items",
                    mapOf("uid" to parentActivity?.profile?.id)
                ).decodeList<Keranjang>()

                println(lastOrder)


//                if (lastOrder.isEmpty()){
//                    requireView().findViewById<TextView>(R.id.orderAlt).visibility= View.VISIBLE
//                    requireView().findViewById<RelativeLayout>(R.id.lastOrder).visibility = View.GONE
//                }else{
//                requireView().findViewById<TextView>(R.id.orderAlt).visibility= View.GONE
//                    requireView().findViewById<RelativeLayout>(R.id.lastOrder).visibility = View.VISIBLE
//
//                }
                listLastOrder.clear()
                listLastOrder.addAll(lastOrder)
                lastOrderAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
            }
        })
    }

    private fun fetchAndSubscribeCarouselData() {
        // The user ID is fetched but not used, you might want to check if this is intended.
        val userId = parentActivity?.profile?.id ?: return

        progressBar.visibility = View.VISIBLE
        viewPager.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // Fetch data from the "promo" table
                val promos = SupabaseManager.client.postgrest["promo"]
                    .select()
                    .decodeList<Promo>()

                // Map the fetched 'Promo' objects to 'Carousel' objects
                val items = promos.mapNotNull { promo ->
                    promo.fotoBanner?.let { bannerUrl ->
                        // Assuming Carousel model is: data class Carousel(val id: String, val image_url: String)
                        Promo(promo.id, promo.fotoBanner)
                    }
                }

                // Now call setupCarousel with the list of Carousel items
                setupCarousel(items)

            } catch (e: Exception) {
                Log.e("Supabase", "Fetch carousel failed: ${e.message}")
                progressBar.visibility = View.GONE
            }
        }
    }



    private fun refetchCarouselData() {
        lifecycleScope.launch {
            try {
                val data = SupabaseManager.client.postgrest["promo"]
                    .select()
                    .decodeList<Promo>()

                val items = data.map {
                    Promo(it.id, it.fotoBanner)
                }

                setupCarousel(items)
            } catch (e: Exception) {
                Log.e("Supabase", "Refetch error: ${e.message}")
            }
        }
    }

    private fun setupCarousel(items: List<Promo>) {
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
