package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.model.Promo
import android.widget.TextView
import coil.load

class DetailPromoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_promo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.promo_terbaru)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val promo = intent.getParcelableExtra<Promo>("data_promo_terbaru")
        if (promo != null) {
            findViewById<TextView>(R.id.detail_promo_name).text = promo.nama
            findViewById<TextView>(R.id.detail_promo_description).text = promo.deskripsi
            findViewById<ImageView>(R.id.iv_detail_promo_image).load(promo.fotoBanner)
        }
    }
}