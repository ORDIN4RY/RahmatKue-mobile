package ordinary.rahmatbakery.admin.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.adapter.ActivityLogAdapter
import ordinary.rahmatbakery.admin.activity.DashboardRepository
import java.util.Calendar

class NotifActivity : AppCompatActivity() {

    private lateinit var rvActivityLog: RecyclerView
    private lateinit var layoutEmptyActivity: LinearLayout
    private lateinit var activityLogAdapter: ActivityLogAdapter

    private var selectedBulan: Int = 0 // 0 = Semua Bulan
    private var selectedTahun: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notif)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // BACK BUTTON
        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        // INIT VIEW
        rvActivityLog = findViewById(R.id.rvActivityLog)
        layoutEmptyActivity = findViewById(R.id.layoutEmptyActivity)

        // RECYCLER VIEW SETUP
        setupRecyclerViews()

        // LOAD DATA
        loadActivityLog()
    }

    private fun setupRecyclerViews() {
        activityLogAdapter = ActivityLogAdapter(emptyList())

        rvActivityLog.apply {
            layoutManager = LinearLayoutManager(this@NotifActivity)
            adapter = activityLogAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadActivityLog() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activities = DashboardRepository.getActivityLog(
                    bulan = if (selectedBulan == 0) null else selectedBulan,
                    tahun = selectedTahun,
                    limit = 25
                )

                withContext(Dispatchers.Main) {
                    if (activities.isEmpty()) {
                        rvActivityLog.visibility = android.view.View.GONE
                        layoutEmptyActivity.visibility = android.view.View.VISIBLE
                    } else {
                        rvActivityLog.visibility = android.view.View.VISIBLE
                        layoutEmptyActivity.visibility = android.view.View.GONE
                        activityLogAdapter.updateData(activities)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    rvActivityLog.visibility = android.view.View.GONE
                    layoutEmptyActivity.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
}
