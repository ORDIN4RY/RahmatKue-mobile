package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class Kategori (
    val kategoriId: String,
    val kategoriName: String
)