package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Alamat

class AlamatAdapter (

    private val listAlamat: List<Alamat>,
    private val clickListener: AlamatClickListener
)
    : RecyclerView.Adapter<AlamatAdapter.ViewHolder>()  {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nama: TextView = itemView.findViewById(R.id.nama)
            val noHp : TextView = itemView.findViewById(R.id.nomor_telepon)
            val alamat : TextView = itemView.findViewById(R.id.alamat_lengkap)
            val alamatUtama : TextView = itemView.findViewById(R.id.alamat_utama)
            val ubahAlamat : TextView = itemView.findViewById(R.id.ubah_alamat)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlamatAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_alamat, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlamatAdapter.ViewHolder, position: Int) {
            val item = listAlamat[position]
            holder.nama.text = item.nama
            holder.noHp.text = item.noHp
            holder.alamat.text = item.alamat
            if (item.isUtama) {
                holder.alamatUtama.visibility = View.VISIBLE
            } else {
                holder.alamatUtama.visibility = View.INVISIBLE
            }

            holder.ubahAlamat.setOnClickListener {
                clickListener.onAlamatClicked(item)
            }
        }

        override fun getItemCount() = listAlamat.size

}

interface AlamatClickListener {
    fun onAlamatClicked(alamat: Alamat)
}