package ordinary.rahmatbakery.pelanggan.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.DetailPaketAdapter
import ordinary.rahmatbakery.pelanggan.model.Paket
import ordinary.rahmatbakery.pelanggan.model.Produk

class DetailItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_paket)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val imgFoto: ImageView = findViewById(R.id.img_detail)
        val txtNama: TextView = findViewById(R.id.txt_nama_detail)
        val txtDeskripsi: TextView = findViewById(R.id.txt_deskripsi_detail)
        val rvDetailPaket: RecyclerView = findViewById(R.id.rv_detail_paket)
        val btnBack: ImageView = findViewById(R.id.btn_back_detail)

        btnBack.setOnClickListener { finish() }

        // Ambil data dari intent
        val tipe = intent.getStringExtra("TIPE")
        val produk = intent.getParcelableExtra<Produk>("PRODUK")
        val paket = intent.getParcelableExtra<Paket>("PAKET")

        if (tipe == "produk" && produk != null) {
            txtNama.text = produk.nama
            txtDeskripsi.text = produk.deskripsi
            imgFoto.load(produk.gambar) {
                crossfade(true) // animasi lembut saat gambar muncul
                placeholder(R.drawable.placeholder) // opsional: gambar sementara
                error(R.drawable.error_image)       // opsional: jika gagal load
            }
            rvDetailPaket.visibility = RecyclerView.GONE
        } else if (tipe == "paket" && paket != null) {
            txtNama.text = paket.nama
            txtDeskripsi.text = paket.deskripsi
            imgFoto.load(paket.foto) {
                crossfade(true) // animasi lembut saat gambar muncul
                placeholder(R.drawable.placeholder) // opsional: gambar sementara
                error(R.drawable.error_image)       // opsional: jika gagal load
            }

            rvDetailPaket.layoutManager = LinearLayoutManager(this)
            rvDetailPaket.adapter = DetailPaketAdapter(paket.detail)
        }

    }
}