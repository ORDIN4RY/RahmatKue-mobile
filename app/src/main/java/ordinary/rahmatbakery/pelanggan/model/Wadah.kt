package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
data class Wadah(
    val id: String,
    val nama: String,
    val deskripsi: String,
    val foto: String?= null,
    val kapasitas: Int,
    val harga: Int,
    val varian: String
) : Parcelable


