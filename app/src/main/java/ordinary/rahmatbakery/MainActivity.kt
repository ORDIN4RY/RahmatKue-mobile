package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ordinary.rahmatbakery.ngetes.WebSocketClient
import ordinary.rahmatbakery.pelanggan.DashboardActivity
import ordinary.rahmatbakery.util.PrefManager

class MainActivity : AppCompatActivity() {


        private var btnLogin : Button? = null
        private var btnRegister : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val prefManager = PrefManager(this@MainActivity)
        if(prefManager.isLoggedIn()) {
            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLogin = findViewById(R.id.tombolLogin)
        btnRegister = findViewById(R.id.tombolRegister)

        val contentLayout = findViewById<View>(R.id.content)

        btnLogin?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,                // Context
                R.anim.fade_in,      // animasi masuk
                R.anim.fade_out      // animasi keluar
            )

            startActivity(intent, options.toBundle())
            this.finish()
        }

        btnRegister?.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,                // Context
                R.anim.fade_in,      // animasi masuk
                R.anim.fade_out      // animasi keluar
            )

            startActivity(intent, options.toBundle())
            this.finish()
        }


    }
}