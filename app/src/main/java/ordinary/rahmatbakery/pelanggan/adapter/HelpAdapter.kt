package ordinary.rahmatbakery.pelanggan.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.HelpItem

class HelpAdapter(private val helpList: List<HelpItem>) :
    RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    class HelpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestion)
        val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_help, parent, false)
        return HelpViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        val item = helpList[position]
        holder.tvQuestion.text = item.question
        holder.tvAnswer.text = item.answer

        // Toggle tampil/sembunyi jawaban saat diklik
        holder.itemView.setOnClickListener {
            if (holder.tvAnswer.visibility == View.GONE) {
                holder.tvAnswer.visibility = View.VISIBLE
            } else {
                holder.tvAnswer.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = helpList.size
}