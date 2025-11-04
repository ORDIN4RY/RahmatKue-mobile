package ordinary.rahmatbakery.pelanggan.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Alamat (
    @SerialName("id_alamat")
    val id: String ?= "",

    @SerialName("id_user")
    val idUser: String,

    @SerialName("nama_lengkap")
    val nama: String,

    @SerialName("no_hp_penerima")
    val noHp: String,

    @SerialName("alamat_rumah")
    val alamat: String,

    @SerialName("alamat_utama")
    val isUtama: Boolean,

) : Parcelable