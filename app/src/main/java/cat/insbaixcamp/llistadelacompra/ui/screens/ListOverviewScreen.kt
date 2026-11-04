package cat.insbaixcamp.llistadelacompra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.insbaixcamp.llistadelacompra.model.ShoppingList
import cat.insbaixcamp.llistadelacompra.model.User
import cat.insbaixcamp.llistadelacompra.ui.components.AddEditListDialog
import cat.insbaixcamp.llistadelacompra.ui.components.ConfirmDeleteDialog
import cat.insbaixcamp.llistadelacompra.ui.components.EmptyState
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthUiState
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthViewModel
import cat.insbaixcamp.llistadelacompra.viewmodel.ListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOverviewScreen(
    authViewModel: AuthViewModel,
    listViewModel: ListViewModel,
    onNavigateToListDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()

    val myLists by listViewModel.myLists.collectAsState()
    val sharedLists by listViewModel.sharedLists.collectAsState()

    var showUserMenu by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<ShoppingList?>(null) }
    var listToDelete by remember { mutableStateOf<ShoppingList?>(null) }

    val activeUser = (uiState as? AuthUiState.SignedIn)?.user ?: return

    LaunchedEffect(activeUser) {
        listViewModel.observeLists(activeUser)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            user = activeUser,
                            onClick = { showUserMenu = true }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Llistes de la Compra",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeUser.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showUserMenu = true }) {
                            UserAvatar(user = activeUser, onClick = { showUserMenu = true })
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(activeUser.displayName, fontWeight = FontWeight.Bold)
                                        Text(activeUser.email, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tanca la sessió", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    authViewModel.signOut(context)
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova llista")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (myLists.isEmpty() && sharedLists.isEmpty()) {
                EmptyState(
                    title = "Encara no tens cap llista",
                    subtitle = "Crea la teva primera llista de la compra o demana que te'n comparteixin una.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (myLists.isNotEmpty()) {
                        item {
                            Text(
                                text = "Les meves llistes (${myLists.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(myLists) { shoppingList ->
                            ShoppingListCard(
                                shoppingList = shoppingList,
                                allUsers = allUsers,
                                currentUserId = activeUser.id,
                                onClick = { onNavigateToListDetail(shoppingList.id) },
                                onEdit = { listToEdit = shoppingList },
                                onDelete = { listToDelete = shoppingList }
                            )
                        }
                    }

                    if (sharedLists.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Compartides amb mi (${sharedLists.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(sharedLists) { shoppingList ->
                            ShoppingListCard(
                                shoppingList = shoppingList,
                                allUsers = allUsers,
                                currentUserId = activeUser.id,
                                onClick = { onNavigateToListDetail(shoppingList.id) },
                                onEdit = { listToEdit = shoppingList },
                                onDelete = null
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AddEditListDialog(
            isEditing = false,
            onConfirm = { newTitle ->
                showCreateDialog = false
                val created = listViewModel.createList(newTitle, activeUser.id)
                created?.let { onNavigateToListDetail(it.id) }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    listToEdit?.let { targetList ->
        AddEditListDialog(
            initialTitle = targetList.title,
            isEditing = true,
            onConfirm = { updatedTitle ->
                listViewModel.renameList(targetList.id, updatedTitle)
                listToEdit = null
            },
            onDismiss = { listToEdit = null }
        )
    }

    listToDelete?.let { targetList ->
        ConfirmDeleteDialog(
            title = "Eliminar llista",
            message = "Estàs segur que vols eliminar la llista '${targetList.title}'? S'esborraran tots els seus productes.",
            onConfirm = {
                listViewModel.deleteList(targetList.id)
                listToDelete = null
            },
            onDismiss = { listToDelete = null }
        )
    }
}

@Composable
private fun UserAvatar(
    user: User,
    onClick: () -> Unit
) {
    val initial = user.displayName.firstOrNull()?.uppercase() ?: "U"

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ShoppingListCard(
    shoppingList: ShoppingList,
    allUsers: List<User>,
    currentUserId: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val isOwner = shoppingList.ownerId == currentUserId
    val ownerUser = allUsers.find { it.id == shoppingList.ownerId || (it.email.isNotEmpty() && it.email.lowercase() == shoppingList.ownerId.lowercase()) }

    val totalProducts = shoppingList.products.size
    val purchasedProducts = shoppingList.products.count { it.isPurchased }
    val progress = if (totalProducts > 0) purchasedProducts.toFloat() / totalProducts else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = shoppingList.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar nom",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar llista",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOwner) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "La meva llista",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Compartida per ${ownerUser?.displayName ?: "un altre usuari"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (shoppingList.members.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "👥 ${shoppingList.members.size} membres",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (totalProducts == 0) "Cap producte afegit" else "$purchasedProducts de $totalProducts completats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        }
    }
}
