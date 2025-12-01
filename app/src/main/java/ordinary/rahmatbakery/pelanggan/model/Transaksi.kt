package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Model untuk INSERT ke tabel 'transaksi'
@Serializable
data class TransaksiInsert(
    @SerialName("id_user")
    val idUser: String,
    @SerialName("id_alamat")
    val idAlamat: String,
    @SerialName("total_harga")
    val totalHarga: Int,
    @SerialName("dp_minimal")
    val dpMinimal: Int?= 0,
    @SerialName("status")
    val status: String,
    @SerialName("id_voucher")
    val idVoucher: String?=null,
    @SerialName("waktu_selesai")
    val waktuSelesai: String,
    @SerialName("metode_pengambilan")
    val metodePengiriman: String?="diambil",
    @SerialName("catatan")
    val catatan: String?=null,
    val potongan: Int?=0,
    val ongkir: Int?=0
)

// Model untuk INSERT ke tabel 'detail_transaksi_produk'
@Serializable
data class DetailTransaksiProdukInsert(
    @SerialName("id_transaksi")
    val idTransaksi: String,
    @SerialName("id_produk")
    val idProduk: String,
    val jumlah: Int,
    val subtotal: Int
)

// Model untuk INSERT ke tabel 'detail_transaksi_paket'
@Serializable
data class DetailTransaksiPaketInsert(
    @SerialName("id_transaksi")
    val idTransaksi: String,
    @SerialName("id_paket")
    val idPaket: String,
    val jumlah: Int,
    val subtotal: Int
)