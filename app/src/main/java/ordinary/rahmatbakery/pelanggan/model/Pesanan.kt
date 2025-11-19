package ordinary.rahmatbakery.pelanggan.model

data class Pesanan(
    val date: String,
    val totalAmount: Double,
    val status: String,
    // Tambahkan daftar item pesanan di sini
    val items: List<PesananItem>
)
data class PesananItem(
    val name: String,
    val price: Double,
    val quantity: Int,
    val description: String
)
