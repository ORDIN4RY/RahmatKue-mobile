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
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class LastOrderAdapter(
    private val listPesanan: List<Keranjang>
) : RecyclerView.Adapter<LastOrderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)
        val txtNamaProduk: TextView = itemView.findViewById(R.id.txtNamaProduk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan_terakhir, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPesanan[position]
        val nama = if (item.tipe == "produk") item.produk?.nama else item.paket?.nama
        val gambar = if (item.tipe == "produk") item.produk?.gambar else item.paket?.foto

        holder.txtNamaProduk.text = nama ?: "Tanpa Nama"
        holder.imgProduk.load(gambar) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailProdukActivity::class.java)
            intent.putExtra("TIPE", item.tipe)
            intent.putExtra("FROM", "menu")
            if (item.tipe == "produk") intent.putExtra("PRODUK", item.produk)
            if (item.tipe == "paket") intent.putExtra("PAKET", item.paket)
            context.startActivity(intent)
        }

    }

    override fun getItemCount() = listPesanan.size
}
