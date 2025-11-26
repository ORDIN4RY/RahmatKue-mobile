package ordinary.rahmatbakery.pelanggan.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.GantiPasswordActivity
import ordinary.rahmatbakery.LoginActivity
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.pelanggan.activity.AlamatActivity
import ordinary.rahmatbakery.pelanggan.activity.BantuanActivity
import ordinary.rahmatbakery.pelanggan.activity.EditProfileActivity
import ordinary.rahmatbakery.pelanggan.activity.VoucherActivity

class PengaturanFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_pengaturan, container, false)

        // Ambil tombol dari layout fragment
        val logout = rootView.findViewById<LinearLayout>(R.id.logout)
        val btnVoucher = rootView.findViewById<LinearLayout>(R.id.btn_voucher)
        val btnAlamat = rootView.findViewById<LinearLayout>(R.id.btn_alamat)
        val btnBantuan = rootView.findViewById<LinearLayout>(R.id.btn_bantuan)
        val btnGantiPass = rootView.findViewById<LinearLayout>(R.id.btn_ganti_password)
        val btnProfil = rootView.findViewById<CardView>(R.id.profile_card)


        // Set aksi klik
        btnAlamat.setOnClickListener {
            val intent = Intent(requireContext(), AlamatActivity::class.java)
            startActivity(intent)
        }
        btnBantuan.setOnClickListener {
            val intent = Intent(requireContext(), BantuanActivity::class.java)
            startActivity(intent)
        }

        btnVoucher.setOnClickListener {
            val intent = Intent(requireContext(), VoucherActivity::class.java)
            startActivity(intent)
        }
        btnGantiPass.setOnClickListener {
            val intent = Intent (requireContext(), GantiPasswordActivity::class.java)
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