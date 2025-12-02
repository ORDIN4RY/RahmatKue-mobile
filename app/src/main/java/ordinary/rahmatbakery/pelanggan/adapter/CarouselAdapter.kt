package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Produk
import ordinary.rahmatbakery.pelanggan.model.ProdukGambar
import ordinary.rahmatbakery.pelanggan.model.Promo


class CarouselAdapter(private var items: List<Promo>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_slide)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = items[position]

        // Load gambar dengan Coil/Glide

            holder.imageView.load(item.fotoBanner) {
                crossfade(true)
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)

        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Promo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
