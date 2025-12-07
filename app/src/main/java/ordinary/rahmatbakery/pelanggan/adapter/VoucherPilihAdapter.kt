package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Voucher

class VoucherAdapter(
    private val voucherList: List<Voucher>,
    private val onVoucherClick: (Voucher) -> Unit
) : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

    inner class VoucherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val voucherCard: CardView = itemView.findViewById(R.id.voucher_card)
        val voucherImage: ImageView = itemView.findViewById(R.id.voucher_image)
        val namaVoucher: TextView = itemView.findViewById(R.id.nama_voucher)
        val deskripsiVoucher: TextView = itemView.findViewById(R.id.deskripsi_voucher)
        val alasan: TextView = itemView.findViewById(R.id.alasan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voucher_pilih, parent, false)
        return VoucherViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
        val voucher = voucherList[position]

        holder.namaVoucher.text = voucher.nama_voucher
        var textDesk = ""
        if(voucher.jenis_voucher == "ongkir"){
            if(voucher.persentase_potongan != 0){
                textDesk = "Diskon ${voucher.persentase_potongan}% Untuk biaya Pengiriman"
            }else{
                textDesk = "Gratis Ongkir"
            }
        }else if(voucher.jenis_voucher == "potongan"){
            if(voucher.maksimal_potongan != 0){
                textDesk = "Diskon ${voucher.persentase_potongan}% (maksimal Rp. ${voucher.maksimal_potongan})"
            }else{
                textDesk = "Diskon ${voucher.persentase_potongan}%"
            }
        }
        holder.deskripsiVoucher.text = textDesk

        holder.voucherImage.load(voucher.foto_voucher) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

        // Tampilkan alasan kenapa voucher tidak bisa dipakai
        if (voucher.alasanTidakBisa.isNotEmpty()) {
            holder.alasan.visibility = View.VISIBLE
            holder.alasan.text = voucher.alasanTidakBisa
            holder.voucherCard.alpha = 0.5f // Buat lebih transparan
            holder.voucherCard.isEnabled = false
        } else {
            holder.alasan.visibility = View.GONE
            holder.voucherCard.alpha = 1.0f
            holder.voucherCard.isEnabled = true

            holder.voucherCard.setOnClickListener {
                onVoucherClick(voucher)
            }
        }
    }

    override fun getItemCount(): Int = voucherList.size
}