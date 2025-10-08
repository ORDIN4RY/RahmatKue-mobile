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

        loginCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .start()

        tombolLogin = findViewById(R.id.btnLogin)
        tombolLogin?.setOnClickListener {
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
    fun loginUser(username: String, password: String) {
        val api = RetrofitClient.instance
        val request = LoginRequest(username, password)

        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.status == "success") {
                        // Simpan ke SharedPreferences
                        val prefManager = PrefManager(this@LoginActivity)

                        val username = response.body()?.username ?: ""
                        val userId = response.body()?.user_id ?: 0

                        prefManager.saveLogin(username, userId)

                        Toast.makeText(
                            this@LoginActivity,
                            "Login Berhasil, Selamat datang ${loginResponse.username}",
                            Toast.LENGTH_LONG
                        ).show()

                        // Pindah ke Dashboard
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            loginResponse?.message ?: "Login gagal",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Response tidak valid dari server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    " ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}