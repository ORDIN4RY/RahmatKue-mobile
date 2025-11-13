package ordinary.rahmatbakery.model

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Profile(
    val id: String,
    val username: String? = "pengguna",
    val level: String? = "pelanggan",
    val point : Int?=0,
    @SerialName("created_at")
    val createdAt: String?
)

