package ordinary.rahmatbakery.pelanggan.model
import kotlinx.serialization.Serializable

//@Serializable
//data class Keranjang (
//    val keranjangId: String,
//    val produk : Produk,
//    var selected : Boolean = false,
//    var jumlah : Int = 15
//)

@Serializable
data class Keranjang(
    val id: String,
    var jumlah: Int,
    val tipe: String?= null, // "produk" atau "paket"
    val produk: Produk? = null,
    val paket: Paket? = null,
    @Transient var terpilih: Boolean = false
)
