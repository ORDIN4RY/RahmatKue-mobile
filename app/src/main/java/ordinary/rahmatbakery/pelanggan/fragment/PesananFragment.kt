package ordinary.rahmatbakery.pelanggan.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.util.SupabaseManager
import ordinary.rahmatbakery.pelanggan.adapter.PesananAdapter
import ordinary.rahmatbakery.pelanggan.model.Pesanan

class PesananFragment : Fragment() {

    private lateinit var rvPesanan: RecyclerView
    private lateinit var pesananAdapter: PesananAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pesanan, container, false)

        setupRecyclerView(root)
        loadPesanan()

        return root
    }

    private fun setupRecyclerView(view: View) {
        rvPesanan = view.findViewById(R.id.rv_pesanan)
        pesananAdapter = PesananAdapter(mutableListOf()) // Mulai dengan list kosong
        rvPesanan.layoutManager = LinearLayoutManager(requireContext())
        rvPesanan.adapter = pesananAdapter
    }

    private fun loadPesanan() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Ambil user dengan aman, tanpa operator '!!'
                val user = SupabaseManager.client.auth.currentUserOrNull() ?: run {
                    Toast.makeText(
                        requireContext(), "Sesi berakhir, silakan login kembali", Toast.LENGTH_SHORT
                    ).show()
                    pesananAdapter.updateData(emptyList()) // Kosongkan tampilan
                    return@launch
                }

                // 2. Ambil semua data dalam SATU PANGGILAN JARINGAN yang efisien
                val daftarPesanan: List<Pesanan> =
                    SupabaseManager.client.postgrest.from("transaksi").select(
                            Columns.raw(
                                """
                            id_transaksi,
                        total_harga,
                        status,
                        created_at,
                         metode_pengambilan,
                    waktu_selesai,
                    catatan,
                    ongkir,
                    nomor_pesanan,
                    dp_minimal,
                    potongan,
                        detail_transaksi_produk (
                            jumlah,
                            subtotal,
                            produk:produk (
                                id_produk,
                                nama_produk,
                                harga,
                                foto_produk,
                                varian
                            )
                        ),
                        
                        detail_transaksi_paket (
                            jumlah,
                            subtotal,
                            paket:paket (
                                id_paket,
                                nama_paket,
                                harga_paket,
                                foto_paket
                            )
                        )
                        """
                            )
                        ) {
                            filter { eq("id_user", user.id) }
                            order("created_at", Order.DESCENDING) // Urutkan dari yang terbaru
                        }.decodeList<Pesanan>() // Decode langsung ke List<Pesanan>

                // 3. Periksa apakah hasilnya kosong
                if (daftarPesanan.isEmpty()) {
                    Toast.makeText(
                        requireContext(), "Anda belum memiliki pesanan", Toast.LENGTH_SHORT
                    ).show()
                }

                if (isAdded) {
                    pesananAdapter.updateData(daftarPesanan)
                }// 4. Kirim data ke adapter untuk ditampilkan

            } catch (e: Exception) {
                // Cetak error ke Logcat agar kita bisa melihat pesan lengkapnya
                Log.e("PesananFragment", "Error loading data", e)
                Toast.makeText(
                    requireContext(), "Gagal memuat data: ${e.message}", Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
