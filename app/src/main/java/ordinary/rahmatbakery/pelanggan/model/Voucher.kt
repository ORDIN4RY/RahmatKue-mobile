package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ordinary.rahmatbakery.model.Profile

@Parcelize
@Serializable
data class Voucher(
    @SerialName ("id_voucher")
    val id_voucher: String,
    @SerialName ("nama_voucher")
    val nama_voucher: String,
    @SerialName("tgl_mulai")
    val tgl_mulai: String,
    @SerialName ("tgl_berakhir")
    val tgl_berakhir: String,
    @SerialName("deskripsi")
    val deskripsi: String? = null,
    @SerialName("poin_tukar")
    val poin_tukar: Int? = 0 ,
    @SerialName("foto")
    val foto_voucher: String? = null,
    @SerialName("minimal_pembelian")
    val minimal_pembelian: Int? = 0 ,
    @SerialName("jenis_voucher")
    val jenis_voucher: String? = null,
    @SerialName("persentase_potongan")
    val persentase_potongan:Int?=0,
    @SerialName("maksimal_potongan")
    val maksimal_potongan:Int?=0,
    @SerialName("kategori")
    val kategoriList: List<String> = emptyList(),
    @SerialName("is_active")
    val isActive : Boolean = true,
    @Transient var alasanTidakBisa: String = "" // Untuk menyimpan alasan voucher tidak bisa dipakai
): Parcelable
