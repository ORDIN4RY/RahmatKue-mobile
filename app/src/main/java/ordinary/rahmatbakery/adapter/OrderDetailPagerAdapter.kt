package ordinary.rahmatbakery.adapter


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ordinary.rahmatbakery.admin.*
import ordinary.rahmatbakery.admin.fragment.OrderAddressFragment
import ordinary.rahmatbakery.admin.fragment.OrderCancellationFragment
import ordinary.rahmatbakery.admin.fragment.OrderInfoFragment
//import ordinary.rahmatbakery.admin.fragment.OrderPaymentFragment
import ordinary.rahmatbakery.admin.fragment.OrderProductsFragment

class OrderDetailPagerAdapter(
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OrderInfoFragment()
            1 -> OrderProductsFragment()
            2 -> OrderAddressFragment()
            3 -> OrderCancellationFragment()
            else -> OrderInfoFragment()
        }
    }
}