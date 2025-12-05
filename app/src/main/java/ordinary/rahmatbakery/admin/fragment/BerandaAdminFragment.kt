package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope

import coil.load
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.activity.DashboardRepository
import ordinary.rahmatbakery.model.DashboardStats
import ordinary.rahmatbakery.model.PesananTerakhir
import ordinary.rahmatbakery.model.ProdukTerlaris
import java.text.NumberFormat
import java.util.Locale

class BerandaAdminFragment : Fragment() {

    private val repository = DashboardRepository()

    // Views
    private lateinit var tvTotalPesanan: TextView
    private lateinit var tvTotalPemasukan: TextView
    private lateinit var tvSelesai: TextView
    private lateinit var tvProses: TextView
    private lateinit var tvBatal: TextView
    private lateinit var layoutPesananTerbaru: LinearLayout
    private lateinit var layoutProdukTerlaris: LinearLayout
    private var progressBar: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        loadDashboardData()
    }

    private fun initViews(view: View) {
        tvTotalPesanan = view.findViewById(R.id.tv_total_pesanan)
        tvTotalPemasukan = view.findViewById(R.id.tv_total_pemasukan)
        tvSelesai = view.findViewById(R.id.tv_selesai)
        tvProses = view.findViewById(R.id.tv_proses)
        tvBatal = view.findViewById(R.id.tv_batal)
        layoutPesananTerbaru = view.findViewById(R.id.layout_pesanan_terbaru)
        layoutProdukTerlaris = view.findViewById(R.id.layout_produk_terlaris)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun loadDashboardData() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Load statistik dashboard
                val stats = repository.getDashboardStats()
                if (isAdded) updateDashboardStats(stats)

                // Load pesanan terakhir
                val pesanan = repository.getPesananTerakhir()
                if (isAdded) updatePesananTerakhir(pesanan)

                // Load produk terlaris
                val produk = repository.getProdukTerlaris()
                if (isAdded) updateProdukTerlaris(produk)

            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded) showError("Gagal memuat data: ${e.message}")
            } finally {
                if (isAdded) showLoading(false)
            }
        }
    }

    private fun updateDashboardStats(stats: DashboardStats) {
        tvTotalPesanan.text = stats.totalPesanan.toString()
        tvTotalPemasukan.text = formatRupiah(stats.totalPemasukan)
        tvSelesai.text = "Selesai : ${stats.selesai}"
        tvProses.text = "Proses : ${stats.proses}"
        tvBatal.text = "Dibatalkan : ${stats.batal}"
    }

    private fun updatePesananTerakhir(pesananList: List<PesananTerakhir>) {
        layoutPesananTerbaru.removeViews(2, layoutPesananTerbaru.childCount - 2)

        if (pesananList.isEmpty()) {
            layoutPesananTerbaru.addView(createEmptyView("Belum ada pesanan"))
            return
        }

        pesananList.forEach { pesanan ->
            createPesananRow(pesanan)?.let { row ->
                layoutPesananTerbaru.addView(row)
            }
        }
    }

    private fun createPesananRow(pesanan: PesananTerakhir): LinearLayout? {
        if (!isAdded) return null

        val rowLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }

        fun createTextView(text: String, weight: Float): TextView {
            return TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weight
                )
                this.text = text
                textSize = 12f
                setTextColor(resources.getColor(android.R.color.black, null))
            }
        }

        rowLayout.addView(createTextView(pesanan.id, 0.5f))
        rowLayout.addView(createTextView(pesanan.tglPesan, 1.5f))
        rowLayout.addView(createTextView(pesanan.tglSelesai ?: "-", 1.5f))
        rowLayout.addView(createTextView(pesanan.nama, 1f))
        rowLayout.addView(createTextView(pesanan.jumlahItem.toString(), 1f))
        rowLayout.addView(createTextView(formatRupiahShort(pesanan.totalHarga), 1.5f))

        return rowLayout
    }

    private fun updateProdukTerlaris(produkList: List<ProdukTerlaris>) {
        layoutProdukTerlaris.removeAllViews()

        if (produkList.isEmpty()) {
            layoutProdukTerlaris.addView(createEmptyView("Belum ada data produk"))
            return
        }

        produkList.forEach { produk ->
            if (!isAdded) return@forEach

            val itemView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_produk_terlaris, layoutProdukTerlaris, false)

            val imgProduk = itemView.findViewById<ImageView>(R.id.img_produk)
            val tvNamaProduk = itemView.findViewById<TextView>(R.id.tv_nama_produk)
            val tvJumlahTerjual = itemView.findViewById<TextView>(R.id.tv_jumlah_terjual)

            imgProduk.load(produk.fotoProduk) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvNamaProduk.text = produk.namaProduk
            tvJumlahTerjual.text = produk.jumlahTerjual.toString()

            layoutProdukTerlaris.addView(itemView)
        }
    }

    private fun createEmptyView(message: String): TextView? {
        if (!isAdded) return null

        return TextView(requireContext()).apply {
            text = message
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            setPadding(16, 32, 16, 32)
            gravity = Gravity.CENTER
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    private fun formatRupiahShort(amount: Int): String {
        return when {
            amount >= 1_000_000 -> "${amount / 1_000_000}jt"
            amount >= 1_000 -> "${amount / 1_000}k"
            else -> amount.toString()
        }
    }
}
