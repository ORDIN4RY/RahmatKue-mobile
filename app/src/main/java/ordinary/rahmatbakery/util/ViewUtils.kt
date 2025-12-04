package ordinary.rahmatbakery.util

import android.view.View
import android.widget.ListView

fun setListViewHeightBasedOnChildren(listView: ListView) {
    val adapter = listView.adapter ?: return

    var totalHeight = 0
    for (i in 0 until adapter.count) {
        val listItem = adapter.getView(i, null, listView)
        listItem.measure(
            View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        totalHeight += listItem.measuredHeight
    }

    val params = listView.layoutParams
    params.height = totalHeight + (listView.dividerHeight * (adapter.count - 1))
    listView.layoutParams = params
    listView.requestLayout()
}
