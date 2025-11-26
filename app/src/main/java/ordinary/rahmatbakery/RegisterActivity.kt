package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import ordinary.rahmatbakery.api.SupabaseManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var tombolBack: ImageButton
    private lateinit var textLogin: TextView
    private lateinit var tombolRegister: Button
    private lateinit var inputEmail: EditText
    private lateinit var inputPass: EditText
    private lateinit var regisCard: CardView
    private lateinit var inputConfirmPass: EditText
    private lateinit var inputName: EditText
    private lateinit var inputPhone: EditText



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        regisCard = findViewById<CardView>(R.id.cardRegis)
        inputPass = findViewById(R.id.inputPass)
        inputEmail = findViewById(R.id.inputEmail)
        tombolRegister = findViewById(R.id.btnRegister)
        tombolBack = findViewById(R.id.back)
        textLogin = findViewById(R.id.keLogin)
        inputConfirmPass = findViewById(R.id.inputConfirmPass)
        inputName = findViewById(R.id.inputName)
        inputPhone = findViewById(R.id.inputPhone)


        regisCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .start()

        tombolRegister.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPass.text.toString()
            val confirmPass = inputConfirmPass.text.toString()
            val name = inputName.text.toString()
            val phoneInput = inputPhone.text.toString()

            if (!isValidPhone(phoneInput)) {
                Toast.makeText(this, "Nomor telepon tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (password != confirmPass) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val phone = normalizePhone(phoneInput)

            if (email.isEmpty() || password.isEmpty() || confirmPass.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, name, phone)
            }
        }

        tombolBack.setOnClickListener {
            goBack()
        }

        textLogin.setOnClickListener {
            goToLogin()
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val clean = phone.replace("-", "").replace(" ", "")

        return clean.matches(Regex("^\\+?\\d{9,15}$"))
    }

    fun normalizePhone(phone: String): String {
        var p = phone.replace(" ", "").replace("-", "")

        return when {
            p.startsWith("+") -> p
            p.startsWith("0") -> "+62" + p.substring(1)
            else -> "+62$p" // fallback
        }
    }


    private fun registerUser(email: String, password: String, name: String, phone : String) {
        lifecycleScope.launch {
            try {

                val result = SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = buildJsonObject {
                        put("phone", JsonPrimitive(phone))    // <-- disimpan dalam user_metadata
                        put("full_name", JsonPrimitive(name))
                    }
                }

                Toast.makeText(
                    this@RegisterActivity,
                    "silahkan aktivasi melalui link yang dikirim di email",
                    Toast.LENGTH_SHORT
                ).show()
                // Pindah ke login
                goToLogin()


            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "gagal Mendaftar: ${e.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun animateOut(onEnd: () -> Unit) {
        regisCard.animate()
            .translationY(200f)
            .alpha(0f)
            .setDuration(200)
            .withEndAction(onEnd)
            .start()
    }

    private fun goToLogin() {
        animateOut {
            val intent = Intent(this, LoginActivity::class.java)

            val options = ActivityOptionsCompat.makeCustomAnimation(
                this,                // Context
                R.anim.fade_in,      // animasi masuk
                R.anim.fade_out      // animasi keluar
            )

            startActivity(intent, options.toBundle())
            this.finish()
        }
    }

    private fun goBack() {
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

}