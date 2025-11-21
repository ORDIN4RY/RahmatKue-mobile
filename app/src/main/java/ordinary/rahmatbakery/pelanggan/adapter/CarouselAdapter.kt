package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Carousel



class CarouselAdapter(private val items: List<Int>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {


    inner class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_slide)
         }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        // Menggunakan layout item_carousel_slide.xml yang sudah dibuat
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        return CarouselViewHolder(view)
    }

    // Menghubungkan data dengan view di ViewHolder
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {

        // Mengatur warna latar belakang sesuai data
        holder.imageView.setImageResource(items[position])


    }

    // Mengembalikan jumlah total slide
    override fun getItemCount(): Int = items.size
}
