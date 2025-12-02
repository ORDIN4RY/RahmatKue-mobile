package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.graphics.Paint
import ordinary.rahmatbakery.pelanggan.model.Produk
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.UserVoucher
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.activity.DetailVoucherActivity
import java.text.NumberFormat
import java.util.Locale

class VoucherSayaAdapter(
    private val listVoucherSaya: List<UserVoucher>
) : RecyclerView.Adapter<VoucherSayaAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voucherImg: ImageView = itemView.findViewById(R.id.voucher_image)
        val voucherName: TextView = itemView.findViewById(R.id.nama_voucher)
        val voucherDeksripsi: TextView = itemView.findViewById(R.id.deskripsi_voucher)
        val tglVoucherBerlaku: TextView = itemView.findViewById(R.id.tgl_berlaku)
        val voucherCard: RelativeLayout = itemView.findViewById(R.id.voucher_card)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VoucherSayaAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherSayaAdapter.ViewHolder, position: Int) {
        val item = listVoucherSaya[position]
        holder.voucherName.text = item.voucher.nama_voucher
        holder.voucherDeksripsi.text = item.voucher.deskripsi
        holder.tglVoucherBerlaku.text = item.voucher.tgl_berakhir



        holder.voucherImg.load(item.voucher.foto_voucher) {
            crossfade(true) // animasi lembut saat gambar muncul
            placeholder(R.drawable.placeholder) // opsional: gambar sementara
            error(R.drawable.error_image)       // opsional: jika gagal load
        }
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailVoucherActivity::class.java)
            intent.putExtra("data_voucher_saya", item)  // kirim parcelable
            context.startActivity(intent)
        }

        // atau kalau klik tombol detail:
        holder.voucherCard.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailVoucherActivity::class.java)
            intent.putExtra("data_voucher_saya", item)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = listVoucherSaya.size
}