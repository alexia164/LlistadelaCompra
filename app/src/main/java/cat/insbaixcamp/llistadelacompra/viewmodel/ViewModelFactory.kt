package cat.insbaixcamp.llistadelacompra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cat.insbaixcamp.llistadelacompra.data.AuthRepository
import cat.insbaixcamp.llistadelacompra.data.ListRepository

class AppViewModelFactory(
    private val authRepository: AuthRepository,
    private val listRepository: ListRepository,
    private val listId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository, listRepository) as T
            }
            modelClass.isAssignableFrom(ListViewModel::class.java) -> {
                ListViewModel(listRepository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                requireNotNull(listId) { "listId must be provided for DetailViewModel" }
                DetailViewModel(listId, listRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
