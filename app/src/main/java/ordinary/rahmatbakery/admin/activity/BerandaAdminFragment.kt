package ordinary.rahmatbakery.admin.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import ordinary.rahmatbakery.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

/**
 * A simple [Fragment] subclass for the Dashboard screen.
 * This fragment implements the UI based on the provided image, including a placeholder
 * for the Pie Chart using the MPAndroidChart library.
 */
class BerandaAdminFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_beranda_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart(view)
    }

    /**
     * Sets up the Pie Chart using dummy data.
     * NOTE: This requires the MPAndroidChart dependency.
     */
    private fun setupPieChart(view: View) {
        // Create a new PieChart instance
        val pieChart = PieChart(requireContext())
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.isDrawHoleEnabled = false // Make it a solid pie chart

        // Dummy Data based on the image: Selesai: 15, Proses: 3, Dibatalkan: 2
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(15f, "Selesai"))
        entries.add(PieEntry(3f, "Proses"))
        entries.add(PieEntry(2f, "Dibatalkan"))

        val dataSet = PieDataSet(entries, "Order Status")

        // Colors based on the image (light blue, orange, and a third color for the remaining slice)
        val colors = listOf(
            Color.rgb(173, 216, 230), // Light Blue (similar to image)
            Color.rgb(255, 165, 0),   // Orange (similar to image)
            Color.rgb(139, 69, 19)    // Brown (for the third slice)
        )
        dataSet.colors = colors
        dataSet.setDrawValues(false) // Do not draw values on the slices

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refresh chart

        // Add the PieChart to the chart_container FrameLayout
        val chartContainer = view.findViewById<FrameLayout>(R.id.chart_container)
        chartContainer.removeAllViews() // Clear placeholder background
        chartContainer.addView(pieChart, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment.
         */
        @JvmStatic
        fun newInstance() = BerandaAdminFragment()
    }
}
