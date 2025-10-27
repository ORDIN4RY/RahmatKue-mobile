package ordinary.rahmatbakery.util

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import ordinary.rahmatbakery.api.SupabaseManager
import ordinary.rahmatbakery.model.Profile

class AuthRepository {

    val supabase = SupabaseManager.client

    suspend fun login(email: String, password: String): Profile? {

        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = supabase.auth.currentUserOrNull()
            ?: throw Exception("User tidak ditemukan")

        return supabase
            .postgrest["profiles"]
            .select {
                filter{
                    eq("id", user.id)
                }
            }
            .decodeSingle<Profile>()
    }

    suspend fun getCurrentProfile(): Profile? {
        val session = supabase.auth.currentSessionOrNull() ?: return null
        val userId = session.user?.id ?: return null

        return supabase
            .postgrest["profiles"]
            .select {
                filter{
                    eq("id", userId)
                }
            }
            .decodeSingle<Profile>()
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }
}

