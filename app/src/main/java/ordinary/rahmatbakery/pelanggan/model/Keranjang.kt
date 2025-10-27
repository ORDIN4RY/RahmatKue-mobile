package ordinary.rahmatbakery.pelanggan.model
import kotlinx.serialization.Serializable

@Serializable
data class Keranjang (
    val keranjangId: String,
    val produk: MenuProduk
)