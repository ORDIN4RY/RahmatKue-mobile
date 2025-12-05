package ordinary.rahmatbakery.admin.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ordinary.rahmatbakery.R
import ordinary.rahmatbakery.adapter.OrderProductAdapter
import ordinary.rahmatbakery.admin.activity.OrderDetailActivity
import ordinary.rahmatbakery.model.OrderAdmin
import ordinary.rahmatbakery.pelanggan.model.Pesanan
import java.text.NumberFormat
import java.util.Locale

class OrderProductsFragment : Fragment() {

    private lateinit var order: OrderAdmin

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_order_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        order = (activity as? OrderDetailActivity)?.order ?: return

        val rvProducts = view.findViewById<RecyclerView>(R.id.rvProducts)
        rvProducts.layoutManager = LinearLayoutManager(requireContext())

        val products = order.detailProduk ?: emptyList()
        val packages = order.detailPaket ?: emptyList()

        view.findViewById<TextView>(R.id.tvTotalHarga).text = formatRupiah(order.totalHarga)
        view.findViewById<TextView>(R.id.tvPotongan).text = formatRupiah(order.potongan)
        view.findViewById<TextView>(R.id.tvOngkir).text = formatRupiah(order.ongkir)
        view.findViewById<TextView>(R.id.tvDpMinimal).text = formatRupiah(order.dpMinimal)
        val kekuranganBayar = order.totalHarga - order.dpMinimal
        view.findViewById<TextView>(R.id.tvKekuranganBayar).text = formatRupiah(kekuranganBayar)

//        if (order.ongkir > 0){
//            view.findViewById<TextView>(R.id.tvTotalHarga).text = formatRupiah(hargaSetelahOngkir)
//
//        }
//        if (order.potongan > 0){
//            view.findViewById<TextView>(R.id.tvTotalHarga).text = formatRupiah(hargaSetelahOngkirdanPotongan)
//        }
        view.findViewById<TextView>(R.id.tvTotalHarga).text = formatRupiah(order.totalHarga)
        view.findViewById<TextView>(R.id.tvPotongan).text = formatRupiah(order.potongan)
        if(order.dpMinimal==order.totalHarga) {
            view.findViewById<TextView>(R.id.tvDpMinimal).text = "Rp 0,00"
        }

        rvProducts.adapter = OrderProductAdapter(products, packages)
    }

}

private fun formatRupiah(amount: Int): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}

