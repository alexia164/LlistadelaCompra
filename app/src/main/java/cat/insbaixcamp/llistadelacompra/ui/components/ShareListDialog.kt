package cat.insbaixcamp.llistadelacompra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cat.insbaixcamp.llistadelacompra.model.User

@Composable
fun ShareListDialog(
    members: List<User>,
    owner: User?,
    currentUserId: String,
    currentUserEmail: String = "",
    onShareWithUser: (String) -> Boolean,
    onRemoveMember: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var userInput by remember { mutableStateOf("") }
    var shareError by remember { mutableStateOf<String?>(null) }

    val isOwner = (owner?.id == currentUserId) ||
            (currentUserEmail.isNotEmpty() && owner?.email?.lowercase() == currentUserEmail.lowercase())

    val handleAddUser = {
        val trimmed = userInput.trim()
        if (trimmed.isEmpty()) {
            shareError = "Introdueix un nom o correu electrònic"
        } else {
            val success = onShareWithUser(trimmed)
            if (success) {
                userInput = ""
                shareError = null
            } else {
                shareError = "No s'ha pogut afegir l'usuari"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartir la llista") },
        text = {
            Column {
                Text(
                    text = "Introdueix el nom o correu electrònic de l'usuari amb qui vols compartir aquesta llista:",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        shareError = null
                    },
                    label = { Text("Nom o email") },
                    placeholder = { Text("Ex: laia@example.com") },
                    singleLine = true,
                    isError = shareError != null,
                    supportingText = {
                        shareError?.let { err ->
                            Text(text = err, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { handleAddUser() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Membres amb accés (${members.size + (if (owner != null) 1 else 0)}):",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.height(180.dp)) {
                    owner?.let {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(it.displayName, style = MaterialTheme.typography.bodyMedium)
                                    Text(it.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    "Propietari/a",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider()
                        }
                    }

                    items(members) { member ->
                        val isSelf = member.id == currentUserId ||
                                (currentUserEmail.isNotEmpty() && member.email.lowercase() == currentUserEmail.lowercase())
                        val canRemove = isOwner || !isSelf

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isSelf) "${member.displayName} (tu)" else member.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(member.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (canRemove) {
                                IconButton(onClick = { onRemoveMember(member.id) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar accés",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { handleAddUser() }) {
                Text("Afegir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tancar")
            }
        }
    )
}
