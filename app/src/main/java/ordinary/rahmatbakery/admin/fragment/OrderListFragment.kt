package ordinary.rahmatbakery.admin.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.adapter.OrderAdapter
import ordinary.rahmatbakery.admin.activity.OrderDetailActivity
import ordinary.rahmatbakery.admin.activity.OrderRepository
import ordinary.rahmatbakery.model.OrderAdmin

class OrderListFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var orderAdapter: OrderAdapter
    private lateinit var orderRepository: OrderRepository

    private var allOrders = listOf<OrderAdmin>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)

        setupRecyclerView()
        setupChipFilter()
        loadOrders()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus)
        rvOrders = view.findViewById(R.id.rvOrders)
        progressBar = view.findViewById(R.id.progressBar)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)

        orderRepository = OrderRepository()
    }



    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList<OrderAdmin>()) { order ->
            val intent = Intent(requireContext(), OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.idTransaksi)
            startActivity(intent)
        }

        rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupChipFilter() {
        chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                filterOrders(null)
                return@setOnCheckedStateChangeListener
            }

            val checkedChip = requireView().findViewById<Chip>(checkedIds[0])
            val status = when (checkedChip.id) {
                R.id.chipWaiting -> "Menunggu Pembayaran"
                R.id.chipWaitingProcces -> "Menunggu Diproses"
                R.id.chipProcess -> "Sedang Diproses"
                R.id.chipReady -> "Siap Diambil/Diantar"
                R.id.chipCompleted -> "Selesai"
                R.id.chipCancelled -> "Dibatalkan"
                else -> null
            }

            filterOrders(status)
        }
    }

    private fun loadOrders() {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                allOrders = orderRepository.getAllOrders()

                if (allOrders.isEmpty()) {
                    showEmpty(true)
                } else {
                    showEmpty(false)
                    orderAdapter.updateOrders(allOrders)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat pesanan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun filterOrders(status: String?) {
        val filtered = if (status == null) {
            allOrders
        } else {
            allOrders.filter { it.status == status }
        }

        if (filtered.isEmpty()) {
            showEmpty(true)
        } else {
            showEmpty(false)
            orderAdapter.updateOrders(filtered)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvOrders.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showEmpty(show: Boolean) {
        layoutEmpty.visibility = if (show) View.VISIBLE else View.GONE
        rvOrders.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }
}