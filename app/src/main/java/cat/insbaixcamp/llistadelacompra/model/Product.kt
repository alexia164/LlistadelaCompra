package cat.insbaixcamp.llistadelacompra.model

import org.json.JSONObject
import java.util.UUID

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val categoryId: String,
    val isPurchased: Boolean = false,
    val iconEmoji: String = "🛒",
    val quantity: String = "1",
    val count: Int = 1
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("categoryId", categoryId)
            put("isPurchased", isPurchased)
            put("iconEmoji", iconEmoji)
            put("quantity", quantity)
            put("count", count)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Product {
            return Product(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", ""),
                categoryId = json.optString("categoryId", ""),
                isPurchased = json.optBoolean("isPurchased", false),
                iconEmoji = json.optString("iconEmoji", "🛒"),
                quantity = json.optString("quantity", "1"),
                count = json.optInt("count", 1)
            )
        }
    }
}
