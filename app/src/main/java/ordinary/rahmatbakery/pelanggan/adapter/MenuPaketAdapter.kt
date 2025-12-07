package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.model.Paket
import java.text.NumberFormat
import java.util.Locale

class MenuPaketAdapter(
    private val listPaket: List<Paket>
) : RecyclerView.Adapter<MenuPaketAdapter.ViewHolder>() {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImg: ImageView = itemView.findViewById(R.id.productImg)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val discountedPrice: TextView = itemView.findViewById(R.id.discounted_price)
        val badgeDiskon: TextView = itemView.findViewById(R.id.badge_diskon)
        val infoHemat: TextView = itemView.findViewById(R.id.info_hemat)
        val btnPesan: TextView = itemView.findViewById(R.id.btn_pesan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_paket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPaket[position]
        holder.productName.text = item.nama
        holder.productPrice.text = formatRupiah.format(item.harga)

        var hargaDiskon = 0
        var persenDiskon = 0
        var nominalHemat = 0

        if (item.tipe_diskon != null && item.diskon != null) {
            if (item.tipe_diskon == "persen") {
                persenDiskon = item.diskon
                hargaDiskon = item.harga - (item.harga * item.diskon / 100)
                nominalHemat = item.harga - hargaDiskon
            } else {
                hargaDiskon = item.harga - item.diskon
                nominalHemat = item.diskon
                // Hitung persen untuk badge
                persenDiskon = ((item.diskon.toFloat() / item.harga.toFloat()) * 100).toInt()
            }
        }

        if (hargaDiskon > 0) {
            // Show discount elements
            holder.discountedPrice.visibility = View.VISIBLE
            holder.discountedPrice.text = formatRupiah.format(hargaDiskon)
            holder.productPrice.paintFlags = holder.productPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // Show discount badge
            holder.badgeDiskon.visibility = View.VISIBLE
            holder.badgeDiskon.text = "-${persenDiskon}%"

            // Show hemat info
            holder.infoHemat.visibility = View.VISIBLE
            holder.infoHemat.text = "Hemat ${formatRupiah.format(nominalHemat)}"
        } else {
            // Hide discount elements
            holder.discountedPrice.visibility = View.GONE
            holder.productPrice.paintFlags = holder.productPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.badgeDiskon.visibility = View.GONE
            holder.infoHemat.visibility = View.GONE
        }

        holder.productImg.load(item.foto) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

        holder.btnPesan.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailProdukActivity::class.java)
            intent.putExtra("TIPE", "paket")
            intent.putExtra("PAKET", item)
            intent.putExtra("FROM", "menu")
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = listPaket.size
}