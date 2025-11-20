package ordinary.rahmatbakery.pelanggan.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Transient

@Serializable
data class Keranjang(
    val id: String,
    var jumlah: Int,
    val tipe: String?= null, // "produk" atau "paket"
    val produk: Produk? = null,
    val paket: Paket? = null,
    @Transient var terpilih: Boolean = false
)

@Serializable
data class KeranjangInsert(
    @SerialName("id_user")
    @Contextual
    val idUser: String?,

    @SerialName("id_produk")
    @Contextual
    val idProduk: String? = null, // default null

    @SerialName("id_paket")
    @Contextual
    val idPaket: String? = null, // default null

    @SerialName("jumlah")
    val jumlah: Int
)