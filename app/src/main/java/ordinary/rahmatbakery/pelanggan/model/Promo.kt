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

    @SerialName("foto_banner")
    val fotoBanner: String?=null,
    @SerialName("foto_square")
    val fotoSquare: String?=null,
    @SerialName("nama")
    val nama: String?=null,
    @SerialName("deskripsi")
    val deskripsi: String??=null,
    @SerialName("tipe_diskon")
    val tipeDiskon: String?=null, // misal: "persen" atau "potongan_harga"
    @SerialName("nilai_diskon")
    val nilaiDiskon: Int?=0


) : Parcelable
