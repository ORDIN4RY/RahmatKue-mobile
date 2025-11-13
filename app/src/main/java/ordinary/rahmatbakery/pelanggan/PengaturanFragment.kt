package ordinary.rahmatbakery.pelanggan

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.BantuanActivity
import ordinary.rahmatbakery.LoginActivity
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.util.PrefManager

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PengaturanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PengaturanFragment : Fragment() {
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pengaturan, container, false)

        // Ambil tombol dari layout fragment
        val btnNotif = rootView.findViewById<ImageView>(R.id.icon_notif)
        val btnCart = rootView.findViewById<ImageView>(R.id.icon_cart)
        val logout = rootView.findViewById<TextView>(R.id.logout)
        val btnVoucher = rootView.findViewById<TextView>(R.id.btn_voucher)
        val btnAlamat = rootView.findViewById<TextView>(R.id.btn_alamat)
        val btnBantuan = rootView.findViewById<TextView>(R.id.btn_bantuan)


        // Set aksi klik
        btnNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifActivity::class.java)
            startActivity(intent)
        }
        btnCart.setOnClickListener {
            val intent = Intent(requireContext(), KeranjangActivity::class.java)
            startActivity(intent)
        }
        btnAlamat.setOnClickListener {
            val intent = Intent(requireContext(), AlamatActivity::class.java)
            startActivity(intent)
        }
        btnBantuan.setOnClickListener {
            val intent = Intent(requireContext(), BantuanActivity::class.java)
            startActivity(intent)
        }


        logout.setOnClickListener {

            lifecycleScope.launch {
                try {
                    val response = SupabaseManager.client.auth.signOut()
                    // atau SessionManager.logout(context)
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    requireActivity().finish()
                    startActivity(intent)

                } catch (e: Exception) {
                    Toast.makeText(requireActivity(), "Gagal logout: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }
}