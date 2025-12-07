package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.adapter.PesananAdminAdapter
import ordinary.rahmatbakery.admin.adapter.ProdukAdapter
import ordinary.rahmatbakery.admin.activity.DashboardRepository
import ordinary.rahmatbakery.admin.model.PesananAdmin
import ordinary.rahmatbakery.admin.model.Produk
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BerandaAdminFragment : Fragment() {

    // Views
    private lateinit var tvPromoAktif: TextView
    private lateinit var tvMenungguKonfirmasi: TextView
    private lateinit var tvActivityLog: TextView
    private lateinit var tvSelesai: TextView
    private lateinit var tvProses: TextView
    private lateinit var tvDibatalkan: TextView
    private lateinit var tvTotalOrder: TextView
    private lateinit var rvPesananTerbaru: RecyclerView
    private lateinit var rvProdukTerlaris: RecyclerView

    private lateinit var pieChartView: ordinary.rahmatbakery.admin.view.PieChartView

    // Filter Views
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var btnApplyFilter: Button

    // Adapters
    private lateinit var pesananAdapter: PesananAdminAdapter
    private lateinit var produkAdapter: ProdukAdapter
    private lateinit var activityLogAdapter: ordinary.rahmatbakery.admin.adapter.ActivityLogAdapter

    // Filter Data
    private var selectedBulan: Int = 0 // 0 = Semua Bulan
    private var selectedTahun: Int = Calendar.getInstance().get(Calendar.YEAR)

    // Bulan List
    private val bulanList = listOf(
        "Semua Bulan",
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

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
        setupRecyclerViews()
        setupFilterSpinners()
        loadAllData()
    }

    private fun initViews(view: View) {
        tvPromoAktif = view.findViewById(R.id.tvPromoAktif)
        tvMenungguKonfirmasi = view.findViewById(R.id.tvMenungguKonfirmasi)

        tvSelesai = view.findViewById(R.id.tvSelesai)
        tvProses = view.findViewById(R.id.tvProses)
        tvDibatalkan = view.findViewById(R.id.tvDibatalkan)
        tvTotalOrder = view.findViewById(R.id.tvTotalOrder)
        rvPesananTerbaru = view.findViewById(R.id.rvPesananTerbaru)
        rvProdukTerlaris = view.findViewById(R.id.rvProdukTerlaris)
         pieChartView = view.findViewById(R.id.pieChartView)

        // Filter views
        spinnerBulan = view.findViewById(R.id.spinnerBulan)
        spinnerTahun = view.findViewById(R.id.spinnerTahun)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)
    }

    private fun setupRecyclerViews() {
        // Setup RecyclerView Pesanan
        pesananAdapter = PesananAdminAdapter(emptyList())
        rvPesananTerbaru.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pesananAdapter
            setHasFixedSize(true)
        }

        // Setup RecyclerView Produk
        produkAdapter = ProdukAdapter(emptyList())
        rvProdukTerlaris.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = produkAdapter
            setHasFixedSize(true)
        }


    }

    private fun setupFilterSpinners() {
        // Setup Spinner Bulan
        val bulanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bulanList
        )
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = bulanAdapter

        // Setup Spinner Tahun (5 tahun terakhir + 2 tahun ke depan)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList = (currentYear - 5..currentYear + 2).toList()
        val tahunAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tahunList
        )
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter = tahunAdapter

        // Set default tahun ke tahun sekarang
        val currentYearIndex = tahunList.indexOf(currentYear)
        if (currentYearIndex >= 0) {
            spinnerTahun.setSelection(currentYearIndex)
        }

        // Button Apply Filter
        btnApplyFilter.setOnClickListener {
            selectedBulan = spinnerBulan.selectedItemPosition
            selectedTahun = spinnerTahun.selectedItem.toString().toInt()

            // Show loading toast
            Toast.makeText(
                requireContext(),
                "Memuat data ${bulanList[selectedBulan]} $selectedTahun...",
                Toast.LENGTH_SHORT
            ).show()

            // Reload data with filter
            loadAllData()
        }
    }

    private fun loadAllData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loadCardStats()
                loadOrderStats()
                loadRecentOrders()
                loadTopProducts()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error loading data: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadCardStats() {
        try {
            // Load Promo Aktif
            val promoAktif = DashboardRepository.getPromoAktifCount()

            // Load Order Menunggu Konfirmasi
            val menungguKonfirmasi = DashboardRepository.getMenungguKonfirmasiCount(
                bulan = if (selectedBulan == 0) null else selectedBulan,
                tahun = selectedTahun
            )

            // Load Activity Count
            val activityCount = DashboardRepository.getActivityLogCount(
                bulan = if (selectedBulan == 0) null else selectedBulan,
                tahun = selectedTahun
            )

            withContext(Dispatchers.Main) {
                tvPromoAktif.text = promoAktif.toString()
                tvMenungguKonfirmasi.text = menungguKonfirmasi.toString()
                tvActivityLog.text = activityCount.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadOrderStats() {
        try {
            val stats = DashboardRepository.getOrderStatistics(
                bulan = if (selectedBulan == 0) null else selectedBulan,
                tahun = selectedTahun
            )

            withContext(Dispatchers.Main) {
                tvSelesai.text = "Selesai : ${stats.selesai}"
                tvProses.text = "Proses : ${stats.proses}"
                tvDibatalkan.text = "Dibatalkan : ${stats.dibatalkan}"

                val total = stats.selesai + stats.proses + stats.dibatalkan
                tvTotalOrder.text = "Total Order : $total"

                // Update Pie Chart dengan data real
                pieChartView.setData(stats.selesai, stats.proses, stats.dibatalkan)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadRecentOrders() {
        try {
            val orders = DashboardRepository.getRecentOrders(
                bulan = if (selectedBulan == 0) null else selectedBulan,
                tahun = selectedTahun
            )

            val pesananList = orders.mapIndexed { index, transaksi ->
                val dateTime = formatDate(transaksi.createdAt)
                val tanggal = dateTime.first
                val waktu = dateTime.second

                PesananAdmin(
                    id = index + 1,
                    tanggal = tanggal,
                    waktu = waktu,
                    nomor = transaksi.nomorPesanan ?: "N/A",
                    jenis = transaksi.metodePengambilan?.replaceFirstChar {
                        it.uppercase()
                    } ?: "N/A",
                    status = transaksi.status ?: "N/A"
                )
            }

            withContext(Dispatchers.Main) {
                pesananAdapter.updateData(pesananList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError("Gagal memuat pesanan")
            }
        }
    }

    private suspend fun loadTopProducts() {
        try {
            val products = DashboardRepository.getTopSellingProducts(
                bulan = if (selectedBulan == 0) null else selectedBulan,
                tahun = selectedTahun
            )

            val produkList = products.map { product ->
                Produk(
                    id = product.idProduk,
                    nama = product.namaProduk,
                    gambarUrl = product.fotoProduk,
                    jumlahTerjual = product.totalTerjual
                )
            }

            withContext(Dispatchers.Main) {
                produkAdapter.updateData(produkList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                showError("Gagal memuat produk terlaris")
            }
        }
    }



    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }

    /**
     * Format tanggal untuk return Pair<Tanggal, Waktu>
     */
    private fun formatDate(dateString: String?): Pair<String, String> {
        if (dateString == null) return Pair("N/A", "N/A")

        return try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            )
            val date = inputFormat.parse(dateString.replace("Z", "").replace("+00", ""))

            if (date != null) {
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                Pair(
                    dateFormat.format(date),
                    timeFormat.format(date)
                )
            } else {
                Pair("N/A", "N/A")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("N/A", "N/A")
        }
    }
}