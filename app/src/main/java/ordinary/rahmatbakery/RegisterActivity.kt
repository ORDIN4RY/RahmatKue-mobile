package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.DashboardActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var tombolBack : ImageButton? = null
        var textLogin : TextView? = null
        var tombolRegister: Button? = null
        var inputEmail: EditText? = null
        var inputPass: EditText? = null

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val regisCard = findViewById<CardView>(R.id.cardRegis)
        inputPass = findViewById(R.id.inputPass)
        inputEmail = findViewById(R.id.inputEmail)

        regisCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .start()

        tombolRegister = findViewById(R.id.btnRegister)
        tombolRegister?.setOnClickListener {
            val email = inputEmail?.text.toString()
            val password = inputPass?.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password)
            }
        }

        tombolBack = findViewById(R.id.back)
        tombolBack.setOnClickListener {
            regisCard.animate()
                .translationY(200f)
                .alpha(0f)
                .setDuration(300)
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
                .setDuration(300)
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

    private fun registerUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val result = SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                // Jika berhasil
                val session = SupabaseManager.client.auth.currentSessionOrNull()
                if (session != null) {
                    Toast.makeText(this@RegisterActivity, "berhasil mendaftar!", Toast.LENGTH_SHORT).show()

                    // Pindah ke DashboardActivity
                    val regisCard = findViewById<CardView>(R.id.cardRegis)
                    regisCard.animate()
                        .translationY(200f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this@RegisterActivity,
                                R.anim.fade_in,
                                R.anim.fade_out
                            )
                            startActivity(intent, options.toBundle())
                            finish()
                        }
                        .start()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Session tidak ditemukan!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "gagal Mendaftar: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

}