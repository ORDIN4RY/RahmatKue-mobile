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
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.model.Produk

class MenuTerbaruAdapter(private val produkList: List<Produk>) :
    RecyclerView.Adapter<MenuTerbaruAdapter.ProdukViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        // Kita tetap bisa pakai layout yang sama
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promo_square, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        val produk = produkList[position]
        holder.bind(produk)
    }

    override fun getItemCount(): Int = produkList.size

    class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Pastikan ID ini cocok dengan layout item_promo_square.xml
        private val produkImage: ImageView = itemView.findViewById(R.id.iv_promo_square)

        fun bind(produk: Produk) {

            produkImage.load(produk.gambar) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.error_image)
            }

            // Aksi klik untuk membuka detail produk (bisa diaktifkan nanti)
            itemView.setOnClickListener {
                 val context = itemView.context
                 val intent = Intent(context, DetailProdukActivity::class.java)
                 intent.putExtra("data_menu_terbaru", produk)
                 context.startActivity(intent)
            }
        }
    }
}
