package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load // Pastikan import ini ada
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan
import java.text.NumberFormat
import java.util.Locale

class PesananItemAdapter(private val items: List<TampilanItemPesanan>) :
    RecyclerView.Adapter<PesananItemAdapter.ItemViewHolder>() {
        
    private val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.txt_nama_produk_pesanan)
        private val quantity: TextView = itemView.findViewById(R.id.txt_qty_pesanan)
        private val price: TextView = itemView.findViewById(R.id.txt_harga_pesanan)
        private val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)

        fun bind(item: TampilanItemPesanan) {

            // 1. Isi data teks
            name.text = item.nama
            quantity.text = "${item.jumlah}x "

            // 2. TAMPILKAN HARGA SATUAN, BUKAN SUBTOTAL
            // Ini akan memperbaiki masalah harga tidak sesuai.
            price.text = formatRupiah.format(item.subtotal)

            // 3. Logika memuat gambar dari URL yang benar
            if (!item.foto.isNullOrEmpty()) {
                // Langsung gunakan item.fotoUrl karena sudah merupakan URL lengkap
                imgProduk.load(item.foto) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background) // Ganti dengan placeholder Anda
                    error(R.drawable.ic_launcher_foreground)       // Ganti dengan gambar error Anda
                }
            } else {
                // Jika tidak ada URL, tampilkan gambar default
                imgProduk.setImageResource(R.drawable.ic_launcher_background)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        // Di sini harus bersih: cukup panggil bind.
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
