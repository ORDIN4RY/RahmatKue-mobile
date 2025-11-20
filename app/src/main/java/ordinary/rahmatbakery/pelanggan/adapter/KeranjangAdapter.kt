package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
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
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import java.text.NumberFormat
import java.util.Locale


class KeranjangAdapter(
    private val listKeranjang: MutableList<Keranjang>,
    private val onJumlahChanged: (Keranjang, Int) -> Unit
) : RecyclerView.Adapter<KeranjangAdapter.ViewHolder>() {

    var localeID: Locale = Locale("in", "ID")
    var formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID).apply {
        maximumFractionDigits = 0
    }

    interface OnItemInteractionListener {
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

        val nama = if (item.tipe == "produk") item.produk?.nama else item.paket?.nama
        val gambar = if (item.tipe == "produk") item.produk?.gambar else item.paket?.foto
        val harga = if (item.tipe == "produk") item.produk?.harga else item.paket?.harga

        holder.txtNamaProduk.text = nama ?: "Tanpa Nama"
        holder.imgProduk.load(gambar) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }
        holder.txtHarga.text = formatRupiah.format((harga ?: 0) * item.jumlah)

        holder.jumlah.setText(item.jumlah.toString())
        holder.checkbox.isChecked = item.terpilih

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                listKeranjang[currentPos].terpilih = isChecked
                listener?.onDataChanged()
            }
        }

        holder.iconPlus.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            val current = listKeranjang[currentPos]
            current.jumlah++
            holder.jumlah.setText(current.jumlah.toString())
            listener?.onDataChanged()
            onJumlahChanged(current, current.jumlah)
            holder.txtHarga.text = formatRupiah.format((harga ?: 0) * item.jumlah)
        }

        holder.iconMinus.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            val current = listKeranjang[currentPos]

            if (current.tipe == "produk") {
                if (current.jumlah <= 15) {
                    Toast.makeText(holder.itemView.context, "Minimal Produk satuan adalah 15 biji", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }else{
                if (current.jumlah <= 1){
                    Toast.makeText(holder.itemView.context, "Minimal paket adalah 1", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            current.jumlah--
            holder.jumlah.setText(current.jumlah.toString())
            listener?.onDataChanged()
            onJumlahChanged(current, current.jumlah)
            holder.txtHarga.text = formatRupiah.format((harga ?: 0) * item.jumlah)
        }

        // Hapus listener lama untuk menghindari penumpukan
        holder.jumlah.onFocusChangeListener = null
        holder.jumlah.setOnEditorActionListener(null)

        // --- 1. Listener untuk menghandle saat FOKUS HILANG ---
        holder.jumlah.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            // Kita hanya peduli saat fokusnya HILANG (hasFocus == false)
            if (!hasFocus) {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos == RecyclerView.NO_POSITION) return@OnFocusChangeListener

                val newJumlahStr = holder.jumlah.text.toString()
                if (newJumlahStr.isNotEmpty()) {
                    try {
                        val newJumlah = newJumlahStr.toInt()
                        val currentItem = listKeranjang[currentPos]

                        // Validasi minimal kuantitas
                        var validatedJumlah = newJumlah
                        var validationMessage: String? = null

                        if (currentItem.tipe == "produk" && newJumlah < 15) {
                            validatedJumlah = 15
                            validationMessage = "Minimal produk satuan adalah 15 biji"
                        } else if (currentItem.tipe == "paket" && newJumlah < 1) {
                            validatedJumlah = 1
                            validationMessage = "Minimal paket adalah 1"
                        }

                        // Jika ada pesan validasi, tampilkan
                        validationMessage?.let {
                            Toast.makeText(holder.itemView.context, it, Toast.LENGTH_SHORT).show()
                            holder.jumlah.setText(currentItem.jumlah.toString()) // Kembalikan ke jumlah semula
                        }

                        // Jika jumlahnya berubah dari validasi atau dari input pengguna
                        if (validatedJumlah != currentItem.jumlah) {
                            currentItem.jumlah = validatedJumlah
                            holder.jumlah.setText(validatedJumlah.toString()) // Update tampilan EditText

                            // Panggil listener untuk update UI dan Database
                            listener?.onDataChanged()
                            onJumlahChanged(currentItem, validatedJumlah)
                            holder.txtHarga.text = formatRupiah.format((harga ?: 0) * validatedJumlah)
                        }
                    } catch (e: NumberFormatException) {
                        // Jika input tidak valid, kembalikan ke jumlah semula
                        holder.jumlah.setText(item.jumlah.toString())
                    }
                } else {
                    // Jika input kosong, kembalikan ke jumlah semula
                    holder.jumlah.setText(item.jumlah.toString())
                }
            }
        }

        // --- 2. Listener untuk menghandle tombol 'Enter' (Done/Selesai) di Keyboard ---
        holder.jumlah.setOnEditorActionListener { v, actionId, _ ->
            // Cek apakah aksinya adalah 'Done' (Selesai)
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // Hapus fokus dari EditText
                v.clearFocus()

                // Sembunyikan keyboard
                val imm = holder.itemView.context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                // Untuk action lain, biarkan sistem yang menghandle
                false
            }
        }


        holder.txtNamaProduk.setOnClickListener {
            goToDetail(item, holder)
        }

        holder.imgProduk.setOnClickListener {
            goToDetail(item, holder)

        }
    }

    private fun goToDetail(item: Keranjang, holder: ViewHolder) {
        val context = holder.itemView.context
        val intent = Intent(context, DetailProdukActivity::class.java)
        intent.putExtra("TIPE", item.tipe)
        intent.putExtra("FROM", "keranjang")
        if (item.tipe == "produk") intent.putExtra("PRODUK", item.produk)
        if (item.tipe == "paket") intent.putExtra("PAKET", item.paket)
        context.startActivity(intent)
    }


    override fun getItemCount() = listKeranjang.size

    fun getSelectedItems(): List<Keranjang> = listKeranjang.filter { it.terpilih }

    fun removeSelectedItems() {
        listKeranjang.removeAll { it.terpilih }
        notifyDataSetChanged()
    }

}