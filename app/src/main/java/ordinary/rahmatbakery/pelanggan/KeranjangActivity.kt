package ordinary.rahmatbakery.pelanggan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
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
import ordinary.rahmatbakery.pelanggan.adapter.KategoriAdapter
import ordinary.rahmatbakery.pelanggan.adapter.KeranjangAdapter
import ordinary.rahmatbakery.pelanggan.model.Keranjang
import ordinary.rahmatbakery.pelanggan.model.MenuProduk

class KeranjangActivity : AppCompatActivity() {


    private val listKeranjang = mutableListOf<Keranjang>()


    private lateinit var rvKeranjang: RecyclerView
    private lateinit var adapterKeranjang: KeranjangAdapter

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_keranjang)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.keranjang)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvKeranjang = findViewById(R.id.rvKeranjang)
        adapterKeranjang = KeranjangAdapter(listKeranjang)

        rvKeranjang.adapter = adapterKeranjang
        rvKeranjang.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        loadKeranjang()

    }

    fun loadKeranjang(){
        lifecycleScope.launch {
            val produk = SupabaseManager.client.from("produk")
                .select(columns = Columns.raw(
                    """id : id_produk,
                productName : nama_produk,
                productImg : foto_produk,
                productPrice : harga""".trimIndent()
                )) // Select all columns, or specify with Columns.list("name", "country_id")
                .decodeList<MenuProduk>()

            if(!produk.isEmpty()){
                var i = 1
                for (menuProduk in produk) {
                    listKeranjang.add(Keranjang("${i++}",menuProduk))
                }
                for (menuProduk in produk) {
                    listKeranjang.add(Keranjang("${i++}",menuProduk))
                }
                adapterKeranjang.notifyDataSetChanged()
            }
        }
    }
}