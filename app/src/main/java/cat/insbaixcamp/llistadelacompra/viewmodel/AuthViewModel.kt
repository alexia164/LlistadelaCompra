package cat.insbaixcamp.llistadelacompra.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.insbaixcamp.llistadelacompra.data.AuthRepository
import cat.insbaixcamp.llistadelacompra.data.ListRepository
import cat.insbaixcamp.llistadelacompra.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val listRepository: ListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(listRepository.getUsers())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUserFlow.collect { user ->
                if (user != null) {
                    listRepository.setCurrentUser(user)
                    listRepository.saveUser(user)
                    _allUsers.value = listRepository.getUsers()
                    _uiState.value = AuthUiState.SignedIn(user)
                } else {
                    _uiState.value = AuthUiState.SignedOut
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signIn(context)
            result.onSuccess { user ->
                listRepository.setCurrentUser(user)
                listRepository.saveUser(user)
                _allUsers.value = listRepository.getUsers()
                _uiState.value = AuthUiState.SignedIn(user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Error al iniciar sessió amb Google.")
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.signOut(context)
            _uiState.value = AuthUiState.SignedOut
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.SignedOut
        }
    }

    fun refreshUsers() {
        _allUsers.value = listRepository.getUsers()
    }
}
