package ordinary.rahmatbakery.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.model.Produk


class ProdukAdapter(private var produkList: List<Produk>) :
    RecyclerView.Adapter<ProdukAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduk: ImageView = view.findViewById(R.id.ivProduk)
        val tvNamaProduk: TextView = view.findViewById(R.id.tvNamaProduk)
        val tvJumlahTerjual: TextView = view.findViewById(R.id.tvJumlahTerjual)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produk_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produk = produkList[position]
        holder.tvNamaProduk.text = produk.nama
        holder.tvJumlahTerjual.text = produk.jumlahTerjual.toString()

        // Load image dengan Coil
        holder.ivProduk.load(produk.gambarUrl) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.placeholder)
        }
    }

    override fun getItemCount() = produkList.size

    fun updateData(newList: List<Produk>) {
        produkList = newList
        notifyDataSetChanged()
    }
}