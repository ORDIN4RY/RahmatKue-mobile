package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Alamat
import ordinary.rahmatbakery.pelanggan.model.Kategori

class KategoriAdapter(
    private val listProduk: List<Kategori>,
    private var selectedKategoriId: String, // ID kategori yang sedang dipilih
    private val onKategoriClicked: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kategText: TextView = itemView.findViewById(R.id.textKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriAdapter.ViewHolder, position: Int) {
        val item = listProduk[position]
        holder.kategText.text = item.nama

        if (item.id == selectedKategoriId) {
            // Jika ID item ini sama dengan ID yang dipilih, set state menjadi 'selected'
            holder.itemView.isSelected = true
        } else {
            // Jika tidak, pastikan state-nya tidak 'selected'
            holder.itemView.isSelected = false
        }

        holder.itemView.setOnClickListener {
            onKategoriClicked(item)
        }
    }

    override fun getItemCount() = listProduk.size

    fun updateSelected(newSelectedId: String) {
        selectedKategoriId = newSelectedId
        notifyDataSetChanged() // Render ulang RecyclerView
    }
}
