package ordinary.rahmatbakery.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.model.OrderAdmin
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private var orders: List<OrderAdmin>,
    private val onOrderClick: (OrderAdmin) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardOrder: CardView = itemView.findViewById(R.id.cardOrder)
        val tvNomorPesanan: TextView = itemView.findViewById(R.id.tvNomorPesanan)
        val tvNamaPelanggan: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        val tvTanggalPesanan: TextView = itemView.findViewById(R.id.tvTanggalPesanan)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvTotalHarga: TextView = itemView.findViewById(R.id.tvTotalHarga)
        val tvMetodePengambilan: TextView = itemView.findViewById(R.id.tvMetodePengambilan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_admin, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvNomorPesanan.text = order.nomorPesanan ?: "-"
        holder.tvNamaPelanggan.text = order.profile?.username ?: "Pelanggan"
        holder.tvTanggalPesanan.text = formatDate(order.createdAt)
        holder.tvStatus.text = order.status
        holder.tvTotalHarga.text = formatRupiah(order.totalHarga)
        holder.tvMetodePengambilan.text = order.metodePengambilan

        // Set status color
        when (order.status) {
            "Menunggu Pembayaran" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_waiting)
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"))
            }
            "Sedang Diproses" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_proses)
                holder.tvStatus.setTextColor(Color.parseColor("#3B82F6"))
            }
            "Selesai" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                holder.tvStatus.setTextColor(Color.parseColor("#10B981"))
            }
            "Dibatalkan" -> {
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancel)
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444"))
            }
        }

        holder.cardOrder.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<OrderAdmin>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "-"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}