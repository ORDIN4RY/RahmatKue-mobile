package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan

// Adapter ini sekarang bekerja dengan 'TampilanItemPesanan', membuatnya fleksibel.
class PesananItemAdapter(private val items: List<TampilanItemPesanan>) :
    RecyclerView.Adapter<PesananItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.txt_nama_produk_pesanan)
        private val quantity: TextView = itemView.findViewById(R.id.txt_qty_pesanan)
        private val price: TextView = itemView.findViewById(R.id.txt_harga_pesanan)

        fun bind(item: TampilanItemPesanan) {
            name.text = item.nama
            quantity.text = "${item.jumlah}x"
            // Menampilkan subtotal adalah yang paling relevan di sini
            price.text = "Rp ${String.format("%,d", item.subtotal)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // Pastikan Anda menggunakan layout yang benar untuk item individu
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
