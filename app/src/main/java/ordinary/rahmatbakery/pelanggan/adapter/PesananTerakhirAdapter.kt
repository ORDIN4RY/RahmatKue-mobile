package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.PesananTerakhir

class PesananTerakhirAdapter(
    private val listPesanan: List<PesananTerakhir>
) : RecyclerView.Adapter<PesananTerakhirAdapter.ViewHolder>() {

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
        holder.txtNamaProduk.text = item.namaProduk

        holder.imgProduk.load(item.gambarProduk) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }
    }

    override fun getItemCount() = listPesanan.size
}
