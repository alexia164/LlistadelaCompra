package cat.insbaixcamp.llistadelacompra.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val ownerId: String,
    val members: List<String> = emptyList(),
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("ownerId", ownerId)
            put("createdAt", createdAt)

            val membersArray = JSONArray()
            members.forEach { membersArray.put(it) }
            put("members", membersArray)

            val categoriesArray = JSONArray()
            categories.forEach { categoriesArray.put(it.toJson()) }
            put("categories", categoriesArray)

            val productsArray = JSONArray()
            products.forEach { productsArray.put(it.toJson()) }
            put("products", productsArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ShoppingList {
            val membersList = mutableListOf<String>()
            val membersArray = json.optJSONArray("members")
            if (membersArray != null) {
                for (i in 0 until membersArray.length()) {
                    membersList.add(membersArray.getString(i))
                }
            }

            val categoriesList = mutableListOf<Category>()
            val categoriesArray = json.optJSONArray("categories")
            if (categoriesArray != null) {
                for (i in 0 until categoriesArray.length()) {
                    categoriesList.add(Category.fromJson(categoriesArray.getJSONObject(i)))
                }
            }

            val productsList = mutableListOf<Product>()
            val productsArray = json.optJSONArray("products")
            if (productsArray != null) {
                for (i in 0 until productsArray.length()) {
                    productsList.add(Product.fromJson(productsArray.getJSONObject(i)))
                }
            }

            return ShoppingList(
                id = json.optString("id", UUID.randomUUID().toString()),
                title = json.optString("title", ""),
                ownerId = json.optString("ownerId", ""),
                members = membersList,
                categories = categoriesList,
                products = productsList,
                createdAt = json.optLong("createdAt", System.currentTimeMillis())
            )
        }
    }
}
