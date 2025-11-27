package ordinary.rahmatbakery.pelanggan.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- PERUBAHAN UTAMA DI SINI ---
// Tipe data ID diubah dari Int menjadi String untuk menampung UUID.
// Tipe data harga/subtotal diubah dari Double menjadi Int agar cocok dengan database.

@Serializable
@Parcelize
data class Pesanan(
    @SerialName("id_transaksi")
    val idTransaksi: String, // Dulu Int, sekarang String (untuk UUID)

    @SerialName("total_harga")
    val totalHarga: Int, // Dulu Double, sekarang Int

    @SerialName("status")
    val status: String,

    @SerialName("created_at")
    val createdAt: String,

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
    val subtotal: Int, // Dulu Double, sekarang Int
    @SerialName("produk")
    val produk: Produk2
) : Parcelable

@Parcelize
@Serializable
data class PesananPaketItems(
    @SerialName("jumlah")
    val jumlah: Int,
    @SerialName("subtotal")
    val subtotal: Int, // Dulu Double, sekarang Int
    @SerialName("paket")
    val paket: ProdukPaket
) : Parcelable

@Serializable
@Parcelize
data class Produk2(
    @SerialName("id_produk")
    val idProduk: String, // Dulu Int, sekarang String (untuk UUID)
    @SerialName("nama_produk")
    val namaProduk: String,
    @SerialName("harga")
    val harga: Int, // Dulu Double, sekarang Int
    @SerialName("foto_produk")
    val fotoProduk: String? = null
) : Parcelable

@Serializable
@Parcelize
data class ProdukPaket(
    @SerialName("id_paket")
    val idPaket: String, // Dulu Int, sekarang String (untuk UUID)
    @SerialName("nama_paket")
    val namaPaket: String,
    @SerialName("harga_paket")
    val harga: Int, // Dulu Double, sekarang Int
    @SerialName("foto_paket")
    val fotoPaket: String? = null
) : Parcelable

// Wrapper class ini tidak perlu diubah, tapi tipe datanya harus cocok
data class TampilanItemPesanan(
    val nama: String,
    val jumlah: Int,
    val subtotal: Int // Dulu Double, sekarang Int
)
