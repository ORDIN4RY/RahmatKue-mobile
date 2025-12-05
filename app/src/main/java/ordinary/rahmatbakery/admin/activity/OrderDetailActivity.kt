package ordinary.rahmatbakery.admin.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.adapter.OrderDetailPagerAdapter
import ordinary.rahmatbakery.admin.activity.OrderRepository
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.model.OrderAdmin
import ordinary.rahmatbakery.CancelOrderDialog

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnProcess: MaterialButton
    private lateinit var btnReady: MaterialButton
    private lateinit var btnComplete: MaterialButton
    private lateinit var btnClose: MaterialButton
    private lateinit var tabTitles: Array<String>

    private lateinit var orderRepository: OrderRepository
    private lateinit var pagerAdapter: OrderDetailPagerAdapter

    var order: OrderAdmin? = null
        private set
    // Public untuk diakses fragment
    private var orderId: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        orderId = intent.getStringExtra("ORDER_ID")



        if (orderId == null) {
            Toast.makeText(this, "ID Pesanan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        initViews()
        setupToolbar()
        loadOrderDetail()
//        lifecycleScope.launch {
//            try {
//                order = OrderRepository().getOrderById(orderId!!)!!
//                // order sudah bisa dipakai di sini
//                updateUI(order)
//            } catch (e: Exception) {
//                Toast.makeText(this@OrderDetailActivity, "Gagal memuat order", Toast.LENGTH_SHORT).show()
//            }
//        }

    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        layoutActions = findViewById(R.id.layoutActions)
        btnCancel = findViewById(R.id.btnCancel)
        btnProcess = findViewById(R.id.btnProcess)
        btnComplete = findViewById(R.id.btnComplete)
        btnReady = findViewById(R.id.btnReady)
        btnClose = findViewById(R.id.btnClose)

        orderRepository = OrderRepository()
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = OrderDetailPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun loadOrderDetail() {
        lifecycleScope.launch {
            try {
                val fetchedOrder= orderRepository.getOrderById(orderId!!) ?: run {

                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Pesanan tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                order = fetchedOrder

                tabTitles = arrayOf(
                        "Informasi Umum",
                        "Detail Produk/Paket",
                        "Alamat",
                        "Pembatalan"
                )
                setupViewPager()
                setupActionButtons()


            } catch (e: Exception) {
                Toast.makeText(
                    this@OrderDetailActivity,
                    "Gagal memuat detail pesanan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupActionButtons() {
        when (order?.status) {
            "Menunggu Pembayaran" -> {
                // Bisa batalkan dan proses
                btnCancel.visibility = View.VISIBLE
                btnProcess.visibility = View.GONE
                btnReady.visibility = View.GONE
                btnComplete.visibility = View.GONE
                btnClose.visibility = View.GONE
                layoutActions.visibility = View.VISIBLE
            }"Menunggu Diproses" -> {
                // Bisa batalkan dan proses
                btnCancel.visibility = View.VISIBLE
                btnProcess.visibility = View.VISIBLE
                btnComplete.visibility = View.GONE
            btnReady.visibility = View.GONE
                btnClose.visibility = View.GONE
                layoutActions.visibility = View.VISIBLE
            }
            "Sedang Diproses" -> {
                // Bisa batalkan dan selesaikan
                btnCancel.visibility = View.GONE
                btnProcess.visibility = View.GONE
                btnComplete.visibility = View.VISIBLE
                btnReady.visibility = View.VISIBLE
                btnClose.visibility = View.GONE
                layoutActions.visibility = View.VISIBLE
            }
            "Siap Diambil/Diantar" -> {
                // Bisa batalkan dan selesaikan
                btnCancel.visibility = View.GONE
                btnProcess.visibility = View.GONE
                btnComplete.visibility = View.VISIBLE
                btnClose.visibility = View.GONE
                btnReady.visibility = View.GONE
                layoutActions.visibility = View.VISIBLE
            }
            "Selesai", "Dibatalkan" -> {
                // Hanya tombol tutup
                layoutActions.visibility = View.GONE
                btnClose.visibility = View.VISIBLE
            }
        }

        btnCancel.setOnClickListener { showCancelDialog() }
        btnProcess.setOnClickListener { processOrder() }
        btnComplete.setOnClickListener { completeOrder() }
        btnClose.setOnClickListener { finish() }
    }

    private fun showCancelDialog() {
        val dialog = CancelOrderDialog(this) { alasan ->
            cancelOrder(alasan)
        }
        dialog.show()
    }

    private fun cancelOrder(alasan: String) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin membatalkan pesanan ini?")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val adminId = SupabaseManager.client.auth.currentUserOrNull()?.id

                        if (adminId == null) {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Anda harus login sebagai admin",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        val success = orderRepository.cancelOrder(orderId!!, alasan, adminId)

                        if (success) {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Pesanan berhasil dibatalkan",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Reload order
                            loadOrderDetail()
                        } else {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Gagal membatalkan pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun processOrder() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Proses pesanan ini?")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val success = orderRepository.processOrder(orderId!!)

                        if (success) {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Pesanan berhasil diproses",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadOrderDetail()
                        } else {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Gagal memproses pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun completeOrder() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Selesaikan pesanan ini? Pastikan barang sudah diterima pelanggan.")
            .setPositiveButton("Ya") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val success = orderRepository.completeOrder(orderId!!)

                        if (success) {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Pesanan berhasil diselesaikan",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadOrderDetail()
                        } else {
                            Toast.makeText(
                                this@OrderDetailActivity,
                                "Gagal menyelesaikan pesanan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@OrderDetailActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}