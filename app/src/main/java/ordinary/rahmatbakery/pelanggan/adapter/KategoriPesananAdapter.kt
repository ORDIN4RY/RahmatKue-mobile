package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Kategori

class KategoriPesananAdapter(
    private val onKategoriClick: (Kategori) -> Unit // Hanya butuh listener
) : RecyclerView.Adapter<KategoriPesananAdapter.ViewHolder>() {

    private val listKategori = mutableListOf<Kategori>()
    private var selectedPosition = 0 // Posisi item "Semua" adalah 0 secara default

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val kategText: TextView = itemView.findViewById(R.id.textKategori)

        init {
            // Listener diatur sekali di sini untuk efisiensi
            itemView.setOnClickListener {
                // Pastikan posisi yang diklik valid
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    // Panggil listener di Fragment
                    onKategoriClick(listKategori[adapterPosition])

                    // Update tampilan secara internal
                    updateSelection(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kategori = listKategori[position]
        holder.kategText.text = kategori.nama

        // Atur state visual berdasarkan posisi yang dipilih
        holder.itemView.isSelected = (position == selectedPosition)
    }

    override fun getItemCount() = listKategori.size

    // Fungsi untuk mengupdate data dari Fragment
    fun setData(newList: List<Kategori>) {
        listKategori.clear()
        listKategori.addAll(newList)
        notifyDataSetChanged()
    }

    // Fungsi internal untuk mengupdate tampilan visual
    private fun updateSelection(newPosition: Int) {
        if (newPosition == selectedPosition) {
            // Jika item yang sama diklik lagi, reset ke "Semua"
            if (selectedPosition != 0) {
                selectedPosition = 0
                onKategoriClick(listKategori[0]) // Beri tahu Fragment bahwa "Semua" dipilih
            }
        } else {
            // Jika item baru diklik
            val previousPosition = selectedPosition
            selectedPosition = newPosition

            // Render ulang item yang lama dan yang baru untuk efisiensi
            notifyItemChanged(previousPosition)
            notifyItemChanged(newPosition)
        }
    }
}
