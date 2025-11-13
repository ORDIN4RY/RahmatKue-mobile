package ordinary.rahmatbakery.pelanggan.adapter

import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.TukarVoucher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class TukarVoucherAdapter(private val produkList: List<TukarVoucher>) :
    RecyclerView.Adapter<TukarVoucherAdapter.ProdukViewHolder>() {

    class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gambar = itemView.findViewById<ImageView>(R.id.imageView)
        val nama = itemView.findViewById<TextView>(R.id.nama_voucher)
        val poin = itemView.findViewById<TextView>(R.id.jumlah_poin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tukar_voucher, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val item = produkList[position]
        holder.nama.text = item.nama
        holder.poin.text = "${item.poin}"
        holder.gambar.load(item.gambar)
    }

    override fun getItemCount(): Int = produkList.size
}
