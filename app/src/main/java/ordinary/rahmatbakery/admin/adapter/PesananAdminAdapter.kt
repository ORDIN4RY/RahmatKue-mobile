package ordinary.rahmatbakery.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.model.PesananAdmin


class PesananAdminAdapter(private var pesananList: List<PesananAdmin>) :
    RecyclerView.Adapter<PesananAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvWaktu: TextView = view.findViewById(R.id.tvWaktu)
        val tvNomor: TextView = view.findViewById(R.id.tvNomor)
        val tvJenis: TextView = view.findViewById(R.id.tvJenis)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pesanan = pesananList[position]
        holder.tvId.text = pesanan.id.toString()
        holder.tvTanggal.text = pesanan.tanggal
        holder.tvWaktu.text = pesanan.waktu
        holder.tvNomor.text = pesanan.nomor
        holder.tvJenis.text = pesanan.jenis
        holder.tvStatus.text = pesanan.status

        // Set warna status
        val statusColor = when (pesanan.status.lowercase()) {
            "selesai", "pesanan selesai" -> 0xFF4CAF50.toInt()
            "dibatalkan", "batal" -> 0xFFF44336.toInt()
            else -> 0xFFFF9800.toInt()
        }
        holder.tvStatus.setTextColor(statusColor)
    }

    override fun getItemCount() = pesananList.size

    fun updateData(newList: List<PesananAdmin>) {
        pesananList = newList
        notifyDataSetChanged()
    }
}
