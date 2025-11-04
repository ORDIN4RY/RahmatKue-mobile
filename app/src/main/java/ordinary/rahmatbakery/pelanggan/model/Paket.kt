package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class Paket(
    val id : String,
    val nama: String,
    val deskripsi: String,
    val foto: String,
    val harga: Int,
    val detail: List<Detail> = emptyList()
)

@Serializable
data class Detail(
    val produk: Produk,
    val jumlah: Int
)
