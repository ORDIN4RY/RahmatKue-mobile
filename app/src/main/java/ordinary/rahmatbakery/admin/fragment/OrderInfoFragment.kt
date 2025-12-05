package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.adapter.OrderProductAdapter
import ordinary.rahmatbakery.admin.activity.OrderDetailActivity
import ordinary.rahmatbakery.model.OrderAdmin
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// Tab 1: Informasi Umum
class OrderInfoFragment : Fragment() {

    companion object {
        private const val ARG_ORDER = "order"

        fun newInstance(order: OrderAdmin): OrderInfoFragment {
            val fragment = OrderInfoFragment()
            val args = Bundle()
            args.putString(ARG_ORDER, order.toString()) // Atau gunakan Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var order: OrderAdmin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_order_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get order from parent activity
        order = (activity as? OrderDetailActivity)?.order ?: return

        setupViews(view)
    }

    private fun setupViews(view: View) {
        view.findViewById<TextView>(R.id.tvNomorPesanan).text = order.nomorPesanan ?: "-"
        view.findViewById<TextView>(R.id.tvTanggalDibuat).text = formatDate(order.createdAt)
        view.findViewById<TextView>(R.id.tvWaktuSelesai).text = formatDate(order.waktuSelesai)
        view.findViewById<TextView>(R.id.tvMetodePengambilan).text = order.metodePengambilan
        view.findViewById<TextView>(R.id.tvCatatan).text = order.catatan ?: "-"
        view.findViewById<TextView>(R.id.tvVoucher).text = if (order.idVoucher != null) "Ya" else "-"

        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        tvStatus.text = order.status
        setStatusBackground(tvStatus, order.status)
    }

    private fun setStatusBackground(textView: TextView, status: String) {
        when (status) {
            "Menunggu Pembayaran" -> textView.setBackgroundResource(R.drawable.bg_status_waiting)
            "Menunggu Diproses" -> textView.setBackgroundResource(R.drawable.bg_status_waiting)
            "Sedang Diproses" -> textView.setBackgroundResource(R.drawable.bg_status_proses)
            "Siap diambil/diantar" -> textView.setBackgroundResource(R.drawable.bg_status_proses)
            "Selesai" -> textView.setBackgroundResource(R.drawable.bg_status_selesai)
            "Dibatalkan" -> textView.setBackgroundResource(R.drawable.bg_status_cancel)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}

// Tab 2: Detail Produk/Paket

// Tab 3: Alamat

//// Tab 4: Pembayaran
//class OrderPaymentFragment : Fragment() {
//
//    private lateinit var order: OrderAdmin
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.tab_payment, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        order = (activity as? OrderDetailActivity)?.order ?: return
//
//        view.findViewById<TextView>(R.id.tvTotalHarga).text = formatRupiah(order.totalHarga)
//        view.findViewById<TextView>(R.id.tvPotongan).text = formatRupiah(order.potongan)
//        view.findViewById<TextView>(R.id.tvOngkir).text = formatRupiah(order.ongkir)
//        view.findViewById<TextView>(R.id.tvDpMinimal).text = formatRupiah(order.dpMinimal)
//
//        if(order.dpMinimal==order.totalHarga){
//            view.findViewById<TextView>(R.id.tvDpMinimal).text = "Rp 0,00"
//
//        }
//    }


//}

// Tab 5: Pembatalan

