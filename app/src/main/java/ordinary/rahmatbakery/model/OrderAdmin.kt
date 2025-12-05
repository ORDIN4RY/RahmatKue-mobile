package ordinary.rahmatbakery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderAdmin(
    @SerialName("id_transaksi")
    val idTransaksi: String,

    @SerialName("id_user")
    val idUser: String? = null,

    @SerialName("id_alamat")
    val idAlamat: String? = null,

    @SerialName("nomor_pesanan")
    val nomorPesanan: String? = null,

    @SerialName("total_harga")
    val totalHarga: Int = 0,

    @SerialName("dp_minimal")
    val dpMinimal: Int = 0,

    @SerialName("potongan")
    val potongan: Int = 0,

    @SerialName("ongkir")
    val ongkir: Int = 0,

    @SerialName("status")
    val status: String = "Menunggu Pembayaran",

    @SerialName("metode_pengambilan")
    val metodePengambilan: String = "diambil",

    @SerialName("catatan")
    val catatan: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("waktu_selesai")
    val waktuSelesai: String? = null,

    @SerialName("id_voucher")
    val idVoucher: String? = null,

    // Relasi
    @SerialName("profiles")
    val profile: Profile? = null,

    @SerialName("alamat")
    val alamat: Address? = null,

    @SerialName("pembayaran")
    val pembayaran: List<Payment>? = null,

    @SerialName("detail_transaksi_produk")
    val detailProduk: List<OrderProduct>? = null,

    @SerialName("detail_transaksi_paket")
    val detailPaket: List<OrderPackage>? = null,

    @SerialName("batal")
    val pembatalan: List<Cancellation>? = null
)

