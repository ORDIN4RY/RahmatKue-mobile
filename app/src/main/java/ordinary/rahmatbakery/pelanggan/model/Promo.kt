package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Promo(
    @SerialName("id_promo")
    val id: String,
    val nama: String,
    val deskripsi: String?,
    @SerialName("tipe_diskon")
    val tipeDiskon: String, // misal: "persen" atau "potongan_harga"
    @SerialName("nilai_diskon")
    val nilaiDiskon: Int
) : Parcelable
