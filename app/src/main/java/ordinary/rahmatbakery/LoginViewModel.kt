package ordinary.rahmatbakery
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ordinary.rahmatbakery.admin.model.Profile
import ordinary.rahmatbakery.util.AuthRepository

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val profile = repo.login(email, password)
                if (profile != null)
                    _state.value = AuthState.Success(profile)
                else
                    _state.value = AuthState.Error("Login gagal")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Error tidak diketahui")
            }
        }
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val profile: Profile) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
