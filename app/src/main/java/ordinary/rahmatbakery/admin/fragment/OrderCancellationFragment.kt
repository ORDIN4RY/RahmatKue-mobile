package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.activity.OrderDetailActivity
import ordinary.rahmatbakery.model.OrderAdmin
import java.text.SimpleDateFormat
import java.util.Locale

class OrderCancellationFragment : Fragment() {

    private lateinit var order: OrderAdmin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order = (activity as? OrderDetailActivity)?.order ?: return

        val cancellation = order.pembatalan?.firstOrNull()
        val layoutCancellation = view.findViewById<LinearLayout>(R.id.layoutCancellation)
        val tvNoCancellation = view.findViewById<TextView>(R.id.tvNoCancellation)

        if (cancellation != null) {
            layoutCancellation.visibility = View.VISIBLE
            tvNoCancellation.visibility = View.GONE

            view.findViewById<TextView>(R.id.tvTipeBatal).text = cancellation.tipe ?: "-"
            view.findViewById<TextView>(R.id.tvTanggalBatal).text =
                formatDate(cancellation.dibuatPada)
            view.findViewById<TextView>(R.id.tvAlasanBatal).text = cancellation.alasan ?: "-"
        } else {
            layoutCancellation.visibility = View.GONE
            tvNoCancellation.visibility = View.VISIBLE
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
        }}
