package ordinary.rahmatbakery.pelanggan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.LoginActivity
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.model.Profile
import ordinary.rahmatbakery.util.AuthRepository
import ordinary.rahmatbakery.util.PrefManager

class DashboardActivity(
    private val repo: AuthRepository = AuthRepository()
) : AppCompatActivity() {

    var profile : Profile?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        var navbar : BottomNavigationView? = null

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch{
            profile = repo.getCurrentProfile()
            if(profile == null){
                AlertDialog.Builder(this@DashboardActivity)
                    .setTitle("Sesi Habis")
                    .setMessage("Mohon login kembali")
                    .setPositiveButton("OK") { _, _ ->
                        val intent = Intent(this@DashboardActivity, LoginActivity::class.java)

                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            this@DashboardActivity,                // Context
                            R.anim.fade_in,      // animasi masuk
                            R.anim.fade_out      // animasi keluar
                        )

                        startActivity(intent, options.toBundle())
                        this@DashboardActivity.finish()
                    }
            }
            if (savedInstanceState == null) {
                replaceFragment(BerandaFragment())
            }
        }

        navbar = findViewById(R.id.bottomNav)

        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(BerandaFragment())
                    true
                }
                R.id.menu -> {
                    replaceFragment(MenuFragment())
                    true
                }
                R.id.order -> {
                    replaceFragment(PesananFragment())
                    true
                }
                R.id.setting -> {
                    replaceFragment(PengaturanFragment())
                    true
                }
                else -> false
            }
        }

    }
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}