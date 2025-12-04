package ordinary.rahmatbakery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    @SerialName("id_pembayaran")
    val idPembayaran: String,

    @SerialName("id_transaksi")
    val idTransaksi: String? = null,

    @SerialName("nominal")
    val nominal: Int = 0,

    @SerialName("metode")
    val metode: String? = null,

    @SerialName("tgl_pembayaran")
    val tglPembayaran: String? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("invoice_url")
    val invoiceUrl: String? = null
)

@Serializable
data class Address(
    @SerialName("id_alamat")
    val idAlamat: String,

    @SerialName("id_user")
    val idUser: String? = null,

    @SerialName("nama_lengkap")
    val namaLengkap: String? = null,

    @SerialName("no_hp_penerima")
    val noHpPenerima: String? = null,

    @SerialName("alamat_rumah")
    val alamatRumah: String? = null,

    @SerialName("detail_lain")
    val detailLain: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("alamat_utama")
    val alamatUtama: Boolean = false
)

@Serializable
data class Cancellation(
    @SerialName("id_batal")
    val idBatal: String,

    @SerialName("id_transaksi")
    val idTransaksi: String? = null,

    @SerialName("alasan")
    val alasan: String? = null,

    @SerialName("tipe")
    val tipe: String? = null,

    @SerialName("status")
    val status: String = "Menunggu Konfirmasi",

    @SerialName("dibuat_pada")
    val dibuatPada: String? = null,

    @SerialName("dikonfirmasi_pada")
    val dikonfirmasiPada: String? = null,

    @SerialName("dikonfirmasi_oleh")
    val dikonfirmasiOleh: String? = null
)