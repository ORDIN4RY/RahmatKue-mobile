package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.DetailVoucherActivity
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import java.text.SimpleDateFormat
import java.util.Locale

class VoucherSayaAdapter(
    private var displayList: MutableList<UserVoucher>
) : RecyclerView.Adapter<VoucherSayaAdapter.ViewHolder>() {

    private var originalList: List<UserVoucher> = listOf()

    fun getOriginalData(): List<UserVoucher> {
        return originalList
    }

    fun setOriginalData(data: List<UserVoucher>) {
        originalList = data
        displayList.clear()
        displayList.addAll(originalList)
        notifyDataSetChanged()
    }

    fun updateData(filteredData: List<UserVoucher>) {
        displayList.clear()
        displayList.addAll(filteredData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voucherImg: ImageView = itemView.findViewById(R.id.voucher_image)
        val voucherName: TextView = itemView.findViewById(R.id.nama_voucher)
        val voucherDeksripsi: TextView = itemView.findViewById(R.id.deskripsi_voucher)
        val tglVoucherBerlaku: TextView = itemView.findViewById(R.id.tgl_berlaku)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayList[position]
        holder.voucherName.text = item.voucher.nama_voucher
        holder.voucherDeksripsi.text = item.voucher.deskripsi
        holder.tglVoucherBerlaku.text = "Berlaku hingga ${formatTanggal(item.voucher.tgl_berakhir)}"

        holder.voucherImg.load(item.voucher.foto_voucher) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailVoucherActivity::class.java)
            intent.putExtra("data_voucher_saya", item)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = displayList.size

    private fun formatTanggal(tanggal: String): String {
        return try {
            val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
            outFormat.format(inFormat.parse(tanggal)!!)
        } catch (e: Exception) {
            tanggal
        }
    }
}
