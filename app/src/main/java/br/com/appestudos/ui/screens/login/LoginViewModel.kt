package br.com.appestudos.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.auth.FirebaseAuthManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authManager: FirebaseAuthManager = FirebaseAuthManager()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val user = authManager.loginWithEmail(email, password)
            _uiState.value = if (user != null) {
                LoginUiState(user = user)
            } else {
                LoginUiState(error = "Erro ao entrar. Verifique suas credenciais.")
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            val result = authManager.registerWithEmail(email, password)
            _uiState.value = result.fold(
                onSuccess = { user -> LoginUiState(user = user) },
                onFailure = { e -> LoginUiState(error = e.message ?: "Erro desconhecido") }
            )
        }
    }

    fun logout() {
        authManager.logout()
        _uiState.value = LoginUiState()
    }

    fun getCurrentUser() {
        val user = authManager.getCurrentUser()
        _uiState.value = LoginUiState(user = user)
    }
}
