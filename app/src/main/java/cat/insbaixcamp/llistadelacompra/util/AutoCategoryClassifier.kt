package cat.insbaixcamp.llistadelacompra.util

object AutoCategoryClassifier {

    data class CategorySuggestion(
        val categoryName: String,
        val emoji: String
    )

    private val rules = listOf(
        // Làctics
        listOf("llet", "iogurt", "formatge", "mantega", "nata", "flam", "cuixart", "mató", "batut", "kèfir") to CategorySuggestion("Làctics", "🥛"),
        
        // Fleca i Pa
        listOf("pa", "croissant", "magdalena", "baguette", "ensaïmada", "galeta", "galetes", "torrades", "muffins", "donut", "coca", "pa de pagès") to CategorySuggestion("Fleca", "🍞"),
        
        // Carn i Peix
        listOf("pollastre", "vedella", "porc", "hamburguesa", "botifarra", "salsitxa", "pernil", "gall d'indi", "llonganissa", "lluç", "sardina", "sardines", "salmó", "tonyina", "gambes", "bacallà", "llamàntol", "calamar") to CategorySuggestion("Carn i Peix", "🥩"),
        
        // Fruita i Verdura
        listOf("tomàquet", "poma", "plàtan", "patata", "ceba", "taronja", "enciam", "maduixa", "llimona", "alvocat", "cogombre", "pastanaga", "all", "pina", "pessic", "melon", "pera", "mandarina", "raïm", "espàrrecs", "espinacs", "carbassó", "albergínia") to CategorySuggestion("Fruita i Verdura", "🍎"),
        
        // Begudes
        listOf("aigua", "suc", "refresc", "cervesa", "vi", "cafè", "te", "orxata", "cava", "vermut", "soda") to CategorySuggestion("Begudes", "🥤"),
        
        // Neteja i Llar
        listOf("detergent", "sabó", "paper", "suavitzant", "lleixiu", "esponja", "fregall", "xampú", "gel", "dentífric", "fregona", "bossa") to CategorySuggestion("Neteja i Llar", "🧹"),
        
        // Despensa
        listOf("arròs", "pasta", "macarrons", "espaguetis", "oli", "sal", "sucre", "farina", "llegums", "garbanzos", "mongetes", "llenties", "tomàquet fregit", "vinagre", "mayonesa") to CategorySuggestion("Despensa", "🥫"),
        
        // Snacks i Dolços
        listOf("xocolata", "patates xips", "fruts secs", "ametlles", "pinares", "ambrosia", "bombolles", "chuches", "gelat", "turró") to CategorySuggestion("Snacks i Dolços", "🍫")
    )

    fun classify(productName: String): CategorySuggestion {
        val lower = productName.lowercase().trim()
        if (lower.isEmpty()) return CategorySuggestion("General", "📦")

        for ((keywords, suggestion) in rules) {
            for (kw in keywords) {
                if (lower.contains(kw)) {
                    return suggestion
                }
            }
        }
        return CategorySuggestion("General", "📦")
    }
}
