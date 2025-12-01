package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load // Pastikan import ini ada
import io.ktor.websocket.Frame
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.RincianPesananActivity
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan
import java.text.NumberFormat
import java.util.Locale

class PesananAdapter(private val orderList: MutableList<Pesanan>) :
    RecyclerView.Adapter<PesananAdapter.OrderViewHolder>() {


    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Deklarasi View
        private val txtTanggal: TextView = itemView.findViewById(R.id.txt_tgl_pesan)
        private val txtStatus: TextView = itemView.findViewById(R.id.txt_status_pesanan)
        private val txtTotal: TextView = itemView.findViewById(R.id.txt_total_harga_semua)
        private val layoutProdukPertama: View = itemView.findViewById(R.id.item_pesanan)
        private val txtNamaProduk: TextView = itemView.findViewById(R.id.txt_nama_produk_pesanan)
         private val txtJumlah: TextView = itemView.findViewById(R.id.txt_qty_pesanan)
        private val txtTotalItem : TextView =itemView.findViewById(R.id.txt_harga_pesanan)
        private val rvProdukLain: RecyclerView = itemView.findViewById(R.id.rv_produk_lain)
        private val btnLihat: View = itemView.findViewById(R.id.btnLihatSemua)
        private val txtLihat: TextView = itemView.findViewById(R.id.txtLihatSemua)
        private val imageView: ImageView = itemView.findViewById(R.id.imgProduk)

        // Formatter untuk Rupiah, lebih efisien diletakkan di sini
        private val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }

        // Semua logika tampilan terpusat di sini
        fun bind(pesanan: Pesanan) {
            Log.d("PesananDebug", "=============================================")
            Log.d("PesananDebug", "Menganalisis Transaksi ID: ${pesanan.idTransaksi}")
            Log.d("PesananDebug", "Jumlah Item Produk di DB: ${pesanan.items.size}")
            Log.d("PesananDebug", "Jumlah Item Paket di DB: ${pesanan.paketItems.size}")
            // --- 1. Bind data transaksi utama ---

            txtTanggal.text = pesanan.createdAt.substringBefore("T")
            txtStatus.text = pesanan.status


            // --- 2. Gabungkan item, pastikan semua properti TampilanItemPesanan diisi ---
            val semuaItem = (pesanan.items.map {
                TampilanItemPesanan(
                    nama = it.produk.namaProduk,
                    jumlah = it.jumlah,
                    subtotal = it.subtotal,
                    foto = it.produk.fotoProduk,
                    hargaSatuan = it.produk.harga
                )
            } + pesanan.paketItems.map {
                TampilanItemPesanan(
                    nama = it.paket.namaPaket,
                    jumlah = it.jumlah,
                    subtotal = it.subtotal,
                    foto= it.paket.fotoPaket,
                    hargaSatuan = it.paket.hargaPaket
                )
            })

            // --- 3. Atur tampilan berdasarkan jumlah item ---
            if (semuaItem.isEmpty()) {
                layoutProdukPertama.visibility = View.GONE
                btnLihat.visibility = View.GONE
                return
            }

            val itemPertama = semuaItem[0]
            val totalHarga = itemPertama.jumlah * itemPertama.hargaSatuan
            layoutProdukPertama.visibility = View.VISIBLE
            txtNamaProduk.text = itemPertama.nama
            txtJumlah.text = "${itemPertama.jumlah}x"
            txtTotalItem.text = formatRupiah.format(totalHarga)

            // Hitung ulang total harga yang benar dari subtotal item
            val totalHargaYangBenar = pesanan.items.sumOf { it.subtotal } + pesanan.paketItems.sumOf { it.subtotal }


            txtTotal.text = formatRupiah.format(totalHargaYangBenar )

            // Muat gambar sampul dari item pertama
            if (!itemPertama.foto.isNullOrEmpty()) {
                imageView.load(itemPertama.foto) {
                    crossfade(true)
                    placeholder(R.drawable.ic_launcher_background)
                    error(R.drawable.ic_launcher_foreground)
                }
            } else {
                imageView.setImageResource(R.drawable.ic_launcher_background)
            }

            // Logika untuk item lainnya
            if (semuaItem.size > 1) {
                btnLihat.visibility = View.VISIBLE

                val itemLain = semuaItem.drop(1)
                val itemLainAdapter = PesananItemAdapter(itemLain  )
                rvProdukLain.layoutManager = LinearLayoutManager(itemView.context)
                rvProdukLain.adapter = itemLainAdapter
                rvProdukLain.visibility = View.GONE
                txtLihat.text = "Lihat semua"

                btnLihat.setOnClickListener {
                    val isVisible = rvProdukLain.visibility == View.VISIBLE
                    rvProdukLain.visibility = if (isVisible) View.GONE else View.VISIBLE
                    txtLihat.text = if (isVisible) "Lihat semua" else "Sembunyikan"
                }
            } else {
                btnLihat.visibility = View.GONE
                rvProdukLain.visibility = View.GONE
            }

            // --- 4. OnClickListener ---
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RincianPesananActivity::class.java)
                intent.putExtra(RincianPesananActivity.EXTRA_TRANSAKSI, pesanan)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pesanan_main, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        // onBindViewHolder HARUS BERSIH. Cukup panggil bind.
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    fun updateData(newList: List<Pesanan>) {
        orderList.clear()
        orderList.addAll(newList)
        Log.d("PesananAdapter", "Data updated. New list size: ${orderList.size}")
        notifyDataSetChanged()
    }
}
