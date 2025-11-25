package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import ordinary.rahmatbakery.pelanggan.model.Produk
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import java.text.NumberFormat
import java.util.Locale

class MenuProdukAdapter(
    private val listProduk: List<Produk>)
    : RecyclerView.Adapter<MenuProdukAdapter.ViewHolder>()  {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImg: ImageView = itemView.findViewById(R.id.productImg)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val btnPesan: TextView = itemView.findViewById(R.id.btn_pesan)
        val miniText: TextView = itemView.findViewById(R.id.productSize)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuProdukAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuProdukAdapter.ViewHolder, position: Int) {
        val item = listProduk[position]
        holder.productName.text = item.nama
        holder.productPrice.text = formatRupiah.format(item.harga)

        holder.productImg.load(item.gambar) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }

        holder.btnPesan.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailProdukActivity::class.java)
            intent.putExtra("TIPE", "produk")
            intent.putExtra("PRODUK", item)
            intent.putExtra("FROM", "menu") // menandakan dari menu, bukan keranjang
            context.startActivity(intent)
        }

        if (item.varian == "mini") {
            holder.miniText.visibility = View.VISIBLE
        } else {
            holder.miniText.visibility = View.GONE
        }

    }

    override fun getItemCount() = listProduk.size
}