package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ordinary.rahmatbakery.R
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.pelanggan.adapter.PesananAdapter
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import ordinary.rahmatbakery.pelanggan.model.PesananItem

class PesananFragment  : Fragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var orderAdapter: PesananAdapter

    // 1. Mengaitkan Layout Fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ganti R.layout.fragment_orders dengan nama file layout Fragment Anda
        // Layout ini harus berisi RecyclerView dengan ID rv_orders
        return inflater.inflate(R.layout.fragment_pesanan, container, false)
    }

    // 2. Inisialisasi View setelah Fragment View dibuat
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi RecyclerView Utama dari View Fragment
        recyclerView = view.findViewById(R.id.rv_pesanan)

        // 2. Siapkan Data Pesanan (Data Dummy dengan Item Bersarang)
        val orderList = createDummyOrderList()

        // 3. Inisialisasi dan Set Adapter Utama
        orderAdapter = PesananAdapter(orderList)
        recyclerView?.adapter = orderAdapter

        // 4. Set Layout Manager (Wajib untuk RecyclerView)
        // Gunakan requireContext() untuk mendapatkan Context dari Fragment
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
    }

    // Fungsi untuk membuat data dummy (sama seperti di Activity)
    private fun createDummyOrderList(): List<Pesanan> {
        // ... (Implementasi data dummy sama seperti sebelumnya) ...
        val items1 = listOf(
            PesananItem("Paket A", 25000.0, 2, "-"),
            PesananItem("Paket B", 5000.0, 3, "-")
        )
        val items2 = listOf(
            PesananItem("Kotak a", 150000.0, 1, "Ukuran L"),
            PesananItem("Kotak B", 250000.0, 1, "Ukuran XL")
        )

        return listOf(
            Pesanan( "18 Nov 2025", 65000.0, "Selesai", items1),
            Pesanan( "17 Nov 2025", 400000.0, "Diproses", items2),
              )
    }

    // 3. Penting: Bersihkan referensi View saat Fragment dihancurkan
    // Ini mencegah kebocoran memori (memory leak)
    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }
}