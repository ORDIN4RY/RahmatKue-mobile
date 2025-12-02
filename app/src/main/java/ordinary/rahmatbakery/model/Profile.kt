package ordinary.rahmatbakery.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
@Parcelize
@kotlinx.serialization.Serializable
data class Profile(
    val id: String,
    val username: String? = "pengguna",
    val level: String? = "pelanggan",
    @SerialName ("point")
    val point : Int?=0,
    @SerialName("created_at")
    val createdAt: String?,

    @SerialName("is_blocked")
    val isBanned : Boolean = false
): Parcelable

