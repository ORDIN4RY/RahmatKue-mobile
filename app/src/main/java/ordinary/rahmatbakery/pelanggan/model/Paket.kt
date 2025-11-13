package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Paket(
    val id : String,
    val nama: String,
    val deskripsi: String,
    val foto: String,
    val harga: Int,
    val detail: List<Detail> = emptyList()
) : Parcelable


@Parcelize
@Serializable
data class Detail(
    val produk: Produk,
    val jumlah: Int
) : Parcelable
