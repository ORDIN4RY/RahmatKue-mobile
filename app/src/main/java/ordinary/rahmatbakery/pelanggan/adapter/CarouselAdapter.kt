package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Carousel



class CarouselAdapter(private var itemsCarousel: List<Carousel>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {


    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_slide)
         }

    fun updateData(newItems: List<Carousel>) {
        itemsCarousel = newItems
        notifyDataSetChanged() // Memberi tahu adapter bahwa seluruh data telah berubah
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        // Menggunakan layout item_carousel_slide.xml yang sudah dibuat
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    // Menghubungkan data dengan view di ViewHolder
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {

        val item = itemsCarousel[position]

        // Menggunakan Coil untuk memuat gambar dari URL
        holder.imageView.load(item.image_url) {
            crossfade(true)
            placeholder(R.drawable.placeholder)
            error(R.drawable.error_image)
        }

    }

    // Mengembalikan jumlah total slide
    override fun getItemCount() = itemsCarousel.size
}
