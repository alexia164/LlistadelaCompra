package cat.insbaixcamp.llistadelacompra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.insbaixcamp.llistadelacompra.data.ListRepository
import cat.insbaixcamp.llistadelacompra.model.Category
import cat.insbaixcamp.llistadelacompra.model.PredefinedProduct
import cat.insbaixcamp.llistadelacompra.model.Product
import cat.insbaixcamp.llistadelacompra.model.ShoppingList
import cat.insbaixcamp.llistadelacompra.util.AutoCategoryClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val listId: String,
    private val repository: ListRepository
) : ViewModel() {

    private val _shoppingList = MutableStateFlow<ShoppingList?>(null)
    val shoppingList: StateFlow<ShoppingList?> = _shoppingList.asStateFlow()

    init {
        observeList()
    }

    private fun observeList() {
        viewModelScope.launch {
            repository.getListByIdFlow(listId).collect {
                _shoppingList.value = it
            }
        }
    }

    fun shareListWithUser(userIdentifier: String): Boolean {
        return repository.shareListWithUser(listId, userIdentifier)
    }

    fun removeMember(userId: String) {
        repository.removeMemberFromList(listId, userId)
    }

    fun addCategory(name: String, iconEmoji: String = "📁"): Category? {
        if (name.isBlank()) return null
        return repository.addCategory(listId, name, iconEmoji)
    }

    fun updateCategory(categoryId: String, newName: String) {
        if (newName.isNotBlank()) {
            repository.updateCategory(listId, categoryId, newName)
        }
    }

    fun deleteCategory(categoryId: String) {
        repository.deleteCategory(listId, categoryId)
    }

    fun addProduct(
        productName: String,
        quantity: String,
        count: Int = 1,
        explicitCategoryId: String? = null
    ) {
        val trimmedName = productName.trim()
        if (trimmedName.isEmpty()) return

        val currentList = _shoppingList.value ?: return

        val targetCatId: String
        val targetEmoji: String

        if (!explicitCategoryId.isNullOrEmpty()) {
            targetCatId = explicitCategoryId
            targetEmoji = "🛒"
        } else {
            val suggestion = AutoCategoryClassifier.classify(trimmedName)
            val existingCategory = currentList.categories.find {
                it.name.equals(suggestion.categoryName, ignoreCase = true)
            }

            if (existingCategory != null) {
                targetCatId = existingCategory.id
            } else {
                val newCat = repository.addCategory(listId, suggestion.categoryName, suggestion.emoji)
                targetCatId = newCat.id
            }
            targetEmoji = suggestion.emoji
        }

        repository.addProduct(
            listId = listId,
            name = trimmedName,
            categoryId = targetCatId,
            quantity = quantity.ifBlank { "1" },
            iconEmoji = targetEmoji,
            count = count
        )
    }

    fun addPredefinedProduct(predefined: PredefinedProduct) {
        val currentList = _shoppingList.value ?: return
        val existingCat = currentList.categories.find {
            it.name.equals(predefined.defaultCategoryName, ignoreCase = true)
        }
        val targetCatId = existingCat?.id ?: repository.addCategory(listId, predefined.defaultCategoryName, predefined.emoji).id

        repository.addProduct(
            listId = listId,
            name = predefined.name,
            categoryId = targetCatId,
            quantity = "1",
            iconEmoji = predefined.emoji,
            count = 1
        )
    }

    fun toggleProductPurchased(productId: String) {
        repository.toggleProductPurchased(listId, productId)
    }

    fun updateProduct(product: Product) {
        if (product.name.isNotBlank()) {
            repository.updateProduct(listId, product)
        }
    }

    fun deleteProduct(productId: String) {
        repository.deleteProduct(listId, productId)
    }
}
