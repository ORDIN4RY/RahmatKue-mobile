package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Keranjang

class KeranjangAdapter (
    private val listKeranjang: List<Keranjang>
    ) : RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgProduk: ImageView = itemView.findViewById(R.id.img_product)
            val txtNamaProduk: TextView = itemView.findViewById(R.id.nama_produk)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_keranjang, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = listKeranjang[position]
            holder.txtNamaProduk.text = item.produk.productName

            holder.imgProduk.load(item.produk.productImg) {
                crossfade(true) // animasi lembut saat gambar muncul
                placeholder(R.drawable.placeholder) // opsional: gambar sementara
                error(R.drawable.error_image)       // opsional: jika gagal load
            }
        }

        override fun getItemCount() = listKeranjang.size
}