package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.RincianPesananActivity
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.TampilanItemPesanan

class PesananAdapter(private val orderList: MutableList<Pesanan>) :
    RecyclerView.Adapter<PesananAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Deklarasi View dari layout item_pesanan_main.xml
        private val txtTanggal: TextView = itemView.findViewById(R.id.txt_tgl_pesan)
        private val txtStatus: TextView = itemView.findViewById(R.id.txt_status_pesanan)
        private val txtTotal: TextView = itemView.findViewById(R.id.txt_total_harga_semua)
        private val layoutProdukPertama: View = itemView.findViewById(R.id.rv_item_pesanan)
        private val txtNamaProduk: TextView = itemView.findViewById(R.id.txt_nama_produk_pesanan)
        private val txtItemTambahan: TextView = itemView.findViewById(R.id.txt_item_tambahan)
        private val rvProdukLain: RecyclerView = itemView.findViewById(R.id.rv_produk_lain)
        private val btnLihat: View = itemView.findViewById(R.id.btnLihatSemua)
        private val txtLihat: TextView = itemView.findViewById(R.id.txtLihatSemua)

        fun bind(pesanan: Pesanan) {
            // --- 1. Bind data transaksi utama ---
            txtTanggal.text = pesanan.createdAt.substringBefore("T") // Ambil tanggal saja
            txtStatus.text = pesanan.status
            txtTotal.text = "Rp ${String.format("%,d", pesanan.totalHarga)}"

            // --- 2. Gabungkan item produk dan paket menjadi satu list ---
            val semuaItem = (pesanan.items.map {
                TampilanItemPesanan(nama = it.produk.namaProduk, jumlah = it.jumlah, subtotal = it.subtotal)
            } + pesanan.paketItems.map {
                TampilanItemPesanan(nama = it.paket.namaPaket, jumlah = it.jumlah, subtotal = it.subtotal)
            }).sortedBy { it.nama } // Urutkan berdasarkan nama

            // --- 3. Atur visibilitas dan data berdasarkan jumlah item ---
            if (semuaItem.isEmpty()) {
                layoutProdukPertama.visibility = View.GONE
                btnLihat.visibility = View.GONE
                return
            }

            // Tampilkan item pertama
            layoutProdukPertama.visibility = View.VISIBLE
            txtNamaProduk.text = "${semuaItem.first().jumlah}x ${semuaItem.first().nama}"

            if (semuaItem.size > 1) {
                // Ada lebih dari 1 item, tampilkan tombol "Lihat Semua"
                btnLihat.visibility = View.VISIBLE
                txtItemTambahan.visibility = View.VISIBLE
                txtItemTambahan.text = "+ ${semuaItem.size - 1} item lainnya"

                // Siapkan adapter untuk item sisanya
                val itemLainAdapter = PesananItemAdapter(semuaItem.drop(1))
                rvProdukLain.layoutManager = LinearLayoutManager(itemView.context)
                rvProdukLain.adapter = itemLainAdapter
                rvProdukLain.visibility = View.GONE // Sembunyikan pada awalnya
                txtLihat.text = "Lihat semua"

                btnLihat.setOnClickListener {
                    val isVisible = rvProdukLain.visibility == View.VISIBLE
                    rvProdukLain.visibility = if (isVisible) View.GONE else View.VISIBLE
                    txtLihat.text = if (isVisible) "Lihat semua" else "Sembunyikan"
                }
            } else {
                // Hanya ada 1 item, sembunyikan elemen yang tidak perlu
                btnLihat.visibility = View.GONE
                txtItemTambahan.visibility = View.GONE
                rvProdukLain.visibility = View.GONE
            }

            // --- 4. Atur OnClickListener untuk seluruh item view ---
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RincianPesananActivity::class.java)
                intent.putExtra("TRANSACTION_DATA", pesanan) // Kirim objek Pesanan
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
        val pesanan = orderList[position]
        holder.bind(pesanan) // Cukup panggil bind
    }

    override fun getItemCount(): Int = orderList.size

    // Fungsi krusial untuk memperbarui data dari Fragment
    fun updateData(newList: List<Pesanan>) {
        orderList.clear()
        orderList.addAll(newList)
        notifyDataSetChanged() // Untuk performa lebih baik, bisa diganti dengan DiffUtil
    }
}
