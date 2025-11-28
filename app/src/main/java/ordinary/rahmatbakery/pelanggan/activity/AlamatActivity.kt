package ordinary.rahmatbakery.pelanggan.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.AlamatAdapter
import ordinary.rahmatbakery.pelanggan.adapter.AlamatClickListener
import ordinary.rahmatbakery.pelanggan.model.Alamat
import ordinary.rahmatbakery.util.AuthRepository
import android.widget.TextView

class AlamatActivity(
    private val repo: AuthRepository = AuthRepository()
) : AppCompatActivity(), AlamatClickListener {

    private lateinit var adapterAlamat: AlamatAdapter
    private lateinit var btnAddAlamat: Button
    private lateinit var btnBack: ImageView
    private val listAlamat = mutableListOf<Alamat>()
    private var isSelectionMode = false // TAMBAHKAN FLAG UNTUK MENANDAI MODE
    private lateinit var tvTitle : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alamat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isSelectionMode = intent.getBooleanExtra("SELECTION_MODE", false)


        btnAddAlamat = findViewById(R.id.btn_add_alamat)
        btnBack = findViewById(R.id.back)
        tvTitle = findViewById(R.id.title_alamat)

        btnBack.setOnClickListener {
            finish()
        }
        if (isSelectionMode) {
            tvTitle.text = "Pilih Alamat Pengiriman"
        }

        btnAddAlamat.setOnClickListener {
            val intent = Intent(this, UbahTambahAlamatActivity::class.java)
            alamatResultLauncher.launch(intent)
        }

        setupRecyclerView()
        loadAlamat()
    }

    private val alamatResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadAlamat()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.rvAlamat) // Ganti ID
        adapterAlamat = AlamatAdapter(listAlamat, this)
        recyclerView.adapter = adapterAlamat

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    fun loadAlamat() {
        lifecycleScope.launch {
            val currentUser = repo.getCurrentProfile()

            if (currentUser == null) {
                Toast.makeText(
                    this@AlamatActivity,
                    "Gagal memuat sesi pengguna.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@launch
            }

            val listAlamatFromDb = SupabaseManager.client.from("alamat")
                .select(Columns.ALL)
                 {
                    filter {
                        eq("id_user", currentUser.id)
                    }
                }
                .decodeList<Alamat>()

            listAlamat.clear()

            listAlamat.addAll(listAlamatFromDb)

            adapterAlamat.notifyDataSetChanged()
        }
    }


    override fun onAlamatClicked(alamat: Alamat) {
        if (isSelectionMode) {
            // MODE SELEKSI: Kirim alamat yang dipilih kembali dan tutup.
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_ALAMAT", alamat)
            setResult(RESULT_OK, resultIntent)
            finish() // Tutup activity ini
        } else {
            // MODE MANAJEMEN (DEFAULT): Buka activity untuk mengedit alamat.
            val intent = Intent(this, UbahTambahAlamatActivity::class.java)
            intent.putExtra("EXTRA_ALAMAT", alamat)
            alamatResultLauncher.launch(intent) // Gunakan launcher untuk memulai activity
        }
    }

    override fun onEditClicked(alamat: Alamat){
        val intent = Intent(this, UbahTambahAlamatActivity::class.java)
        intent.putExtra("EXTRA_ALAMAT", alamat)
        alamatResultLauncher.launch(intent) // Gunakan launcher untuk memulai activity
    }
}