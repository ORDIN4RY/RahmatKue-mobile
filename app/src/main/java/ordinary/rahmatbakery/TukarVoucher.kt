 package ordinary.rahmatbakery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.pelanggan.adapter.TukarVoucherAdapter
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TukarVoucher.newInstance] factory method to
 * create an instance of this fragment.
 */
class TukarVoucher : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout fragment_tukar_voucher.xml
        val view = inflater.inflate(R.layout.fragment_tukar_voucher, container, false)

        // Inisialisasi RecyclerView dari view, bukan dari Fragment langsung
        val recyclerView = view.findViewById<RecyclerView>(R.id.rec_view_voucher)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 2 kolom grid

//        // Contoh data (bisa kamu ganti nanti dengan data dari database / API)
//        val dataProduk = listOf(
//            TukarVoucher("Pupuk Organik", 25000, R.drawable.pupuk_organik),
//            TukarVoucher("Pupuk Urea", 30000, R.drawable.pupuk_urea),
//            TukarVoucher("Pupuk NPK", 35000, R.drawable.pupuk_npk)
//        )
//
//        // Set adapter-nya
//        recyclerView.adapter = ProdukAdapter(dataProduk)

        return view
    }
}

