package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Paket(
    val id : String ?=null,
    val nama: String,
    val deskripsi: String,
    val foto: String = "foto.png",
    val harga: Int,
    val detail: List<Detail> = emptyList(),
    val tipe_diskon: String? = null,
    val diskon: Int? = 0,

) : Parcelable


@Parcelize
@Serializable
data class Detail(
    val produk: Produk
) : Parcelable
