package ordinary.rahmatbakery.admin.activity

import ordinary.rahmatbakery.admin.activity.OrderStatus.*

object DataDummy{

    // --- 1. Order Data ---
    private val dummyOrders = listOf(
        // 15 Completed Orders
        Order(1, "10/10/25", "10/10/25", "Ahmad", 2, 50000, COMPLETED),
        Order(2, "10/10/25", "10/10/25", "Budi", 1, 25000, COMPLETED),
        Order(3, "11/10/25", "11/10/25", "Citra", 5, 125000, COMPLETED),
        Order(4, "11/10/25", "11/10/25", "Dewi", 1, 25000, COMPLETED),
        Order(5, "12/10/25", "12/10/25", "Eko", 3, 75000, COMPLETED),
        Order(6, "12/10/25", "12/10/25", "Fani", 2, 50000, COMPLETED),
        Order(7, "13/10/25", "13/10/25", "Gita", 1, 25000, COMPLETED),
        Order(8, "13/10/25", "13/10/25", "Hadi", 4, 100000, COMPLETED),
        Order(9, "14/10/25", "14/10/25", "Irma", 2, 50000, COMPLETED),
        Order(10, "14/10/25", "14/10/25", "Joko", 1, 25000, COMPLETED),
        Order(11, "15/10/25", "15/10/25", "Kiki", 3, 75000, COMPLETED),
        Order(12, "15/10/25", "15/10/25", "Lina", 2, 50000, COMPLETED),
        Order(13, "16/10/25", "16/10/25", "Mira", 1, 25000, COMPLETED),
        Order(14, "16/10/25", "16/10/25", "Nia", 4, 100000, COMPLETED),
        Order(15, "17/10/25", "17/10/25", "Oki", 2, 50000, COMPLETED),

        // 3 In Progress Orders
        Order(16, "18/10/25", "", "Panca", 1, 25000, IN_PROGRESS),
        Order(17, "18/10/25", "", "Qori", 2, 50000, IN_PROGRESS),
        Order(18, "19/10/25", "", "Rina", 1, 25000, IN_PROGRESS),

        // 2 Cancelled Orders
        Order(19, "19/10/25", "19/10/25", "Sari", 3, 75000, CANCELLED),
        Order(20, "20/10/25", "20/10/25", "Tono", 1, 25000, CANCELLED)
    )

    // --- 2. Product Data ---
    private val dummyProducts = listOf(
        Product(1, "Kue AMD", "url_kue_amd_1", 40),
        Product(2, "Kue AMD", "url_kue_amd_2", 30),
        Product(3, "Kue Coklat", "url_kue_coklat", 15),
        Product(4, "Roti Manis", "url_roti_manis", 10)
    )

    // --- Dashboard Summary Functions ---

    fun getTotalOrders(): Int {
        return dummyOrders.size // Should be 20
    }

    fun getTotalRevenue(): Long {
        // Calculate revenue from COMPLETED orders only
        return dummyOrders
            .filter { it.status == COMPLETED }
            .sumOf { it.price } // Should sum up to 1,000,000
    }

    fun getOrderStatusCounts(): Map<OrderStatus, Int> {
        return dummyOrders.groupingBy { it.status }.eachCount()
    }

    fun getLatestOrders(limit: Int = 5): List<Order> {
        // Return the most recent orders (highest ID first)
        return dummyOrders.sortedByDescending { it.id }.take(limit)
    }

    fun getBestSellingProducts(limit: Int = 2): List<Product> {
        // Return products sorted by sales count
        return dummyProducts.sortedByDescending { it.salesCount }.take(limit)
    }
}
