package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class Kategori (
    val id: String,
    val nama: String
)