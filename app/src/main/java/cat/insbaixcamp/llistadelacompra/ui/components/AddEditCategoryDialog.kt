package cat.insbaixcamp.llistadelacompra.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddEditCategoryDialog(
    initialName: String = "",
    initialEmoji: String = "📁",
    isEditing: Boolean = false,
    onConfirm: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedEmoji by remember { mutableStateOf(initialEmoji) }
    var isError by remember { mutableStateOf(false) }

    val emojis = listOf("📁", "🥛", "🍞", "🍎", "🥩", "🥤", "🧹", "🥫", "🍫", "❄️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Editar categoria" else "Nova categoria")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Nom de la categoria") },
                    placeholder = { Text("Ex: Supermercat") },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("El nom no pot estar buit", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Icona de la categoria:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    emojis.take(5).forEach { emoji ->
                        FilterChip(
                            selected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji },
                            label = { Text(emoji) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    emojis.drop(5).forEach { emoji ->
                        FilterChip(
                            selected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji },
                            label = { Text(emoji) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(name.trim(), selectedEmoji)
                    }
                }
            ) {
                Text(if (isEditing) "Guardar" else "Afegir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}
