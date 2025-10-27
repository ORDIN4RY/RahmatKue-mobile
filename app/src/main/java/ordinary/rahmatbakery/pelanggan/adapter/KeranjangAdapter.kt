package ordinary.rahmatbakery.pelanggan.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import java.text.NumberFormat
import java.util.Locale


class KeranjangAdapter(
    private val listKeranjang: MutableList<Keranjang>
) : RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    interface OnItemInteractionListener {
        /** Dipanggil saat ada perubahan (jumlah atau checkbox) untuk memperbarui UI lain,
         * seperti total harga. */
        fun onDataChanged()
    }

    private var listener: OnItemInteractionListener? = null

    fun setOnItemInteractionListener(listener: OnItemInteractionListener) {
        this.listener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduk: ImageView = itemView.findViewById(R.id.img_product)
        val txtNamaProduk: TextView = itemView.findViewById(R.id.nama_produk)
        val txtHarga: TextView = itemView.findViewById(R.id.txt_harga)
        val jumlah: EditText = itemView.findViewById(R.id.input_count)
        val checkbox: CheckBox = itemView.findViewById(R.id.cb_pilih)
        var textWatcher: TextWatcher? = null

        val iconMinus: ImageView = itemView.findViewById(R.id.icon_minus)
        val iconPlus: ImageView = itemView.findViewById(R.id.icon_plus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keranjang, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listKeranjang[position]
        holder.txtNamaProduk.text = item.produk.productName

        holder.imgProduk.load(item.produk.productImg) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }

        holder.txtHarga.text = "Rp. ${(item.produk.productPrice * item.jumlah)}"

        holder.jumlah.setText(item.jumlah.toString())
        holder.checkbox.isChecked = item.selected
        updateItemPrice(holder, item)

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            // Dapatkan posisi item yang benar-benar diklik
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                // Perbarui model data secara langsung
                listKeranjang[currentPosition].selected = isChecked
                // Beri tahu Activity bahwa ada data yang berubah
                listener?.onDataChanged()
            }
        }

        holder.textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return

                val quantityString = s?.toString()
                if (!quantityString.isNullOrEmpty()) {
                    try {
                        val newQuantity = quantityString.toInt()
                        val currentItem = listKeranjang[currentPosition]

                        // Ubah jumlah di model data
                        if (newQuantity >= 15) {
                            currentItem.jumlah = newQuantity
                        } else {
                            currentItem.jumlah = 15
                        }
                        // Perbarui tampilan harga untuk item ini
                        updateItemPrice(holder, currentItem)
                        // Beri tahu Activity bahwa ada data yang berubah
                        listener?.onDataChanged()

                    } catch (e: NumberFormatException) {
                        // Abaikan jika input bukan angka
                    }
                }
            }
        }
        holder.jumlah.addTextChangedListener(holder.textWatcher)

        holder.iconPlus.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            val currentItem = listKeranjang[currentPosition]
            if (currentItem.jumlah >= 15)
                currentItem.jumlah++
            holder.jumlah.setText(currentItem.jumlah.toString())
            updateItemPrice(holder, currentItem)
            listener?.onDataChanged()

        }
        holder.iconMinus.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition

            val currentItem = listKeranjang[currentPosition]
            if (currentItem.jumlah > 15)
                currentItem.jumlah--
            else
                Toast.makeText(holder.itemView.context, "Jumlah minimal adalah 15", Toast.LENGTH_SHORT).show()
            holder.jumlah.setText(currentItem.jumlah.toString())
            updateItemPrice(holder, currentItem)
            listener?.onDataChanged()

        }

    }

    private fun updateItemPrice(holder: ViewHolder, item: Keranjang) {
        val totalPrice = item.produk.productPrice * item.jumlah
        holder.txtHarga.text = formatRupiah.format(totalPrice)
    }

    override fun getItemCount() = listKeranjang.size

    // --- Fungsi Publik untuk dikontrol oleh Activity ---
    fun getSelectedItems(): List<Keranjang> {
        return listKeranjang.filter { it.selected }
    }

    fun removeSelectedItems() {
        val iterator = listKeranjang.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().selected) {
                iterator.remove()
            }
        }
        notifyDataSetChanged()
    }

}