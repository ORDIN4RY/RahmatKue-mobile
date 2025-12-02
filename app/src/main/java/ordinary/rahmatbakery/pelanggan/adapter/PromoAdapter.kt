// ordinary/rahmatbakery/pelanggan/adapter/PromoAdapter.kt
package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.activity.DetailProdukActivity
import ordinary.rahmatbakery.pelanggan.activity.DetailPromoActivity
import ordinary.rahmatbakery.pelanggan.model.Promo

class PromoAdapter(private val promoList: List<Promo>) :
    RecyclerView.Adapter<PromoAdapter.PromoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_promo_square, parent, false)
        return PromoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromoViewHolder, position: Int) {
        val promo = promoList[position]
        holder.bind(promo)
    }

    override fun getItemCount(): Int = promoList.size

    class PromoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val promoImage: ImageView = itemView.findViewById(R.id.iv_promo_square)

        fun bind(promo: Promo) {

            // Menggunakan Coil untuk memuat gambar dari URL
            promoImage.load(promo.fotoSquare) {
                crossfade(true) // Animasi crossfade yang halus
                placeholder(R.drawable.placeholder) // Gambar yang ditampilkan saat loading
                error(R.drawable.error_image) // Gambar jika terjadi error saat memuat
                // Anda juga bisa menambahkan transformasi, misalnya rounded corners:
                // transformations(RoundedCornersTransformation(16f))
            }

            itemView.setOnClickListener {
                val context = itemView.context
                // Buat Intent untuk membuka DetailPromoActivity
                val intent = Intent(context, DetailPromoActivity::class.java)

                // Kirim seluruh objek 'promo' karena sudah Parcelable
                intent.putExtra("data_promo_terbaru", promo)

                // Mulai activity
                context.startActivity(intent)
            }
        }
    }
}
