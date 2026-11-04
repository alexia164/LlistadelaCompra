package cat.insbaixcamp.llistadelacompra.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import cat.insbaixcamp.llistadelacompra.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override val currentUserFlow: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val user = firebaseUser?.let {
                User(
                    id = it.uid,
                    displayName = it.displayName ?: it.email?.substringBefore("@") ?: "Usuari",
                    email = (it.email ?: "").lowercase(),
                    photoUrl = it.photoUrl?.toString()
                )
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuari",
            email = (firebaseUser.email ?: "").lowercase(),
            photoUrl = firebaseUser.photoUrl?.toString()
        )
    }

    override suspend fun signIn(context: Context): Result<User> {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            val webClientId = if (resId != 0) {
                context.getString(resId)
            } else {
                "163428996350-s34huf3mac3q4bp13rrj2tlsdq3g88ca.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val firebaseUser = authResult.user
                ?: throw IllegalStateException("Inici de sessió fallit a Firebase.")

            val user = User(
                id = firebaseUser.uid,
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Usuari",
                email = (firebaseUser.email ?: "").lowercase(),
                photoUrl = firebaseUser.photoUrl?.toString()
            )

            Result.success(user)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Cancel·lat per l'usuari."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Error de configuració o credencials: ${e.localizedMessage ?: "Error desconegut"}"))
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("network", ignoreCase = true) == true -> "Sense connexió a Internet."
                else -> e.localizedMessage ?: "Error en l'autenticació."
            }
            Result.failure(Exception(msg))
        }
    }

    override suspend fun signOut(context: Context) {
        try {
            auth.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
