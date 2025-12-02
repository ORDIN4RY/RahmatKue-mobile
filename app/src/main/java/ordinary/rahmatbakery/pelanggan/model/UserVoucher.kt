package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ordinary.rahmatbakery.model.Profile

@Parcelize
@Serializable
data class UserVoucher(
    @SerialName("id_user_voucher")
    val id_user_voucher: String,
    @SerialName("id_voucher")
    val id_voucher: String,
    @SerialName ("id_user")
    val id_user: String,
    @SerialName ("status")
    val status: String, // "belum_digunakan" / "sudah_digunakan" / "kadaluarsa"
    @SerialName ("voucher")
    val voucher: Voucher, // hasil join "voucher(*)"

): Parcelable
