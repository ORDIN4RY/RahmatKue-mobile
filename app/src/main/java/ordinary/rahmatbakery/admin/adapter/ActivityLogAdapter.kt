package ordinary.rahmatbakery.admin.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.model.ActivityLog
import ordinary.rahmatbakery.admin.model.ActivityType
import ordinary.rahmatbakery.admin.model.PesananAdmin
import ordinary.rahmatbakery.admin.model.Produk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ActivityLogAdapter(private var activityList: List<ActivityLog>) :
    RecyclerView.Adapter<ActivityLogAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActivityIcon: TextView = view.findViewById(R.id.tvActivityIcon)
        val tvActivityTitle: TextView = view.findViewById(R.id.tvActivityTitle)
        val tvActivityDescription: TextView = view.findViewById(R.id.tvActivityDescription)
        val tvActivityTime: TextView = view.findViewById(R.id.tvActivityTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activityList[position]

        // Set icon dan warna berdasarkan tipe
        val (icon, backgroundColor) = when (activity.type) {
            ActivityType.ORDER_CREATED -> "📦" to "#E3F2FD"
            ActivityType.ORDER_CONFIRMED -> "✅" to "#E8F5E9"
            ActivityType.ORDER_COMPLETED -> "🎉" to "#F3E5F5"
            ActivityType.ORDER_CANCELLED -> "❌" to "#FFEBEE"
            ActivityType.PAYMENT_RECEIVED -> "💰" to "#FFF3E0"
            ActivityType.PROMO_CREATED -> "🎁" to "#FCE4EC"
            ActivityType.PROMO_ACTIVATED -> "🎉" to "#F3E5F5"
            ActivityType.PRODUCT_ADDED -> "➕" to "#E0F2F1"
            ActivityType.PRODUCT_UPDATED -> "✏️" to "#FFF9C4"
            ActivityType.USER_REGISTERED -> "👤" to "#E1F5FE"
        }

        holder.tvActivityIcon.text = icon
        holder.tvActivityIcon.setBackgroundColor(android.graphics.Color.parseColor(backgroundColor))

        holder.tvActivityTitle.text = activity.activity
        holder.tvActivityDescription.text = activity.description
        holder.tvActivityTime.text = getShortRelativeTime(activity.timestamp)
    }

    override fun getItemCount() = activityList.size

    fun updateData(newList: List<ActivityLog>) {
        activityList = newList
        notifyDataSetChanged()
    }

    /**
     * Convert timestamp ke short relative time (2m, 1j, 3h, dll)
     */
    private fun getShortRelativeTime(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp.replace("Z", "").replace("+00", ""))
            val now = Date()
            val diff = now.time - (date?.time ?: 0)

            when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Baru"
                diff < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                    "${minutes}m"
                }
                diff < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    "${hours}j"
                }
                diff < TimeUnit.DAYS.toMillis(7) -> {
                    val days = TimeUnit.MILLISECONDS.toDays(diff)
                    "${days}h"
                }
                else -> {
                    val outputFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))
                    outputFormat.format(date!!)
                }
            }
        } catch (e: Exception) {
            "N/A"
        }
    }
}