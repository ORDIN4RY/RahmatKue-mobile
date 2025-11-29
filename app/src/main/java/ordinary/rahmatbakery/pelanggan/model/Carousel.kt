package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class Carousel(
    val id: Int,
    val image_url: String,
    val is_active: Boolean = true
)
