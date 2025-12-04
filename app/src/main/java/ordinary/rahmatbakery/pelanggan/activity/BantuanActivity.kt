package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.HelpAdapter
import ordinary.rahmatbakery.pelanggan.model.HelpItem

class BantuanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bantuan)

        val btnHubungiAdmin = findViewById<Button>(R.id.btnHubungiAdmin)
        btnHubungiAdmin.setOnClickListener {
            val url = "https://wa.me/+6283198720209?text=Halo,+Saya+membutuhkan+bantuan"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val inputStream = resources.openRawResource(R.raw.help_center)
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val listType = object : TypeToken<List<HelpItem>>() {}.type
        val helpList: List<HelpItem> = Gson().fromJson(jsonString, listType)

        recyclerView.adapter = HelpAdapter(helpList)
    }
}
