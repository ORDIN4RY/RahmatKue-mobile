package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        var navbar : BottomNavigationView? = null

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navbar = findViewById(R.id.bottomNav)
        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // aksi Home
                    true
                }
                R.id.menu -> {
                    // aksi Profile
                    true
                }
                R.id.map -> {
                    // aksi Profile
                    true
                }
                R.id.contact -> {
                    // kirim pesan ke WhatsApp
                    val message = "saya ingin membagikan pesan berikut."

                    try {
                        val share = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, message)
                            type = "text/plain"
                        }
                        startActivity(Intent.createChooser(share, "bagikan melalui:"))

                    } catch (e: Exception) {
                        Toast.makeText(this, "tidak ada aplikasi yang tersedia untuk berbagi", Toast.LENGTH_SHORT).show()
                    }


                    true
                }
                else -> false
            }
        }



    }
}