package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.DetailVoucherActivity
import ordinary.rahmatbakery.pelanggan.model.Voucher

class TukarVoucherAdapter(
    private var displayList: MutableList<Voucher>
) : RecyclerView.Adapter<TukarVoucherAdapter.ViewHolder>() {

    private var originalList: List<Voucher> = listOf()

    fun getOriginalData(): List<Voucher> {
        return originalList
    }

    fun setOriginalData(data: List<Voucher>) {
        originalList = data
        displayList.clear()
        displayList.addAll(originalList)
        notifyDataSetChanged()
    }

    fun updateData(filteredData: List<Voucher>) {
        displayList.clear()
        displayList.addAll(filteredData)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voucherImg: ImageView = itemView.findViewById(R.id.img_tukar_voucher)
        val voucherName: TextView = itemView.findViewById(R.id.nama_tukar_voucher)
        val voucherDeksripsi: TextView = itemView.findViewById(R.id.deskripsi_tukar_voucher)
        val poinVoucher: TextView = itemView.findViewById(R.id.jumlah_poin)
        val btnDetailTukarVoucher: TextView = itemView.findViewById(R.id.btn_detail_tukar_voucher)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tukar_voucher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayList[position]
        holder.voucherName.text = item.nama_voucher
        holder.voucherDeksripsi.text = item.deskripsi
        holder.poinVoucher.text = "${item.poin_tukar ?: 0} Poin"

        holder.voucherImg.load(item.foto_voucher) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

        fun openDetail() {
            val context = holder.itemView.context
            val intent = Intent(context, DetailVoucherActivity::class.java)
            intent.putExtra("data_tukar_voucher", item)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener { openDetail() }
        holder.btnDetailTukarVoucher.setOnClickListener { openDetail() }
    }

    override fun getItemCount(): Int = displayList.size
}
