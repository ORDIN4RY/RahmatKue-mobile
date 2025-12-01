package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.graphics.Paint
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
import ordinary.rahmatbakery.pelanggan.model.Wadah
import java.text.NumberFormat
import java.util.Locale

class MenuCustomAdapter(
    private val listWadah: List<Wadah>)
    : RecyclerView.Adapter<MenuCustomAdapter.ViewHolder>()  {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImg: ImageView = itemView.findViewById(R.id.productImg)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val kapasitas: TextView = itemView.findViewById(R.id.kapasitas)
        val btnPilih: TextView = itemView.findViewById(R.id.btn_pilih)
        val miniText: TextView = itemView.findViewById(R.id.product_varian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuCustomAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_custom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuCustomAdapter.ViewHolder, position: Int) {
        val item = listWadah[position]
        holder.productName.text = item.nama
        holder.productPrice.text = formatRupiah.format(item.harga)
        holder.kapasitas.text = "kapasitas: ${item.kapasitas} item"

        holder.productImg.load(item.foto) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }

        holder.btnPilih.setOnClickListener {
//            val context = holder.itemView.context
//            val intent = Intent(context, DetailProdukActivity::class.java)
//            intent.putExtra("TIPE", "produk")
//            intent.putExtra("PRODUK", item)
//            intent.putExtra("FROM", "menu") // menandakan dari menu, bukan keranjang
//            context.startActivity(intent)
        }

        if (item.varian == "mini") {
            holder.miniText.visibility = View.VISIBLE
        } else {
            holder.miniText.visibility = View.GONE
        }

    }

    override fun getItemCount() = listWadah.size
}