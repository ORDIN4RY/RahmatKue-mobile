package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class Produk (
    val id: String,
    val nama: String,
    val deskripsi: String,
    val gambar: String,
    val harga: Int
)