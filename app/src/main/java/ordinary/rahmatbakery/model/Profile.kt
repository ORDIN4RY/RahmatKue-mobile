package ordinary.rahmatbakery.model

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import ordinary.rahmatbakery.api.SupabaseManager

@kotlinx.serialization.Serializable
data class Profile(
    val id: String,
    val username: String? = "pengguna",
    val level: String? = "pelanggan",
    val point : Int?=0,
    @SerialName("created_at")
    val createdAt: String?
)

suspend fun getProfile(): Profile? {
    val user = SupabaseManager.client.auth.currentUserOrNull() ?: return null

    return SupabaseManager.client
        .postgrest["profiles"]
        .select{
            filter {
                eq("id", user.id)
            }
        }
        .decodeSingleOrNull<Profile>()
}

