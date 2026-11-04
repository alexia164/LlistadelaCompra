package cat.insbaixcamp.llistadelacompra.data

import android.content.Context
import cat.insbaixcamp.llistadelacompra.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<User?>
    fun getCurrentUser(): User?
    suspend fun signIn(context: Context): Result<User>
    suspend fun signOut(context: Context)
}
