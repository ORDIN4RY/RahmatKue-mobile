package ordinary.rahmatbakery.admin.activity.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Transaksi(
    val total_harga: Long,
    val status: String
)

@Serializable
data class TransaksiLatest(
    val id_transaksi: String,
    val created_at: String,
    val total_harga: Long
)

@Serializable
data class ProdukTerlaris(
    val id_produk: String,
    val nama_produk: String,
    val total_terjual: Int?=null
)
