package ordinary.rahmatbakery.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.model.OrderPackage
import ordinary.rahmatbakery.admin.model.OrderProduct
import java.text.NumberFormat
import java.util.Locale

class OrderProductAdapter(
    private val products: List<OrderProduct>,
    private val packages: List<OrderPackage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PRODUCT = 0
        private const val TYPE_PACKAGE = 1
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductVariant: TextView = itemView.findViewById(R.id.tvProductVariant)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        val tvProductSubtotal: TextView = itemView.findViewById(R.id.tvProductSubtotal)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < products.size) TYPE_PRODUCT else TYPE_PACKAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ProductViewHolder

        if (position < products.size) {
            // Bind Product
            val item = products[position]
            val product = item.produk

            viewHolder.ivProductImage.load(product?.fotoProduk) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            viewHolder.tvProductName.text = product?.namaProduk ?: "Produk"
            viewHolder.tvProductVariant.text = "Varian: ${product?.varian ?: "Normal"}"
            viewHolder.tvProductPrice.text = formatRupiah(product?.harga ?: 0)
            viewHolder.tvProductQuantity.text = "x ${item.jumlah}"
            val totalHarga = item.jumlah * (product?.harga ?: 0)
            viewHolder.tvProductSubtotal.text = "Total: ${formatRupiah(totalHarga)}"

        } else {
            // Bind Package
            val packageIndex = position - products.size
            val item = packages[packageIndex]
            val paket = item.paket

            viewHolder.ivProductImage.load(paket?.fotoPaket) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            viewHolder.tvProductName.text = paket?.namaPaket ?: "Paket"
            viewHolder.tvProductVariant.text = "Paket"
            viewHolder.tvProductPrice.text = formatRupiah(paket?.hargaPaket ?: 0)
            viewHolder.tvProductQuantity.text = "x ${item.jumlah}"
            val totalHargaPaket = item.jumlah * (paket?.hargaPaket ?: 0)
            viewHolder.tvProductSubtotal.text = "Total: ${formatRupiah(totalHargaPaket)}"
        }
    }

    override fun getItemCount(): Int = products.size + packages.size

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }

}