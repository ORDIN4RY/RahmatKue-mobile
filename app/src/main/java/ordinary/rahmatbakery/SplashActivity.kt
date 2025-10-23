package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.DashboardActivity

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
        val session = SupabaseManager.client.auth.currentSessionOrNull()

        if (session != null) {
            goToDashboard()
        } else {
            goToMain()
        }

        finish()
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
