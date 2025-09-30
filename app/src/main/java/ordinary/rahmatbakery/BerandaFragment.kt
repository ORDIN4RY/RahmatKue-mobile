package ordinary.rahmatbakery

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
class BerandaFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Ambil tombol dari layout fragment
        val btnNotif = rootView.findViewById<ImageView>(R.id.icon_notif)

        // Set aksi klik
        btnNotif.setOnClickListener {
            val intent = Intent(requireContext(), NotifActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }

}