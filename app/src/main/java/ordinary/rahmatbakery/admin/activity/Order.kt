package ordinary.rahmatbakery.admin.activity

data class Order(
    val id: Int,
    val dateOrdered: String,
    val dateCompleted: String,
    val customerName: String,
    val quantity: Int,
    val price: Long, // Price in Rupiah (e.g., 50000 for 50k)
    val status: OrderStatus
)

enum class OrderStatus {
    COMPLETED,
    IN_PROGRESS,
    CANCELLED
}