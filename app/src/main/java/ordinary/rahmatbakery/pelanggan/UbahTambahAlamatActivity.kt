package ordinary.rahmatbakery.pelanggan

import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.model.Alamat
import ordinary.rahmatbakery.util.AuthRepository

class UbahTambahAlamatActivity(
    private val repo : AuthRepository = AuthRepository()
) : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var cbAlamatUtama: SwitchCompat
    private lateinit var inputNama: EditText
    private lateinit var inputNoHp: EditText
    private lateinit var inputAlamat: EditText
    private var alamat : Alamat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ubah_tambah_alamat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        alamat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_ALAMAT", Alamat::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_ALAMAT")
        }

        if (alamat == null) {
            prepareAdd()
        }else{
            prepareEdit()
        }

    }

    private fun prepareAdd(){
        initListenerAdd()
        btnHapus.visibility = Button.GONE
    }

    private fun initListenerAdd(){
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            addAlamat()
        }
    }

    private fun addAlamat(){
        val nama = inputNama.text.toString().trim()
        val noHp = inputNoHp.text.toString().trim()
        val alamatRumah = inputAlamat.text.toString().trim()
        val isUtama = cbAlamatUtama.isChecked

        if (nama.isEmpty() || noHp.isEmpty() || alamatRumah.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val currentUser = repo.getCurrentProfile()

                if (currentUser == null) {
                    Toast.makeText(
                        this@UbahTambahAlamatActivity,
                        "Gagal memuat sesi pengguna.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                    return@launch
                }

                SupabaseManager.client.postgrest.from("alamat").insert(
                    Alamat(
                        idUser = currentUser.id,
                        nama = nama,
                        noHp = noHp,
                        alamat = alamatRumah,
                        isUtama = isUtama
                    )


                )
                setResult(RESULT_OK)
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Alamat berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@UbahTambahAlamatActivity,
                    "Galat: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.back)
        btnSimpan = findViewById(R.id.btn_simpan)
        btnHapus = findViewById(R.id.btn_hapus)
        cbAlamatUtama = findViewById(R.id.cb_alamat_utama)
        inputNama = findViewById(R.id.input_nama)
        inputAlamat = findViewById(R.id.input_alamat)
        inputNoHp = findViewById(R.id.input_no_hp)
    }

    private fun initListenerEdit() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSimpan.setOnClickListener {
            updateAlamat()
        }

        btnHapus.setOnClickListener {
            deleteAlamat()
        }
    }

    private fun prepareEdit() {
        inputNama.setText(alamat?.nama)
        inputNoHp.setText(alamat?.noHp)
        inputAlamat.setText(alamat?.alamat)
        cbAlamatUtama.isChecked = alamat?.isUtama ?:false
        btnHapus.visibility = Button.VISIBLE

        initListenerEdit()
    }

    private fun updateAlamat() {
        val nama = inputNama.text.toString().trim()
        val noHp = inputNoHp.text.toString().trim()
        val alamatRumah = inputAlamat.text.toString().trim()

        if (nama.isEmpty() || noHp.isEmpty() || alamatRumah.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest.from("alamat").update(
                    {
                        set("nama_lengkap", nama)
                        set("no_hp_penerima", noHp)
                        set("alamat_rumah", alamatRumah)
                        set("alamat_utama", cbAlamatUtama.isChecked) // jika ada kolomnya
                    }
                ) {
                    filter {
                        eq("id_alamat", alamat!!.id!!)
                    }
                }
                setResult(RESULT_OK)
                Toast.makeText(this@UbahTambahAlamatActivity, "Alamat berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish() // Tutup activity setelah berhasil
            } catch (e: Exception) {
                Toast.makeText(this@UbahTambahAlamatActivity, "Gagal memperbarui: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteAlamat() {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest.from("alamat").delete {
                    filter {
                        eq("id_alamat", alamat!!.id!!)
                    }
                }
                Toast.makeText(this@UbahTambahAlamatActivity, "Alamat berhasil dihapus", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@UbahTambahAlamatActivity, "Gagal menghapus: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}