package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var tombolBack : ImageButton? = null
        var textLogin : TextView? = null

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val regisCard = findViewById<CardView>(R.id.cardRegis)

        regisCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .start()

        tombolBack = findViewById(R.id.back)
        tombolBack.setOnClickListener {
            regisCard.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    val intent = Intent(this, MainActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,                // Context
                        R.anim.fade_in,      // animasi masuk
                        R.anim.fade_out      // animasi keluar
                    )
                    startActivity(intent, options.toBundle())
                    this.finish()
                }
                .start()
        }

        textLogin = findViewById(R.id.keLogin)
        textLogin.setOnClickListener {
            regisCard.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    val intent = Intent(this, LoginActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this,                // Context
                        R.anim.fade_in,      // animasi masuk
                        R.anim.fade_out      // animasi keluar
                    )
                    startActivity(intent, options.toBundle())
                    this.finish()
                }
                .start()
        }

    }
}