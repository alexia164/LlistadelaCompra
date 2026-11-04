package cat.insbaixcamp.llistadelacompra.data

import cat.insbaixcamp.llistadelacompra.model.Category
import cat.insbaixcamp.llistadelacompra.model.Product
import cat.insbaixcamp.llistadelacompra.model.ShoppingList
import cat.insbaixcamp.llistadelacompra.model.User
import kotlinx.coroutines.flow.Flow

interface ListRepository {
    fun getUsers(): List<User>
    fun getUsersFlow(): Flow<List<User>>
    fun getCurrentUser(): User?
    fun setCurrentUser(user: User)
    fun saveUser(user: User): User
    fun findUserByNameOrEmail(query: String): User?

    fun getAllListsFlow(): Flow<List<ShoppingList>>
    fun getListsForUserFlow(userId: String, userEmail: String = ""): Flow<List<ShoppingList>>
    fun getListByIdFlow(listId: String): Flow<ShoppingList?>

    fun createList(title: String, ownerId: String): ShoppingList
    fun updateListTitle(listId: String, newTitle: String)
    fun deleteList(listId: String): Boolean
    fun shareListWithUser(listId: String, userIdentifier: String): Boolean
    fun removeMemberFromList(listId: String, userId: String)

    fun addCategory(listId: String, categoryName: String, iconEmoji: String = "📁"): Category
    fun updateCategory(listId: String, categoryId: String, newName: String)
    fun deleteCategory(listId: String, categoryId: String)

    fun addProduct(
        listId: String,
        name: String,
        categoryId: String,
        quantity: String = "1",
        iconEmoji: String = "🛒",
        count: Int = 1
    ): Product

    fun updateProduct(listId: String, product: Product)
    fun toggleProductPurchased(listId: String, productId: String)
    fun deleteProduct(listId: String, productId: String)
}
