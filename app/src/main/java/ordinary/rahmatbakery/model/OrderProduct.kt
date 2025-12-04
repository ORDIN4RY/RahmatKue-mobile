package ordinary.rahmatbakery.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderProduct(
    @SerialName("id_transaksi")
    val idTransaksi: String,

    @SerialName("id_produk")
    val idProduk: String? = null,

    @SerialName("jumlah")
    val jumlah: Int = 0,

    @SerialName("subtotal")
    val subtotal: Int = 0,

    @SerialName("produk")
    val produk: Product? = null
)

@Serializable
data class Product(
    @SerialName("id_produk")
    val idProduk: String,

    @SerialName("nama_produk")
    val namaProduk: String? = null,

    @SerialName("deskripsi")
    val deskripsi: String? = null,

    @SerialName("foto_produk")
    val fotoProduk: String? = null,

    @SerialName("harga")
    val harga: Int = 0,

    @SerialName("varian")
    val varian: String? = null,

    @SerialName("diskon")
    val diskon: Int = 0
)

@Serializable
data class OrderPackage(
    @SerialName("id_transaksi")
    val idTransaksi: String,

    @SerialName("id_paket")
    val idPaket: String? = null,

    @SerialName("jumlah")
    val jumlah: Int = 0,

    @SerialName("subtotal")
    val subtotal: Int = 0,

    @SerialName("paket")
    val paket: Package? = null
)

@Serializable
data class Package(
    @SerialName("id_paket")
    val idPaket: String,

    @SerialName("nama_paket")
    val namaPaket: String? = null,

    @SerialName("deskripsi")
    val deskripsi: String? = null,

    @SerialName("foto_paket")
    val fotoPaket: String? = null,

    @SerialName("harga_paket")
    val hargaPaket: Int = 0,

    @SerialName("diskon")
    val diskon: Int = 0
)