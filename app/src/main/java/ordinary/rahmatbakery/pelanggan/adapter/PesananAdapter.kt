package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.R

class PesananAdapter(private val orderList: List<Pesanan>) :
    RecyclerView.Adapter<PesananAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNamaProduk = itemView.findViewById<TextView>(R.id.txt_nama_produk_pesanan)
        val txtItemTambahan = itemView.findViewById<TextView>(R.id.txt_item_tambahan)
        val txtQty = itemView.findViewById<TextView>(R.id.txt_qty_pesanan)
        val txtHarga = itemView.findViewById<TextView>(R.id.txt_harga_pesanan)

        val txtTanggal: TextView = itemView.findViewById(R.id.txt_tgl_pesan)
        val txtStatus: TextView = itemView.findViewById(R.id.txt_status_pesanan)
        val txtTotal: TextView = itemView.findViewById(R.id.txt_total_harga_semua)

        val layoutProdukPertama = itemView.findViewById<View>(R.id.rv_item_pesanan)
        val rvProduk: RecyclerView = itemView.findViewById(R.id.rv_produk_lain)
        val btnLihat: View = itemView.findViewById(R.id.btnLihatSemua)
        val txtLihat: TextView = itemView.findViewById(R.id.txtLihatSemua)


        // STATE EXPAND
        var expanded = false

        fun bind(order: Pesanan) {
            txtTanggal.text = order.date
            txtStatus.text = order.status
            txtTotal.text =
                "Total ${order.items.size} produk: Rp${String.format("%,.0f", order.totalAmount)}"

            val first = order.items[0]
            txtNamaProduk.text = first.name
            txtItemTambahan.text = first.description
            txtQty.text = "${first.quantity}x"
            txtHarga.text = "Rp ${String.format("%,.0f", first.price)}"
            // ───────────────────────────────
            // 1. Jika item cuma 1 → tampil full, hide tombol
            // ───────────────────────────────
            if (order.items.size == 1) {
                // Hide tampilan utama produk pertama
                layoutProdukPertama.visibility = View.GONE

                // Hide tombol lihat semua
                btnLihat.visibility = View.GONE

                // Tampilkan item 1 di rv_produk_lain
                rvProduk.visibility = View.VISIBLE

                rvProduk.layoutManager = LinearLayoutManager(itemView.context)
                rvProduk.adapter = PesananItemAdapter(order.items)

                return
            }

            // ───────────────────────────────
            // 2. Jika item lebih dari 1 → tampil mode ringkas dulu
            // ───────────────────────────────
            layoutProdukPertama.visibility = View.VISIBLE

// Tampilkan tombol lihat semua
            btnLihat.visibility = View.VISIBLE

// Sembunyikan list tambahan
            rvProduk.visibility = View.GONE

            btnLihat.setOnClickListener {
                if (rvProduk.visibility == View.GONE) {
                    rvProduk.visibility = View.VISIBLE
                    rvProduk.layoutManager = LinearLayoutManager(itemView.context)
                    rvProduk.adapter = PesananItemAdapter(order.items.drop(1)) // sisanya
                } else {
                    rvProduk.visibility = View.GONE
                }

            }
        }

        fun updateView(order: Pesanan) {
            if (expanded) {
                // Expand → tampilkan semua item
                rvProduk.adapter = PesananItemAdapter(order.items)
                rvProduk.visibility = View.VISIBLE

                txtLihat.text = "Sembunyikan"

            } else {
                // Collapse → tampilkan hanya item pertama
                rvProduk.adapter = PesananItemAdapter(order.items.take(1))
                rvProduk.visibility = View.VISIBLE

                txtLihat.text = "Lihat Semua"

            }
        }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pesanan_main, parent, false)
            return OrderViewHolder(view)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            holder.bind(orderList[position])
        }

        override fun getItemCount(): Int = orderList.size
    }

