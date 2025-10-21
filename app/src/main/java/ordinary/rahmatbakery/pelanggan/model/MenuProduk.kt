package ordinary.rahmatbakery.pelanggan.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuProduk (
    val id: String,
    val productName: String,
    val productImg: String,
    val productPrice: Int
)