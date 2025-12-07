package ordinary.rahmatbakery.pelanggan.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Contextual
import kotlinx.serialization.Transient

@Serializable
@Parcelize
data class Keranjang(
    val id: String,
    var jumlah: Int,
    val tipe: String?= null, // "produk" atau "paket"
    val produk: Produk? = null,
    val paket: Paket? = null,
    @Transient var terpilih: Boolean = false
) : Parcelable

@Serializable
data class KeranjangInsert(
    @SerialName("id_user") val idUser: String?,
    @SerialName("id_produk") val idProduk: String? = null,
    @SerialName("id_paket") val idPaket: String? = null,
    @SerialName("jumlah") val jumlah: Int?= 0
)