package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {


    var tombolBack : ImageButton? = null
    var textRegis : TextView? = null
    var tombolLogin : Button? = null

    var inputEmail : EditText? = null
    var inputPass : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        inputPass = findViewById(R.id.inputPass)
        inputEmail = findViewById(R.id.inputEmail)

        val loginCard = findViewById<CardView>(R.id.cardlogin)
        tombolLogin = findViewById(R.id.btnLogin)
        tombolLogin?.setOnClickListener {
            if(inputEmail?.text.toString().isEmpty() || inputPass?.text.toString().isEmpty()){
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            }else{
            loginCard.animate()
                .withEndAction {
                    val intent = Intent(this, DashboardActivity::class.java)
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

        loginCard.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(400)
                    .start()

        tombolBack = findViewById(R.id.back)
        tombolBack?.setOnClickListener {
            loginCard.animate()
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

        textRegis = findViewById(R.id.keRegis)
        textRegis?.setOnClickListener {
            loginCard.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    val intent = Intent(this, RegisterActivity::class.java)
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