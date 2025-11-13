package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.model.Profile
import ordinary.rahmatbakery.pelanggan.activity.DashboardActivity as db_pelanggan
import ordinary.rahmatbakery.admin.activity.DashboardActivity as db_admin

class LoginActivity : AppCompatActivity() {


    private val viewModel: LoginViewModel by viewModels()
    private lateinit var inputEmail: EditText
    private lateinit var inputPass: EditText
    private lateinit var tombolLogin: Button
    private lateinit var tombolBack: ImageButton
    private lateinit var textRegis: TextView
    private lateinit var loginCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //inisialisasi
        inputEmail = findViewById(R.id.inputEmail)
        inputPass = findViewById(R.id.inputPass)
        tombolLogin = findViewById(R.id.btnLogin)
        tombolBack = findViewById(R.id.back)
        textRegis = findViewById(R.id.keRegis)
        loginCard = findViewById(R.id.cardlogin)


        loginCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .start()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is LoginViewModel.AuthState.Idle -> showLoading(false)
                    is LoginViewModel.AuthState.Loading -> showLoading(true)
                    is LoginViewModel.AuthState.Success -> {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, "Login berhasil!", Toast.LENGTH_SHORT).show()
                        goToDashboard(state.profile)
                    }
                    is LoginViewModel.AuthState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        tombolLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPass.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(email, password)
            }
        }

        tombolBack.setOnClickListener {
            goBack()
        }

        textRegis.setOnClickListener {
            goToRegister()
        }


    }

    private fun animateOut(onEnd: () -> Unit) {
        loginCard.animate()
            .translationY(200f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction(onEnd)
            .start()
    }

    private fun showLoading(show: Boolean) {
        val overlay = findViewById<FrameLayout>(R.id.loadingOverlay)
        val card = findViewById<CardView>(R.id.cardlogin)

        if (show) {
            overlay.alpha = 1f
            overlay.visibility = View.VISIBLE
            overlay.animate().alpha(1f).setDuration(200).start()
            card.animate().alpha(0.5f).setDuration(200).start()
        } else {
            overlay.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction { overlay.visibility = View.GONE }
                .start()
            card.animate().alpha(1f).setDuration(200).start()
        }
    }

    private fun goToDashboard(profile: Profile) {
        animateOut {
            var intent = Intent(this, db_pelanggan::class.java)
            if(profile.level == "admin"){
                intent = Intent(this, db_admin::class.java)
            } else{
                intent = Intent(this, db_pelanggan::class.java)
            }

            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                R.anim.fade_in,
                R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            finish()
        }
    }

    private fun goBack(){
        animateOut {
            val intent = Intent(this, MainActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,                // Context
                R.anim.fade_in,
                R.anim.fade_out
            )

            startActivity(intent, options.toBundle())
            this.finish()
        }
    }

    private fun goToRegister(){
        animateOut {
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


