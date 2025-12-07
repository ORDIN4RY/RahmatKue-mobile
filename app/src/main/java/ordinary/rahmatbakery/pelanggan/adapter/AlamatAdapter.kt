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
) : RecyclerView.Adapter<AlamatAdapter.ViewHolder>()  {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.nama)
        val noHp : TextView = itemView.findViewById(R.id.nomor_telepon)
        val alamat : TextView = itemView.findViewById(R.id.alamat_lengkap)
        val wilayah : TextView = itemView.findViewById(R.id.wilayah)
        val alamatUtama : TextView = itemView.findViewById(R.id.alamat_utama)
        val ubahAlamat : TextView = itemView.findViewById(R.id.ubah_alamat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alamat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listAlamat[position]
        holder.nama.text = item.nama
        holder.noHp.text = item.noHp

        // Gabungkan alamat dengan detail jika ada
        val fullAddress = if (!item.detail.isNullOrEmpty()) {
            "${item.alamat}, ${item.detail}"
        } else {
            item.alamat
        }
        holder.alamat.text = fullAddress

        // Format wilayah
        val wilayahParts = mutableListOf<String>()
        if (!item.kecamatan.isNullOrEmpty()) wilayahParts.add(item.kecamatan)
        if (!item.kabupaten.isNullOrEmpty()) wilayahParts.add(item.kabupaten)
        if (!item.provinsi.isNullOrEmpty()) wilayahParts.add(item.provinsi)

        holder.wilayah.text = if (wilayahParts.isNotEmpty()) {
            wilayahParts.joinToString(", ")
        } else {
            "Lokasi belum ditentukan"
        }

        // Show/hide badge utama
        if (item.isUtama) {
            holder.alamatUtama.visibility = View.VISIBLE
        } else {
            holder.alamatUtama.visibility = View.GONE
        }

        holder.ubahAlamat.setOnClickListener {
            clickListener.onEditClicked(item)
        }

        holder.itemView.setOnClickListener {
            clickListener.onAlamatClicked(item)
        }
    }

    override fun getItemCount() = listAlamat.size
}

interface AlamatClickListener {
    fun onAlamatClicked(alamat: Alamat)
    fun onEditClicked(alamat: Alamat)
}