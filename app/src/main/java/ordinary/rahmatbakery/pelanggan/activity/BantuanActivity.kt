package ordinary.rahmatbakery.pelanggan.activity

import ordinary.rahmatbakery.pelanggan.adapter.HelpAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.pelanggan.model.HelpItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ordinary.rahmatbakery.R

class BantuanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bantuan)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Baca JSON dari res/raw/help_data.json
        val inputStream = resources.openRawResource(R.raw.help_center)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val listType = object : TypeToken<List<HelpItem>>() {}.type
        val helpList: List<HelpItem> = Gson().fromJson(jsonString, listType)

        recyclerView.adapter = HelpAdapter(helpList)
    }
}
