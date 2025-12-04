package ordinary.rahmatbakery

import android.os.Bundle
import android.util.Log
import android.widget.Button // Tambahkan import Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager

class GantiPasswordActivity : AppCompatActivity() {
    private val supabaseClient = SupabaseManager.client

    private lateinit var editTextPasswordSaatIni: EditText
    private lateinit var editTextPasswordBaru: EditText
    private lateinit var editTextKonfirmasiPasswordBaru: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ganti_password)


        editTextPasswordSaatIni = findViewById(R.id.password_saat_ini)
        editTextPasswordBaru = findViewById(R.id.password_baru)
        editTextKonfirmasiPasswordBaru = findViewById(R.id.konfirmasi_password_baru)

        val buttonGantiPassword = findViewById<Button>(R.id.btn_ganti_password) // Asumsi ID tombol Anda
        buttonGantiPassword.setOnClickListener {
            onSaveProfileClicked()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun onSaveProfileClicked() {

        val passwordSaatIni = editTextPasswordSaatIni.text.toString()
        val passwordBaru = editTextPasswordBaru.text.toString()
        val konfirmasiPasswordBaru = editTextKonfirmasiPasswordBaru.text.toString()

        if (passwordSaatIni.trim().isEmpty() || passwordBaru.trim().isEmpty() || konfirmasiPasswordBaru.trim().isEmpty()) {
            Toast.makeText(this, "Semua kolom password harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru != konfirmasiPasswordBaru) {
            Toast.makeText(this, "Password baru dan konfirmasi password tidak cocok.", Toast.LENGTH_SHORT).show()
            return
        }

        if (passwordBaru.length < 6) {
            Toast.makeText(this, "Password baru minimal 6 karakter.", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {

            val isPasswordUpdated = updatePassword(passwordBaru)


            if (isPasswordUpdated) {
                Toast.makeText(this@GantiPasswordActivity, "Password berhasil diperbarui!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                 Toast.makeText(this@GantiPasswordActivity, "Gagal memperbarui password. Pastikan Anda sudah login.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun updatePassword(newPassword: String): Boolean {
        return try {
            supabaseClient.auth.updateUser {
                password = newPassword
            }
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Gagal memperbarui password: ${e.message}")
            false
        }
    }
}
