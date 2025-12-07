package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.admin.activity.OrderDetailActivity
import ordinary.rahmatbakery.admin.model.OrderAdmin

class OrderAddressFragment : Fragment() {

    private lateinit var order: OrderAdmin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order = (activity as? OrderDetailActivity)?.order ?: return

        val alamat = order.alamat

        view.findViewById<TextView>(R.id.tvNamaLengkap).text = alamat?.namaLengkap ?: "-"
        view.findViewById<TextView>(R.id.tvNoHp).text = alamat?.noHpPenerima ?: "-"
        view.findViewById<TextView>(R.id.tvAlamat).text = alamat?.alamatRumah ?: "-"
        view.findViewById<TextView>(R.id.tvDetailLain).text = alamat?.detailLain ?: "-"
    }
}
