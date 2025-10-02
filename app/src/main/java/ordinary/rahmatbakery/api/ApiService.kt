package ordinary.rahmatbakery.api

import ordinary.rahmatbakery.model.LoginRequest
import ordinary.rahmatbakery.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}