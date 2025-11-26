package ordinary.rahmatbakery.pelanggan.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import java.text.NumberFormat
import java.util.Locale
import kotlin.text.format

class PesananCheckoutAdapter(private val listPesanan: List<Keranjang>) :
    RecyclerView.Adapter<PesananCheckoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtJumlah: TextView = view.findViewById(R.id.txt_qty_pesanan)
        val txtNama: TextView = view.findViewById(R.id.txt_nama_produk_pesanan)
        val txtHarga: TextView = view.findViewById(R.id.txt_harga_pesanan)
        val txtItemTambahan: TextView = view.findViewById(R.id.txt_item_tambahan)
        val image: ImageView = view.findViewById(R.id.imgProduk)
        val hargaAsliItem: TextView = itemView.findViewById(R.id.txt_harga_asli) // Harga yang dicoret

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPesanan[position]

        // Tentukan nama dan harga berdasarkan tipe item (produk atau paket)
        val nama = item.produk?.nama ?: item.paket?.nama ?: "Item tidak dikenal"
        val hargaAsli: Int
        var hargaSetelahDiskon: Int
        val diskon: Int
        val hargaSatuan = item.produk?.harga ?: item.paket?.harga ?: 0.0
        val hargaTotal = hargaSatuan.toDouble() * item.jumlah

        if (item.produk != null) {
            holder.txtNama.text = item.produk.nama
            hargaAsli = item.produk.harga
            diskon = item.produk.diskon ?: 0
            hargaSetelahDiskon = if (diskon > 0) {
                hargaAsli - (hargaAsli * diskon / 100)
            } else {
                hargaAsli
            }
        } else if (item.paket != null) {
            holder.txtNama.text = item.paket.nama
            hargaAsli = item.paket.harga
            diskon = 0 // Asumsi paket tidak ada diskon
            hargaSetelahDiskon = hargaAsli
        } else {
            // Fallback jika data aneh
            holder.txtNama.text = "Item tidak dikenal"
            hargaAsli = 0
            hargaSetelahDiskon = 0
            diskon = 0
        }



        // Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        holder.txtJumlah.text = "${item.jumlah}x"
        // Tampilkan harga setelah diskon
        holder.txtHarga.text = formatRupiah.format(hargaSetelahDiskon)

        // Logika untuk menampilkan harga asli yang dicoret
        if (diskon > 0) {
            holder.hargaAsliItem.visibility = View.VISIBLE
            holder.hargaAsliItem.text = formatRupiah.format(hargaAsli)
            holder.hargaAsliItem.paintFlags = holder.hargaAsliItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.hargaAsliItem.visibility = View.GONE
        }
        holder.image.load(item.produk?.gambar ?: item.paket?.foto) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }
    }

    override fun getItemCount() = listPesanan.size
}
