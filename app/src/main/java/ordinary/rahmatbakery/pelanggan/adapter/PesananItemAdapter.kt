package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.pelanggan.model.PesananItem
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.RincianPesananActivity
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity

class PesananItemAdapter(private val items: List<PesananItem>) :
    RecyclerView.Adapter<PesananItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txt_nama_produk_pesanan)
        val description: TextView = itemView.findViewById(R.id.txt_item_tambahan)
        val quantity: TextView = itemView.findViewById(R.id.txt_qty_pesanan)
        val Price: TextView = itemView.findViewById(R.id.txt_harga_pesanan)

        fun bind(item: PesananItem) {
            name.text = item.name
            description.text = item.description
                quantity.text = "${item.quantity}"
            Price.text = "Rp ${String.format("%,.0f", item.price)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])


    }

    override fun getItemCount(): Int = items.size
}