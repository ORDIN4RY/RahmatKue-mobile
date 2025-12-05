package ordinary.rahmatbakery.admin.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ordinary.rahmatbakery.databinding.FragmentBerandaAdminBinding
import ordinary.rahmatbakery.util.SupabaseManager
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.activity.model.ProdukTerlaris
import ordinary.rahmatbakery.admin.activity.model.Transaksi
import ordinary.rahmatbakery.admin.activity.model.TransaksiLatest
import java.text.NumberFormat
import java.util.Locale

class BerandaAdminFragment : Fragment() {
//
//    private lateinit var tvTotalPesanan: TextView
//    private lateinit var tvTotalPemasukan: TextView
//
//    private lateinit var tvSelesai: TextView
//    private lateinit var tvProses: TextView
//    private lateinit var tvBatal: TextView
//
//    private lateinit var layoutPesananTerbaru: LinearLayout
//    private lateinit var layoutProdukTerlaris: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beranda_admin, container, false)
    }
//    private val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
//        maximumFractionDigits = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        // ---------------------
//        // FIND VIEW BY ID
//        // ---------------------
//        tvTotalPesanan = view.findViewById(R.id.tv_total_pesanan)
//        tvTotalPemasukan = view.findViewById(R.id.tv_total_pemasukan)
//
//        tvSelesai = view.findViewById(R.id.tv_selesai)
//        tvProses = view.findViewById(R.id.tv_proses)
//        tvBatal = view.findViewById(R.id.tv_batal)
//
//        layoutPesananTerbaru = view.findViewById(R.id.layout_pesanan_terbaru)
//        layoutProdukTerlaris = view.findViewById(R.id.layout_produk_terlaris)
//
//        // ---------------------
//        // LOAD DATA
//        // ---------------------
//        loadDashboardData()
    }}

//
//    private fun loadDashboardData() {
//        lifecycleScope.launch {
//            try {
//                loadTotalPesanan()
//                loadTotalPemasukan()
////                loadStatistikPesanan()
////                loadPesananTerbaru()
//                loadProdukTerlaris()
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    // -----------------------------------------------------
    // 1. TOTAL PESANAN
    // -----------------------------------------------------
//    private suspend fun loadTotalPesanan() {
//        val list = SupabaseManager.client.postgrest
//            .from("transaksi")
//            .select {
//                filter { eq("status", "Selesai") }
//            }
//            .decodeList<Transaksi>()
//
//        tvTotalPesanan.text = list.size.toString()
//    }
//
//    // -----------------------------------------------------
//    // 2. TOTAL PEMASUKAN
//    // -----------------------------------------------------
//    private suspend fun loadTotalPemasukan() {
//        val list = SupabaseManager.client.postgrest
//            .from("transaksi")
//            .select{
//                filter { eq("status", "Selesai") }
//            }
//            .decodeList<Transaksi>()
//
//        val total = list.sumOf { it.total_harga }
//        tvTotalPemasukan.text = formatRupiah.format(total)
//    }
//
////     -----------------------------------------------------
////     3. STATISTIK PESANAN
////    // -----------------------------------------------------
//    private suspend fun loadStatistikPesanan() {
//        val selesai = countStatus("Selesai")
//        val proses = countStatus("Diproses")
//        val batal = countStatus("Dibatalkan")
//
//        tvSelesai.text = selesai.toString()
//        tvProses.text = proses.toString()
//        tvBatal.text = batal.toString()
//    }
//
//    private suspend fun countStatus(status: String): Int {
//        val r = SupabaseManager.client.postgrest
//            .from("transaksi")
//            .select(){
//                filter {
//                    eq("status", status)
//                }
//            }
//
//
//        return r.count ?: 0
//    }
//
    // -----------------------------------------------------
    // 4. PESANAN TERBARU
    // -----------------------------------------------------
//    private suspend fun loadPesananTerbaru() {
//        layoutPesananTerbaru.removeAllViews()
//
//        val data = SupabaseManager.client.postgrest
//            .from("transaksi")
//            .select("id_transaksi, created_at, total_harga")
//            .order("created_at", Order.DESCENDING)
//            .limit(10)
//            .decodeList<TransaksiLatest>()
//
//        data.forEach { item ->
//            val tv = TextView(requireContext())
//            tv.text = "#${item.id_transaksi.take(6)} — Rp ${item.total_harga}"
//            tv.textSize = 14f
//            tv.setPadding(10, 10, 10, 10)
//            layoutPesananTerbaru.addView(tv)
//        }
//    }
//
//    // -----------------------------------------------------
//    // 5. PRODUK TERLARIS
//    // -----------------------------------------------------
//    private suspend fun loadProdukTerlaris() {
//        layoutProdukTerlaris.removeAllViews()
//
//        val data = SupabaseManager.client.postgrest
//            .rpc("get_produk_terlaris") // atau query manual
//            .decodeList<ProdukTerlaris>()
//
//        data.forEach { item ->
//            val tv = TextView(requireContext())
//            tv.text = "${item.nama_produk} — ${item.total_terjual} terjual"
//            tv.textSize = 14f
//            tv.setPadding(10, 10, 10, 10)
//            layoutProdukTerlaris.addView(tv)
//        }
// }
//}

