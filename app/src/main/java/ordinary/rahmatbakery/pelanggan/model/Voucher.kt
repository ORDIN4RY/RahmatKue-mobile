package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Voucher(
    @SerialName ("id_voucher")
    val id_voucher: String,
    @SerialName ("nama_voucher")
    val nama_voucher: String,
    @SerialName ("kode_voucher")
    val kode_voucher: String? = null,
    @SerialName("tgl_mulai")
    val tgl_mulai: String? = null,
    @SerialName ("tgl_berakhir")
    val tgl_berakhir: String? = null,
    @SerialName("deskripsi")
    val deskripsi: String? = null,
    @SerialName("poin_tukar")
    val poin_tukar: Long = 0,
    @SerialName("foto")
    val foto_voucher: String? = null
): Parcelable
