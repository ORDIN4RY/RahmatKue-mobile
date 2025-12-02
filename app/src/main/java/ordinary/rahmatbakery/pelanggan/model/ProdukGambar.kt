package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class ProdukGambar(
    val gambar: String,
    val created_at: String
)
