package cat.insbaixcamp.llistadelacompra.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.insbaixcamp.llistadelacompra.model.Category
import cat.insbaixcamp.llistadelacompra.util.AutoCategoryClassifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    categories: List<Category>,
    initialName: String = "",
    initialQuantity: String = "1",
    initialCount: Int = 1,
    initialCategoryId: String? = null,
    isEditing: Boolean = false,
    onConfirm: (name: String, quantity: String, count: Int, categoryId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var productName by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf(initialQuantity) }
    var count by remember { mutableIntStateOf(initialCount) }
    var selectedCategory by remember {
        mutableStateOf(categories.find { it.id == initialCategoryId })
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val autoCategorySuggestion by remember(productName) {
        derivedStateOf { AutoCategoryClassifier.classify(productName) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Editar producte" else "Afegir producte")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Nom del producte *") },
                    placeholder = { Text("Ex: Llet, Pa de pagès, Pomes...") },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("El nom del producte no pot estar buit", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isEditing && productName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Classificació automàtica: ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${autoCategorySuggestion.emoji} ${autoCategorySuggestion.categoryName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recumpte / Unitats:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (count > 1) count-- },
                            enabled = count > 1
                        ) {
                            Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { count++ }
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Mida / Pes (opcional)") },
                    placeholder = { Text("Ex: 1 kg, 2 packs, 500g") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { "${it.iconEmoji} ${it.name}" }
                            ?: "Automàtica (Segons el nom)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("⚡ Automàtica (Segons el nom)") },
                            onClick = {
                                selectedCategory = null
                                dropdownExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.iconEmoji} ${category.name}") },
                                onClick = {
                                    selectedCategory = category
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (productName.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(productName.trim(), quantity.trim(), count, selectedCategory?.id)
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
