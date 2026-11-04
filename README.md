<div align="center">

# 🛒 Llista de la Compra Compartida

**Crea llistes de la compra, comparteix-les amb qui vulguis i edita-les en temps real.**

![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=black)
![Material 3](https://img.shields.io/badge/Design-Material%203-4CAF50)

### 📲 [Descarrega l'APK](ENLLAÇ_AL_DRIVE_O_GITHUB)

</div>

---

## 📖 Descripció

Aplicació Android per gestionar llistes de la compra de manera col·laborativa. Cada llista és personal, però el propietari pot compartir-la amb altres persones, que podran veure-la i editar-la des del seu propi mòbil. Els canvis es sincronitzen en temps real gràcies a Firebase.

## ✨ Funcionalitats

| | Funcionalitat |
|---|---|
| 🔐 | **Inici de sessió amb Google** amb el compte real del dispositiu |
| 📋 | **Diverses llistes per usuari**: «Les meves llistes» i «Compartides amb mi» |
| 🤝 | **Compartir llistes** pel correu de Gmail, amb permisos d'edició |
| 🗂️ | **Categories**: crear, veure, editar i eliminar (el nom és obligatori) |
| 🥕 | **Productes**: crear, veure, editar i eliminar (el nom és obligatori) |
| ✅ | **Marcar com a completat** sense eliminar el producte, i es pot desmarcar |
| 👥 | **Gestió de membres**: veure qui té accés i deixar de compartir |
| ☁️ | **Sincronització en temps real** i suport sense connexió |

### 🌟 Característiques extra

- **Classificació automàtica**: en escriure un producte, se li assigna la categoria corresponent (per exemple, «llet» → *Làctics*, «pa» → *Fleca*). Si la categoria no existeix a la llista, es crea automàticament, i l'usuari la pot canviar manualment.
- **Productes predefinits amb icones**: catàleg dels articles més habituals per afegir-los amb un sol toc.

## 🚀 Com provar l'aplicació

1. Descarrega l'APK des de [aquest enllaç](ENLLAÇ_AL_DRIVE_O_GITHUB).
2. Al mòbil, obre el fitxer i permet **instal·lar aplicacions d'orígens desconeguts** si Android ho demana.
3. Si Google Play Protect avisa, tria **Més detalls → Instal·la igualment**.
4. Obre l'app i **inicia la sessió amb Google**.

### 🧪 Provar el fet de compartir

1. Amb el **compte A**, crea una llista i comparteix-la amb el Gmail del **compte B**.
2. Amb el **compte B** (en un altre mòbil o tancant la sessió), la llista apareixerà a «Compartides amb mi».
3. Edita un producte o marca'l com a completat: l'altre usuari ho veurà actualitzat.

> ℹ️ Només el **propietari** pot eliminar una llista. Qualsevol membre pot gestionar-ne categories i productes.

## 🛠️ Tecnologies

- **Llenguatge:** Kotlin
- **Interfície:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM (ViewModel + StateFlow) i Navigation Compose
- **Autenticació:** Firebase Authentication + Credential Manager (Google)
- **Base de dades:** Cloud Firestore
- **Patró de dades:** interfície `ListRepository` amb implementació `FirestoreListRepository`

## 🗃️ Estructura de dades (Firestore)

```
users/{uid}
   displayName, email, photoUrl

lists/{listId}
   name, ownerId, ownerEmail, memberEmails[], createdAt
   ├── categories/{categoryId}   → name, order
   └── products/{productId}      → name, categoryId, completed, icon, createdBy
```

## 🔒 Seguretat

Les regles de Firestore fan que només els membres d'una llista (el seu correu és a `memberEmails`) puguin llegir-la i editar-la, i que només el propietari la pugui eliminar.

## ⚙️ Compilar el projecte

1. Clona el repositori i obre'l amb **Android Studio**.
2. Crea un projecte a Firebase, registra l'app Android i afegeix el SHA-1 de debug.
3. Activa **Authentication → Google** i crea la base de dades **Firestore**.
4. Descarrega `google-services.json` i col·loca'l a `app/`.
5. Executa l'app en un emulador amb Google Play o en un mòbil real.

## 👤 Autoria

**[El teu nom]**
[Assignatura / Curs / Centre] · [Any]

---

<div align="center">Fet amb ❤️ i molta llista de la compra 🛒</div>
