package cat.insbaixcamp.llistadelacompra.model

import org.json.JSONObject
import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconEmoji: String = "📁"
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("iconEmoji", iconEmoji)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Category {
            return Category(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", ""),
                iconEmoji = json.optString("iconEmoji", "📁")
            )
        }
    }
}
