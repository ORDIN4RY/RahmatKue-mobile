package ordinary.rahmatbakery.util

import android.content.Context

class PrefManager(context: Context) {
    private val pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)

    fun saveLogin(username: String, token: String) {
        with(pref.edit()) {
            putBoolean("isLoggedIn", true)
            putString("username", username)
            putString("token", token)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = pref.getBoolean("isLoggedIn", false)

    fun getUsername(): String? = pref.getString("username", null)

    fun logout() {
        with(pref.edit()) {
            clear()
            apply()
        }
    }
}
