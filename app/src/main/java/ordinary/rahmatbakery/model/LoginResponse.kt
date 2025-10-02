package ordinary.rahmatbakery.model

data class LoginResponse(
    val status: String,
    val user_id: Int?,
    val username: String?,
    val token: String?,
    val message: String?
)
