package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
class KategoriVoucher (

    @SerialName("id_voucher")
    val idVoucher: String? = null ,
    @SerialName("id_kategori")
    val idKategori: String,
    @SerialName("voucher")
    val voucher: Voucher,
    @SerialName("kategori")
    val kategori: Kategori,
): Parcelable
