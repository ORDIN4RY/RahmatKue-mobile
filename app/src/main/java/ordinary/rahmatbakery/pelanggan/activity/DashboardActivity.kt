package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.LoginActivity
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.model.Profile
import ordinary.rahmatbakery.pelanggan.fragment.BerandaFragment
import ordinary.rahmatbakery.pelanggan.fragment.MenuFragment
import ordinary.rahmatbakery.pelanggan.fragment.PengaturanFragment
import ordinary.rahmatbakery.pelanggan.fragment.PesananFragment
import ordinary.rahmatbakery.util.AuthRepository

class DashboardActivity(
    private val repo: AuthRepository = AuthRepository()
) : AppCompatActivity() {

    var profile : Profile?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pelanggan_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var navbar = findViewById<BottomNavigationView>(R.id.bottomNav)
        var btnNotif = findViewById<ImageView>(R.id.icon_notif)
        var btnCart = findViewById<ImageView>(R.id.icon_cart)
        var judul = findViewById<TextView>(R.id.judul_halaman)

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
                judul.setText("Beranda")
            }
        }

        navbar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(BerandaFragment())
                    judul.setText("Beranda")
                    true
                }
                R.id.menu -> {
                    replaceFragment(MenuFragment())
                    judul.setText("Menu Produk")
                    true
                }
                R.id.order -> {
                    replaceFragment(PesananFragment())
                    judul.setText("Daftar Pesanan")
                    true
                }
                R.id.setting -> {
                    replaceFragment(PengaturanFragment())
                    judul.setText("Profil")
                    true
                }
                else -> false
            }
        }


        btnNotif.setOnClickListener {
            val intent = Intent(this, NotifActivity::class.java)
            startActivity(intent)
        }
        btnCart.setOnClickListener {
            val intent = Intent(this, KeranjangActivity::class.java)
            startActivity(intent)
        }

    }
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}