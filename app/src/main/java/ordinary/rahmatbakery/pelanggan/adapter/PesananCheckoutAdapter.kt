package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import java.text.NumberFormat
import java.util.Locale
import kotlin.text.format

class PesananCheckoutAdapter(private val listPesanan: List<Keranjang>) :
    RecyclerView.Adapter<PesananCheckoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtJumlah: TextView = view.findViewById(R.id.txt_item_jumlah)
        val txtNama: TextView = view.findViewById(R.id.txt_item_nama)
        val txtHarga: TextView = view.findViewById(R.id.txt_item_harga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan_checkout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPesanan[position]

        // Tentukan nama dan harga berdasarkan tipe item (produk atau paket)
        val nama = item.produk?.nama ?: item.paket?.nama ?: "Item tidak dikenal"
        val hargaSatuan = item.produk?.harga ?: item.paket?.harga ?: 0.0
        val hargaTotal = hargaSatuan.toDouble() * item.jumlah

        // Format Rupiah
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.txtJumlah.text = "${item.jumlah}x"
        holder.txtNama.text = nama
        holder.txtHarga.text = formatRupiah.format(hargaTotal)
    }

    override fun getItemCount() = listPesanan.size
}
