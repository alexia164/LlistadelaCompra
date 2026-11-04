package cat.insbaixcamp.llistadelacompra.data

import android.util.Log
import cat.insbaixcamp.llistadelacompra.model.Category
import cat.insbaixcamp.llistadelacompra.model.Product
import cat.insbaixcamp.llistadelacompra.model.ShoppingList
import cat.insbaixcamp.llistadelacompra.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreListRepository : ListRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val cachedUsers = mutableListOf<User>()
    private var currentUser: User? = null

    override fun getUsers(): List<User> = cachedUsers.toList()

    override fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, "Error en carregar els usuaris")
                close(error)
                return@addSnapshotListener
            }
            val users = snapshot?.documents?.mapNotNull { doc ->
                User(
                    id = doc.id,
                    displayName = doc.getString("displayName") ?: doc.getString("name") ?: "Usuari",
                    email = (doc.getString("email") ?: "").lowercase(),
                    photoUrl = doc.getString("photoUrl")
                )
            } ?: emptyList()

            cachedUsers.clear()
            cachedUsers.addAll(users)
            trySend(users)
        }
        awaitClose { listener.remove() }
    }

    override fun getCurrentUser(): User? {
        val fbUser = auth.currentUser ?: return currentUser
        return User(
            id = fbUser.uid,
            displayName = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "Usuari",
            email = (fbUser.email ?: "").lowercase(),
            photoUrl = fbUser.photoUrl?.toString()
        )
    }

    override fun setCurrentUser(user: User) {
        currentUser = user
        saveUser(user)
    }

    override fun saveUser(user: User): User {
        val cleanEmail = user.email.lowercase().trim()
        val userData = hashMapOf(
            "displayName" to user.displayName,
            "email" to cleanEmail,
            "photoUrl" to (user.photoUrl ?: "")
        )

        db.collection("users").document(user.id)
            .set(userData, SetOptions.merge())
            .addOnFailureListener { e ->
                Log.e("FirestoreListRepo", "Error en desar l'usuari a users/${user.id}: ${e.message}", e)
            }

        return user
    }

    override fun findUserByNameOrEmail(query: String): User? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        return cachedUsers.find {
            it.email.lowercase() == q || it.displayName.lowercase() == q || it.id == q
        }
    }

    override fun getAllListsFlow(): Flow<List<ShoppingList>> = callbackFlow {
        val listener = db.collection("lists").addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, "Error en carregar les llistes")
                close(error)
                return@addSnapshotListener
            }
            val lists = snapshot?.documents?.map { doc ->
                ShoppingList(
                    id = doc.id,
                    title = doc.getString("name") ?: doc.getString("title") ?: "",
                    ownerId = doc.getString("ownerId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } ?: emptyList()
            trySend(lists)
        }
        awaitClose { listener.remove() }
    }

    override fun getListsForUserFlow(userId: String, userEmail: String): Flow<List<ShoppingList>> = callbackFlow {
        val cleanEmail = (if (userEmail.isNotBlank()) userEmail else auth.currentUser?.email ?: "").lowercase().trim()

        if (cleanEmail.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = db.collection("lists")
            .whereArrayContains("memberEmails", cleanEmail)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, "Error en consultar les llistes de l'usuari")
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val lists = snapshot.documents.map { doc ->
                    val memberEmails = (doc.get("memberEmails") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    ShoppingList(
                        id = doc.id,
                        title = doc.getString("name") ?: doc.getString("title") ?: "",
                        ownerId = doc.getString("ownerId") ?: "",
                        members = memberEmails,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                }
                trySend(lists)
            }

        awaitClose { listener.remove() }
    }

    override fun getListByIdFlow(listId: String): Flow<ShoppingList?> = callbackFlow {
        val listRef = db.collection("lists").document(listId)
        val categoriesRef = listRef.collection("categories")
        val productsRef = listRef.collection("products")

        var currentListDoc: ShoppingList? = null
        var currentCategories: List<Category> = emptyList()
        var currentProducts: List<Product> = emptyList()

        fun emitCombined() {
            val list = currentListDoc
            if (list != null) {
                trySend(list.copy(categories = currentCategories, products = currentProducts))
            } else {
                trySend(null)
            }
        }

        val listListener = listRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                currentListDoc = null
                emitCombined()
                return@addSnapshotListener
            }

            val memberEmails = (snapshot.get("memberEmails") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            currentListDoc = ShoppingList(
                id = snapshot.id,
                title = snapshot.getString("name") ?: snapshot.getString("title") ?: "",
                ownerId = snapshot.getString("ownerId") ?: "",
                members = memberEmails,
                createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
            )
            emitCombined()
        }

        val categoriesListener = categoriesRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                currentCategories = emptyList()
            } else {
                currentCategories = snapshot.documents.map { doc ->
                    Category(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        iconEmoji = doc.getString("iconEmoji") ?: "📁"
                    )
                }.sortedBy { it.name }
            }
            emitCombined()
        }

        val productsListener = productsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                currentProducts = emptyList()
            } else {
                currentProducts = snapshot.documents.map { doc ->
                    Product(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        categoryId = doc.getString("categoryId") ?: "",
                        isPurchased = doc.getBoolean("completed") ?: doc.getBoolean("isPurchased") ?: false,
                        iconEmoji = doc.getString("icon") ?: doc.getString("iconEmoji") ?: "🛒",
                        quantity = doc.getString("quantity") ?: "1",
                        count = doc.getLong("count")?.toInt() ?: 1
                    )
                }
            }
            emitCombined()
        }

        awaitClose {
            listListener.remove()
            categoriesListener.remove()
            productsListener.remove()
        }
    }

    override fun createList(title: String, ownerId: String): ShoppingList {
        val cleanTitle = title.trim()
        val ownerEmail = (auth.currentUser?.email ?: "").lowercase().trim()

        val listId = UUID.randomUUID().toString()
        val listRef = db.collection("lists").document(listId)

        val listData = hashMapOf(
            "name" to cleanTitle,
            "ownerId" to ownerId,
            "ownerEmail" to ownerEmail,
            "memberEmails" to listOf(ownerEmail),
            "createdAt" to System.currentTimeMillis()
        )

        listRef.set(listData)
            .addOnFailureListener { e -> Log.e("FirestoreListRepo", "Error en crear la llista: ${e.message}", e) }

        val defaultCat = Category(name = "General", iconEmoji = "📦")
        listRef.collection("categories").document(defaultCat.id).set(
            hashMapOf(
                "name" to defaultCat.name,
                "iconEmoji" to defaultCat.iconEmoji,
                "order" to 0
            )
        )

        return ShoppingList(
            id = listId,
            title = cleanTitle,
            ownerId = ownerId,
            members = listOf(ownerEmail),
            categories = listOf(defaultCat)
        )
    }

    override fun updateListTitle(listId: String, newTitle: String) {
        val cleanTitle = newTitle.trim()
        if (cleanTitle.isNotEmpty()) {
            db.collection("lists").document(listId).update("name", cleanTitle)
        }
    }

    override fun deleteList(listId: String): Boolean {
        val currentUid = auth.currentUser?.uid ?: return false
        val listRef = db.collection("lists").document(listId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listDoc = listRef.get().await()
                val ownerId = listDoc.getString("ownerId")
                if (ownerId != currentUid) {
                    Log.w("FirestoreListRepo", "Permís denegat: Només el propietari pot eliminar la llista.")
                    return@launch
                }

                val categoriesDocs = listRef.collection("categories").get().await()
                val productsDocs = listRef.collection("products").get().await()

                val batch = db.batch()
                categoriesDocs.documents.forEach { batch.delete(it.reference) }
                productsDocs.documents.forEach { batch.delete(it.reference) }
                batch.delete(listRef)

                batch.commit().await()
            } catch (e: Exception) {
                Log.e("FirestoreListRepo", "Error en eliminar la llista: ${e.message}", e)
            }
        }
        return true
    }

    override fun shareListWithUser(listId: String, userIdentifier: String): Boolean {
        val cleanEmail = userIdentifier.trim().lowercase()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) return false

        db.collection("lists").document(listId)
            .update("memberEmails", FieldValue.arrayUnion(cleanEmail))
            .addOnFailureListener { e -> Log.e("FirestoreListRepo", "Error en compartir la llista: ${e.message}", e) }

        return true
    }

    override fun removeMemberFromList(listId: String, userId: String) {
        val cleanEmail = userId.trim().lowercase()
        val listRef = db.collection("lists").document(listId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = listRef.get().await()
                val ownerEmail = (doc.getString("ownerEmail") ?: "").lowercase()

                if (cleanEmail == ownerEmail) {
                    Log.w("FirestoreListRepo", "El propietari no es pot eliminar a si mateix de la llista.")
                    return@launch
                }

                listRef.update("memberEmails", FieldValue.arrayRemove(cleanEmail)).await()
            } catch (e: Exception) {
                Log.e("FirestoreListRepo", "Error en eliminar el membre: ${e.message}", e)
            }
        }
    }

    override fun addCategory(listId: String, categoryName: String, iconEmoji: String): Category {
        val cleanName = categoryName.trim()
        if (cleanName.isEmpty()) return Category(name = "General")

        val catId = UUID.randomUUID().toString()
        val category = Category(id = catId, name = cleanName, iconEmoji = iconEmoji)

        db.collection("lists").document(listId)
            .collection("categories").document(catId)
            .set(
                hashMapOf(
                    "name" to cleanName,
                    "iconEmoji" to iconEmoji,
                    "order" to System.currentTimeMillis()
                )
            )

        return category
    }

    override fun updateCategory(listId: String, categoryId: String, newName: String) {
        val cleanName = newName.trim()
        if (cleanName.isNotEmpty()) {
            db.collection("lists").document(listId)
                .collection("categories").document(categoryId)
                .update("name", cleanName)
        }
    }

    override fun deleteCategory(listId: String, categoryId: String) {
        val listRef = db.collection("lists").document(listId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                listRef.collection("categories").document(categoryId).delete().await()

                val prods = listRef.collection("products")
                    .whereEqualTo("categoryId", categoryId)
                    .get().await()

                val batch = db.batch()
                prods.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            } catch (e: Exception) {
                Log.e("FirestoreListRepo", "Error en eliminar la categoria: ${e.message}", e)
            }
        }
    }

    override fun addProduct(
        listId: String,
        name: String,
        categoryId: String,
        quantity: String,
        iconEmoji: String,
        count: Int
    ): Product {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return Product(name = "Producte", categoryId = categoryId)

        val listRef = db.collection("lists").document(listId)
        val prodsRef = listRef.collection("products")
        val currentUid = auth.currentUser?.uid ?: ""

        val resultProduct = Product(
            name = trimmedName,
            categoryId = categoryId,
            quantity = quantity.ifBlank { "1" },
            iconEmoji = iconEmoji,
            count = count.coerceAtLeast(1),
            isPurchased = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingDocs = prodsRef
                    .whereEqualTo("categoryId", categoryId)
                    .whereEqualTo("name", trimmedName)
                    .get().await()

                if (!existingDocs.isEmpty) {
                    val existingDoc = existingDocs.documents.first()
                    val currentCount = existingDoc.getLong("count")?.toInt() ?: 1
                    val newCount = currentCount + count

                    prodsRef.document(existingDoc.id).update(
                        hashMapOf<String, Any>(
                            "count" to newCount,
                            "completed" to false,
                            "quantity" to if (quantity.isNotBlank() && quantity != "1") quantity else (existingDoc.getString("quantity") ?: "1")
                        )
                    ).await()
                } else {
                    val prodId = UUID.randomUUID().toString()
                    val prodData = hashMapOf(
                        "name" to trimmedName,
                        "categoryId" to categoryId,
                        "completed" to false,
                        "icon" to iconEmoji,
                        "quantity" to quantity.ifBlank { "1" },
                        "count" to count.coerceAtLeast(1),
                        "createdBy" to currentUid
                    )
                    prodsRef.document(prodId).set(prodData).await()
                }
            } catch (e: Exception) {
                Log.e("FirestoreListRepo", "Error en afegir el producte: ${e.message}", e)
            }
        }

        return resultProduct
    }

    override fun updateProduct(listId: String, product: Product) {
        if (product.name.isBlank()) return

        val prodRef = db.collection("lists").document(listId)
            .collection("products").document(product.id)

        prodRef.update(
            hashMapOf<String, Any>(
                "name" to product.name.trim(),
                "categoryId" to product.categoryId,
                "completed" to product.isPurchased,
                "icon" to product.iconEmoji,
                "quantity" to product.quantity,
                "count" to product.count
            )
        )
    }

    override fun toggleProductPurchased(listId: String, productId: String) {
        val prodRef = db.collection("lists").document(listId)
            .collection("products").document(productId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = prodRef.get().await()
                val currentStatus = doc.getBoolean("completed") ?: false
                prodRef.update("completed", !currentStatus).await()
            } catch (e: Exception) {
                Log.e("FirestoreListRepo", "Error en canviar l'estat del producte: ${e.message}", e)
            }
        }
    }

    override fun deleteProduct(listId: String, productId: String) {
        db.collection("lists").document(listId)
            .collection("products").document(productId)
            .delete()
    }

    private fun handleFirestoreError(e: Throwable, contextMessage: String) {
        val message = when ((e as? FirebaseFirestoreException)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permís denegat a Firestore."
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sense connexió a Internet. Treballant en mode fora de línia."
            else -> e.localizedMessage ?: "Error en la comunicació amb el servidor."
        }
        Log.e("FirestoreListRepo", "$contextMessage: $message", e)
    }
}
