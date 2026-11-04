package cat.insbaixcamp.llistadelacompra.util

import cat.insbaixcamp.llistadelacompra.model.PredefinedProduct

object PredefinedCatalog {
    val items = listOf(
        // Làctics
        PredefinedProduct("Llet sencer / semidesnatada", "Làctics", "🥛"),
        PredefinedProduct("Iogurt natural", "Làctics", "🍦"),
        PredefinedProduct("Formatge de Maó / Manxego", "Làctics", "🧀"),
        PredefinedProduct("Mantega", "Làctics", "🧈"),
        PredefinedProduct("Mató fresc", "Làctics", "🥣"),

        // Fleca
        PredefinedProduct("Pa de pagès", "Fleca", "🍞"),
        PredefinedProduct("Croissants de mantega", "Fleca", "🥐"),
        PredefinedProduct("Baguette", "Fleca", "🥖"),
        PredefinedProduct("Galetes de xocolata", "Fleca", "🍪"),
        PredefinedProduct("Magdalenes casolanes", "Fleca", "🧁"),

        // Carn i Peix
        PredefinedProduct("Pit de pollastre", "Carn i Peix", "🍗"),
        PredefinedProduct("Hamburgueses de vedella", "Carn i Peix", "🥩"),
        PredefinedProduct("Botifarra de la Garrotxa", "Carn i Peix", "🌭"),
        PredefinedProduct("Pernil dolç / curat", "Carn i Peix", "🥓"),
        PredefinedProduct("Lluç fresc", "Carn i Peix", "🐟"),
        PredefinedProduct("Tonyina en llauna", "Carn i Peix", "🐟"),
        PredefinedProduct("Salmó en filets", "Carn i Peix", "🍣"),

        // Fruita i Verdura
        PredefinedProduct("Tomàquets de penjar", "Fruita i Verdura", "🍅"),
        PredefinedProduct("Pomes Golden", "Fruita i Verdura", "🍎"),
        PredefinedProduct("Plàtans de les Canàries", "Fruita i Verdura", "🍌"),
        PredefinedProduct("Patates de redona", "Fruita i Verdura", "🥔"),
        PredefinedProduct("Cebes figueres", "Fruita i Verdura", "🧅"),
        PredefinedProduct("Enciam fresc", "Fruita i Verdura", "🥬"),
        PredefinedProduct("Taronges de sucar", "Fruita i Verdura", "🍊"),
        PredefinedProduct("Maduixes del Maresme", "Fruita i Verdura", "🍓"),
        PredefinedProduct("Alls", "Fruita i Verdura", "🧄"),

        // Begudes
        PredefinedProduct("Aigua mineral (6x1.5L)", "Begudes", "💧"),
        PredefinedProduct("Suc de taronja natural", "Begudes", "🧃"),
        PredefinedProduct("Cervesa artesana", "Begudes", "🍺"),
        PredefinedProduct("Vi negre del Priorat", "Begudes", "🍷"),
        PredefinedProduct("Cafè molt natural", "Begudes", "☕"),

        // Neteja i Llar
        PredefinedProduct("Detergent per a la roba", "Neteja i Llar", "🧺"),
        PredefinedProduct("Paper higiènic (12 rols)", "Neteja i Llar", "🧻"),
        PredefinedProduct("Sabó de rentar plats", "Neteja i Llar", "🧽"),

        // Despensa
        PredefinedProduct("Arròs bomba del Delta", "Despensa", "🍚"),
        PredefinedProduct("Macarrons / Pasta", "Despensa", "🍝"),
        PredefinedProduct("Oli d'oliva verge extra", "Despensa", "🫒"),
        PredefinedProduct("Sal marina", "Despensa", "🧂"),

        // Snacks
        PredefinedProduct("Xocolata 70% cacau", "Snacks i Dolços", "🍫"),
        PredefinedProduct("Patates xips artesanes", "Snacks i Dolços", "🍟")
    )
}
