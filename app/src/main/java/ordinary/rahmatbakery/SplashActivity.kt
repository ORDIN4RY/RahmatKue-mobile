package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.DashboardActivity
import ordinary.rahmatbakery.util.AuthRepository

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<android.widget.ImageView>(R.id.logoImage)
        val appName = findViewById<android.widget.TextView>(R.id.appName)

        // Animasi fade-in
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000
        fadeIn.fillAfter = true

        logo.startAnimation(fadeIn)
        appName.startAnimation(fadeIn)

        lifecycleScope.launch {
            delay(800)
            checkSession()
        }
    }

    private suspend fun checkSession() {

        SupabaseManager.client.auth.sessionStatus.collect { status ->
            when (status) {
                is SessionStatus.Authenticated -> {
                    Toast.makeText(this@SplashActivity, "Selamat Datang Kembali!", Toast.LENGTH_SHORT).show()
                    goToDashboard()
                }
                is SessionStatus.NotAuthenticated -> {
                    goToMain()
                }
                is SessionStatus.RefreshFailure -> {
                    Toast.makeText(this@SplashActivity, "Session refresh failed", Toast.LENGTH_SHORT).show()
                    goToMain()
                }
                is SessionStatus.Initializing -> {
                    // Saat library sedang inisialisasi atau memuat sesi dari penyimpanan,
                    // kita tidak melakukan apa-apa. Biarkan splash screen tetap terlihat
                    // sambil menunggu status berikutnya.
                }

                else -> {
                    // Blok 'else' ini wajib ada untuk menangani semua kemungkinan status lain
                    // yang tidak tercakup secara eksplisit. Ini membuat 'when' menjadi lengkap.
                }
            }
        }

    }

    private fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,                // Context
            R.anim.fade_in,      // animasi masuk
            R.anim.fade_out      // animasi keluar
        )

        startActivity(intent, options.toBundle())
        this.finish()
    }
    private fun goToDashboard(){
        val intent = Intent(this, DashboardActivity::class.java)

        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,                // Context
            R.anim.fade_in,      // animasi masuk
            R.anim.fade_out      // animasi keluar
        )

        startActivity(intent, options.toBundle())
        this.finish()
    }
}
