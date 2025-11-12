package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Detail

class DetailPaketAdapter(private val listDetail: List<Detail>) :
    RecyclerView.Adapter<DetailPaketAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduk: ImageView = view.findViewById(R.id.img_produk_detail)
        val txtNama: TextView = view.findViewById(R.id.txt_nama_produk_detail)
        val txtJumlah: TextView = view.findViewById(R.id.txt_jumlah_produk_detail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_paket, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val detail = listDetail[position]
        holder.txtNama.text = detail.produk.nama
        holder.txtJumlah.text = "x${detail.jumlah}"
        holder.imgProduk.load(detail.produk.gambar) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }
    }

    override fun getItemCount() = listDetail.size
}