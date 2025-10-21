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
import ordinary.rahmatbakery.api.ApiService
import ordinary.rahmatbakery.api.RetrofitClient
import ordinary.rahmatbakery.model.LoginRequest
import ordinary.rahmatbakery.model.LoginResponse
import ordinary.rahmatbakery.pelanggan.DashboardActivity
import ordinary.rahmatbakery.util.PrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import  ordinary.rahmatbakery.api.SupabaseManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {


    var tombolBack: ImageButton? = null
    var textRegis: TextView? = null
    var tombolLogin: Button? = null

    var inputEmail: EditText? = null
    var inputPass: EditText? = null

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

        loginCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .start()

        tombolLogin = findViewById(R.id.btnLogin)
        tombolLogin?.setOnClickListener {
            val email = inputEmail?.text.toString()
            val password = inputPass?.text.toString()

            if (inputEmail?.text.toString().isEmpty() || inputPass?.text.toString().isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            } else {

                loginUser(inputEmail?.text.toString(), inputPass?.text.toString())

//                loginCard.animate()
//                .withEndAction {
//                    val intent = Intent(this, DashboardActivity::class.java)
//                    val options = ActivityOptionsCompat.makeCustomAnimation(
//                        this,                // Context
//                        R.anim.fade_in,      // animasi masuk
//                        R.anim.fade_out      // animasi keluar
//                    )
//
//                    startActivity(intent, options.toBundle())
//                    this.finish()
//                }
//                .start()


            }

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

    // AuthViewModel.kt (lanjutan)
    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val result = SupabaseManager.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                // Jika berhasil
                val session = SupabaseManager.client.auth.currentSessionOrNull()
                if (session != null) {
                    Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()

                    // Pindah ke DashboardActivity
                    val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this@LoginActivity,
                        R.anim.fade_in,
                        R.anim.fade_out
                    )
                    startActivity(intent, options.toBundle())
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Session tidak ditemukan!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login gagal: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}


