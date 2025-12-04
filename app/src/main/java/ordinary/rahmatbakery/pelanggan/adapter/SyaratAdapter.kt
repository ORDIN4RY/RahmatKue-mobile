package ordinary.rahmatbakery.pelanggan.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ordinary.rahmatbakery.R

class SyaratAdapter(
    private val context: Context,
    private val data: List<Pair<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int = data.size
    override fun getItem(position: Int): Any = data[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_syarat, parent, false)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvSub = view.findViewById<TextView>(R.id.tvSub)

        val (judul, sub) = data[position]

        tvTitle.text = judul
        tvSub.text = sub

        return view
    }
}
