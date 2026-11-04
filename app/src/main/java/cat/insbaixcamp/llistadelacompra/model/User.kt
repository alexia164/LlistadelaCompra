package cat.insbaixcamp.llistadelacompra.model

import org.json.JSONObject

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null
) {
    val name: String
        get() = displayName

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("displayName", displayName)
            put("email", email.lowercase())
            put("photoUrl", photoUrl ?: "")
        }
    }

    companion object {
        fun fromJson(json: JSONObject): User {
            val dName = json.optString("displayName", json.optString("name", "Usuari"))
            return User(
                id = json.optString("id", ""),
                displayName = dName,
                email = json.optString("email", "").lowercase(),
                photoUrl = json.optString("photoUrl", "").ifEmpty { null }
            )
        }
    }
}
