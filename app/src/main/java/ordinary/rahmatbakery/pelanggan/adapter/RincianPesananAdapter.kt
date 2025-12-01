package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.databinding.ItemPesananBinding // Asumsi: View Binding Class
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan
import java.text.NumberFormat
import java.util.Locale

class RincianPesananAdapter :
    ListAdapter<TampilanItemPesanan, RincianPesananAdapter.ItemViewHolder>(ItemPesananDiffCallback()) {

    private val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }

    inner class ItemViewHolder(private val binding: ItemPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TampilanItemPesanan) {
            with(binding) {
                if (!item.foto.isNullOrEmpty()) {
                    imgProduk.load(item.foto) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_background)
                        error(R.drawable.ic_launcher_foreground)
                    }
                } else {
                    imgProduk.setImageResource(R.drawable.ic_launcher_background)
                }

                txtNamaProdukPesanan.text = item.nama
                txtQtyPesanan.text = "${item.jumlah} x ${formatRupiah.format(item.hargaSatuan)}"
                txtHargaPesanan.text = formatRupiah.format(item.subtotal)
            }
        }
    }

    class ItemPesananDiffCallback : DiffUtil.ItemCallback<TampilanItemPesanan>() {
        override fun areItemsTheSame(oldItem: TampilanItemPesanan, newItem: TampilanItemPesanan): Boolean {
            return oldItem.nama == newItem.nama // Ganti dengan ID unik jika ada
        }

        override fun areContentsTheSame(oldItem: TampilanItemPesanan, newItem: TampilanItemPesanan): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemPesananBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}
