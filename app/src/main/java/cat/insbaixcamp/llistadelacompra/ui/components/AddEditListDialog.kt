package cat.insbaixcamp.llistadelacompra.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
fun AddEditListDialog(
    initialTitle: String = "",
    isEditing: Boolean = false,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Canviar nom de la llista" else "Nova llista de la compra")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Nom de la llista") },
                    placeholder = { Text("Ex: Compra del cap de setmana") },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("El nom no pot estar buit", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(title.trim())
                    }
                }
            ) {
                Text(if (isEditing) "Guardar" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·lar")
            }
        }
    )
}
