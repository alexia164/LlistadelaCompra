package cat.insbaixcamp.llistadelacompra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.insbaixcamp.llistadelacompra.data.ListRepository
import cat.insbaixcamp.llistadelacompra.model.ShoppingList
import cat.insbaixcamp.llistadelacompra.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListViewModel(private val repository: ListRepository) : ViewModel() {

    private val _myLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val myLists: StateFlow<List<ShoppingList>> = _myLists.asStateFlow()

    private val _sharedLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val sharedLists: StateFlow<List<ShoppingList>> = _sharedLists.asStateFlow()

    fun observeLists(user: User) {
        viewModelScope.launch {
            repository.getListsForUserFlow(user.id, user.email).collect { allLists ->
                _myLists.value = allLists.filter {
                    it.ownerId == user.id || (user.email.isNotEmpty() && it.ownerId.equals(user.email, ignoreCase = true))
                }
                _sharedLists.value = allLists.filter {
                    it.ownerId != user.id && (user.email.isEmpty() || !it.ownerId.equals(user.email, ignoreCase = true))
                }
            }
        }
    }

    fun createList(title: String, ownerId: String): ShoppingList? {
        if (title.isBlank()) return null
        return repository.createList(title, ownerId)
    }

    fun renameList(listId: String, newTitle: String) {
        if (newTitle.isNotBlank()) {
            repository.updateListTitle(listId, newTitle)
        }
    }

    fun deleteList(listId: String) {
        repository.deleteList(listId)
    }
}
