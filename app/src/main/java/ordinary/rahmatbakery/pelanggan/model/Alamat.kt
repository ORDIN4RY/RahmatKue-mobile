package ordinary.rahmatbakery.pelanggan.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Alamat(
    @SerialName("id_alamat")
    val id: String? = "",

    @SerialName("id_user")
    val idUser: String,

    @SerialName("nama_lengkap")
    val nama: String,

    @SerialName("no_hp_penerima")
    val noHp: String,

    @SerialName("detail_lain")
    val detail: String? = "",

    @SerialName("alamat_rumah")
    val alamat: String,

    @SerialName("alamat_utama")
    val isUtama: Boolean,

    val latitude: Double? = 0.0,

    val longitude: Double? = 0.0,

    @SerialName("kecamatan")
    val kecamatan: String? = "",

    @SerialName("kabupaten")
    val kabupaten: String? = "",

    @SerialName("provinsi")
    val provinsi: String? = ""
) : Parcelable