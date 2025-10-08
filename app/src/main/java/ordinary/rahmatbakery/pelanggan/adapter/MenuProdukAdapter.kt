package ordinary.rahmatbakery.pelanggan.adapter

import ordinary.rahmatbakery.pelanggan.model.MenuProduk
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class MenuProdukAdapter(
    private val listProduk: List<MenuProduk>)
    : RecyclerView.Adapter<MenuProdukAdapter.ViewHolder>()  {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImg: ImageView = itemView.findViewById(R.id.productImg)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuProdukAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuProdukAdapter.ViewHolder, position: Int) {
        val item = listProduk[position]
        holder.productName.text = item.productName
        holder.productPrice.text = "Rp. " + item.productPrice.toString()

        holder.productImg.load(item.productImg) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }
    }

    override fun getItemCount() = listProduk.size
}