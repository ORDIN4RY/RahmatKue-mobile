package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Produk (
    val id: String,
    val nama: String,
    val deskripsi: String,
    val gambar: String,
    val harga: Int
): Parcelable