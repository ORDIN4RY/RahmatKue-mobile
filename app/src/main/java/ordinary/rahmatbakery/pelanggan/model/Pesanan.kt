package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@Parcelize
data class Pesanan(
    @SerialName("id_transaksi")
    val idTransaksi: String,

    @SerialName("total_harga")
    val totalHarga: Int,

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String,
    @SerialName("metode_pengambilan")
    val metodePengambilan: String? = null,
    @SerialName("waktu_selesai")
    val tglPesananJadi: String,
    @SerialName("catatan")
    val catatan: String? = null,
    @SerialName("ongkir")
    val ongkir: Int? = null,
    @SerialName("nomor_pesanan")
    val nomorPesanan: String,
    @SerialName("dp_minimal")
    val DP: Int? = null,
 @SerialName("potongan")
    val potonganHarga: Int? = null,


    @SerialName("detail_transaksi_produk")
    val items: List<PesananItems> = emptyList(),

    @SerialName("detail_transaksi_paket")
    val paketItems: List<PesananPaketItems> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class PesananItems(
    @SerialName("jumlah")
    val jumlah: Int,
    @SerialName("subtotal")
    val subtotal: Int,
    @SerialName("produk")
    val produk: Produk2
) : Parcelable

@Parcelize
@Serializable
data class PesananPaketItems(
    @SerialName("jumlah")
    val jumlah: Int,
    @SerialName("subtotal")
    val subtotal: Int,
    @SerialName("paket")
    val paket: ProdukPaket
) : Parcelable

@Serializable
@Parcelize
data class Produk2(
    @SerialName("id_produk")
    val idProduk: String,
    @SerialName("nama_produk")
    val namaProduk: String,
    @SerialName("harga")
    val harga: Int,
    @SerialName("foto_produk")
    val fotoProduk: String
) : Parcelable

@Serializable
@Parcelize
data class ProdukPaket(
    @SerialName("id_paket")
    val idPaket: String,
    @SerialName("nama_paket")
    val namaPaket: String,
    @SerialName("harga_paket")
    val hargaPaket: Int,
    @SerialName("foto_paket")
    val fotoPaket: String? = null
) : Parcelable

@Serializable
@Parcelize
data class TampilanItemPesanan(
    val nama: String,
    val jumlah: Int,
    val subtotal: Int,
    val foto : String?,
    val hargaSatuan : Int

): Parcelable
