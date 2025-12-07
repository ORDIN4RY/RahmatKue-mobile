package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Kategori (
    val id: String,
    val nama: String,

    @SerialName("minimal_pembelian")
    val minPembelian: Int?= 0
) : Parcelable