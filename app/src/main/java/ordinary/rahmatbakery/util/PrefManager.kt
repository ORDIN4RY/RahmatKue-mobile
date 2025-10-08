package ordinary.rahmatbakery.util

import android.content.Context

class PrefManager(context: Context) {
    private val pref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE)

    fun saveLogin(username: String, userId : Int) {
        with(pref.edit()) {
            putBoolean("isLoggedIn", true)
            putInt("user_id", userId)
            putString("username", username)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = pref.getBoolean("isLoggedIn", false)

    fun getUsername(): String? = pref.getString("username", null)

    fun getId(): Int? = pref.getInt("user_id",0 )

    fun logout() {
        with(pref.edit()) {
            clear()
            apply()
        }
    }
}
