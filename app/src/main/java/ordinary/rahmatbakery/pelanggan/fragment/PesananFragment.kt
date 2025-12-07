package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.PesananAdapter
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.util.SupabaseManager

class PesananFragment : Fragment() {

    private lateinit var rvPesanan: RecyclerView
    private lateinit var pesananAdapter: PesananAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var emptyState: LinearLayout
    private lateinit var progressBar: ProgressBar

    private var allPesanan: List<Pesanan> = emptyList()
    private var currentFilter: String = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pesanan, container, false)

        initViews(root)
        setupRecyclerView()
        setupChipGroup()
        setupSwipeRefresh()

        loadPesanan()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali fragment visible
        // Ini akan auto-refresh setelah user bayar dan kembali ke app
        refreshData()
    }

    private fun initViews(view: View) {
        rvPesanan = view.findViewById(R.id.rv_pesanan)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        chipGroup = view.findViewById(R.id.chip_group_filter)
        emptyState = view.findViewById(R.id.empty_state)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        pesananAdapter = PesananAdapter(mutableListOf())
        rvPesanan.layoutManager = LinearLayoutManager(requireContext())
        rvPesanan.adapter = pesananAdapter
    }

    private fun setupChipGroup() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val checkedChip = group.findViewById<Chip>(checkedIds[0])
            currentFilter = when (checkedChip.id) {
                R.id.chip_semua -> "Semua"
                R.id.chip_belum_dibayar -> "Menunggu Pembayaran"
                R.id.chip_dibayar -> "Menunggu Diproses"
                R.id.chip_proses -> "Diproses"
                R.id.chip_selesai -> "Selesai"
                R.id.chip_dibatalkan -> "Dibatalkan"
                else -> "Semua"
            }

            filterPesanan()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            refreshData()
        }

        swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.primary,
            R.color.primary
        )
    }

    private fun refreshData() {
        loadPesanan(showLoading = false)
    }

    private fun loadPesanan(showLoading: Boolean = true) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (showLoading) {
                    showLoadingState()
                }

                val user = SupabaseManager.client.auth.currentUserOrNull()
                if (user == null) {
                    hideLoadingState()
                    Toast.makeText(
                        requireContext(),
                        "Sesi berakhir, silakan login kembali",
                        Toast.LENGTH_SHORT
                    ).show()
                    showEmptyState()
                    return@launch
                }

                val daftarPesanan: List<Pesanan> = SupabaseManager.client.postgrest
                    .from("transaksi")
                    .select(
                        Columns.raw(
                            """
                            id_transaksi,
                            total_harga,
                            status,
                            created_at,
                            metode_pengambilan,
                            waktu_selesai,
                            catatan,
                            ongkir,
                            nomor_pesanan,
                            dp_minimal,
                            potongan,
                            detail_transaksi_produk (
                                jumlah,
                                subtotal,
                                produk:produk (
                                    id_produk,
                                    nama_produk,
                                    harga,
                                    foto_produk,
                                    varian
                                )
                            ),
                            detail_transaksi_paket (
                                jumlah,
                                subtotal,
                                paket:paket (
                                    id_paket,
                                    nama_paket,
                                    harga_paket,
                                    foto_paket
                                )
                            )
                            """
                        )
                    ) {
                        filter { eq("id_user", user.id) }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<Pesanan>()

                allPesanan = daftarPesanan
                hideLoadingState()

                if (daftarPesanan.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                    filterPesanan()
                }

            } catch (e: Exception) {
                hideLoadingState()
                Log.e("PesananFragment", "Error loading data", e)
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                showEmptyState()
            }
        }
    }

    private fun filterPesanan() {
        val filteredList = if (currentFilter == "Semua") {
            allPesanan
        } else {
            allPesanan.filter { it.status == currentFilter }
        }

        if (isAdded) {
            if (filteredList.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                pesananAdapter.updateData(filteredList)
            }
        }
    }

    private fun showLoadingState() {
        progressBar.visibility = View.VISIBLE
        rvPesanan.visibility = View.GONE
        emptyState.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun hideLoadingState() {
        progressBar.visibility = View.GONE
        swipeRefresh.isRefreshing = false
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        rvPesanan.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyState.visibility = View.GONE
        rvPesanan.visibility = View.VISIBLE
    }
}