package cat.insbaixcamp.llistadelacompra.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cat.insbaixcamp.llistadelacompra.model.Category
import cat.insbaixcamp.llistadelacompra.model.Product
import cat.insbaixcamp.llistadelacompra.model.User
import cat.insbaixcamp.llistadelacompra.ui.components.AddEditCategoryDialog
import cat.insbaixcamp.llistadelacompra.ui.components.AddEditProductDialog
import cat.insbaixcamp.llistadelacompra.ui.components.ConfirmDeleteDialog
import cat.insbaixcamp.llistadelacompra.ui.components.EmptyState
import cat.insbaixcamp.llistadelacompra.ui.components.PredefinedCatalogSheet
import cat.insbaixcamp.llistadelacompra.ui.components.ShareListDialog
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthUiState
import cat.insbaixcamp.llistadelacompra.viewmodel.AuthViewModel
import cat.insbaixcamp.llistadelacompra.viewmodel.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    authViewModel: AuthViewModel,
    detailViewModel: DetailViewModel,
    onNavigateBack: () -> Unit
) {
    val shoppingList by detailViewModel.shoppingList.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val allUsers by authViewModel.allUsers.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    var showPredefinedCatalog by remember { mutableStateOf(false) }

    val categoryExpandedMap = remember { mutableStateMapOf<String, Boolean>() }

    val activeUser = (authState as? AuthUiState.SignedIn)?.user ?: return

    val listData = shoppingList ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Carregant llista...")
        }
        return
    }

    val listOwner = allUsers.find {
        it.id == listData.ownerId || (it.email.isNotEmpty() && it.email.lowercase() == listData.ownerId.lowercase())
    }

    val memberUsers = listData.members.distinct().map { memberKey ->
        allUsers.find {
            it.id == memberKey || (it.email.isNotEmpty() && it.email.lowercase() == memberKey.lowercase())
        } ?: User(
            id = memberKey,
            displayName = if (memberKey.contains("@")) memberKey.substringBefore("@") else memberKey,
            email = if (memberKey.contains("@")) memberKey.lowercase() else "$memberKey@example.com"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = listData.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${listData.products.count { it.isPurchased }}/${listData.products.size} productes completats",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tornar enrere"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showPredefinedCatalog = true }) {
                        Icon(Icons.Default.LibraryAdd, contentDescription = "Catàleg habitual")
                    }
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Category, contentDescription = "Afegir Categoria")
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Compartir Llista")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    onClick = { showPredefinedCatalog = true },
                    icon = { Icon(Icons.Default.LibraryAdd, contentDescription = null) },
                    text = { Text("Catàleg") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Afegir producte")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (listData.categories.isEmpty() && listData.products.isEmpty()) {
                EmptyState(
                    title = "Llista buida",
                    subtitle = "Prem '+' per afegir el teu primer producte o consulta el 'Catàleg'.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listData.categories.forEach { category ->
                        val categoryProducts = listData.products.filter { it.categoryId == category.id }
                        val isExpanded = categoryExpandedMap.getOrDefault(category.id, true)

                        item(key = category.id) {
                            CategorySectionHeader(
                                category = category,
                                productCount = categoryProducts.sumOf { it.count },
                                purchasedCount = categoryProducts.filter { it.isPurchased }.sumOf { it.count },
                                isExpanded = isExpanded,
                                onToggleExpand = {
                                    categoryExpandedMap[category.id] = !isExpanded
                                },
                                onEditCategory = { categoryToEdit = category },
                                onDeleteCategory = { categoryToDelete = category }
                            )

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 6.dp, start = 8.dp, end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (categoryProducts.isEmpty()) {
                                        Text(
                                            text = "Sense productes en aquesta categoria",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                                        )
                                    } else {
                                        categoryProducts.forEach { product ->
                                            ProductRowItem(
                                                product = product,
                                                onTogglePurchased = {
                                                    detailViewModel.toggleProductPurchased(product.id)
                                                },
                                                onEditProduct = { productToEdit = product },
                                                onDeleteProduct = { productToDelete = product }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        ShareListDialog(
            members = memberUsers,
            owner = listOwner,
            currentUserId = activeUser.id,
            currentUserEmail = activeUser.email,
            onShareWithUser = { emailOrName ->
                detailViewModel.shareListWithUser(emailOrName)
            },
            onRemoveMember = { memberId ->
                detailViewModel.removeMember(memberId)
            },
            onDismiss = { showShareDialog = false }
        )
    }

    if (showAddCategoryDialog) {
        AddEditCategoryDialog(
            isEditing = false,
            onConfirm = { name, emoji ->
                showAddCategoryDialog = false
                detailViewModel.addCategory(name, emoji)
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    categoryToEdit?.let { targetCat ->
        AddEditCategoryDialog(
            initialName = targetCat.name,
            initialEmoji = targetCat.iconEmoji,
            isEditing = true,
            onConfirm = { name, _ ->
                detailViewModel.updateCategory(targetCat.id, name)
                categoryToEdit = null
            },
            onDismiss = { categoryToEdit = null }
        )
    }

    categoryToDelete?.let { targetCat ->
        ConfirmDeleteDialog(
            title = "Eliminar categoria",
            message = "Estàs segur que vols eliminar la categoria '${targetCat.name}'? S'esborraran també tots els productes associats.",
            onConfirm = {
                detailViewModel.deleteCategory(targetCat.id)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }

    if (showAddProductDialog) {
        AddEditProductDialog(
            categories = listData.categories,
            isEditing = false,
            onConfirm = { name, quantity, count, categoryId ->
                showAddProductDialog = false
                detailViewModel.addProduct(
                    productName = name,
                    quantity = quantity,
                    count = count,
                    explicitCategoryId = categoryId
                )
            },
            onDismiss = { showAddProductDialog = false }
        )
    }

    productToEdit?.let { targetProduct ->
        AddEditProductDialog(
            categories = listData.categories,
            initialName = targetProduct.name,
            initialQuantity = targetProduct.quantity,
            initialCount = targetProduct.count,
            initialCategoryId = targetProduct.categoryId,
            isEditing = true,
            onConfirm = { name, quantity, count, categoryId ->
                detailViewModel.updateProduct(
                    targetProduct.copy(
                        name = name,
                        quantity = quantity,
                        count = count,
                        categoryId = categoryId ?: targetProduct.categoryId
                    )
                )
                productToEdit = null
            },
            onDismiss = { productToEdit = null }
        )
    }

    productToDelete?.let { targetProduct ->
        ConfirmDeleteDialog(
            title = "Eliminar producte",
            message = "Estàs segur que vols eliminar '${targetProduct.name}' de la llista?",
            onConfirm = {
                detailViewModel.deleteProduct(targetProduct.id)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }

    if (showPredefinedCatalog) {
        PredefinedCatalogSheet(
            onAddProduct = { predefined ->
                detailViewModel.addPredefinedProduct(predefined)
            },
            onDismiss = { showPredefinedCatalog = false }
        )
    }
}

@Composable
private fun CategorySectionHeader(
    category: Category,
    productCount: Int,
    purchasedCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.iconEmoji,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$purchasedCount/$productCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opcions de categoria")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar nom") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditCategory()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar categoria", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDeleteCategory()
                        }
                    )
                }
            }

            IconButton(onClick = onToggleExpand) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Plegar" else "Desplegar"
                )
            }
        }
    }
}

@Composable
private fun ProductRowItem(
    product: Product,
    onTogglePurchased: () -> Unit,
    onEditProduct: () -> Unit,
    onDeleteProduct: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val alphaVal = if (product.isPurchased) 0.5f else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaVal),
        colors = CardDefaults.cardColors(
            containerColor = if (product.isPurchased)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (product.isPurchased) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = product.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = product.iconEmoji,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (product.isPurchased) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        fontWeight = if (product.isPurchased) FontWeight.Normal else FontWeight.Medium,
                        color = if (product.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )

                    if (product.count > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "x${product.count}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (product.quantity.isNotBlank() && product.quantity != "1") {
                    Text(
                        text = "Quantitat: ${product.quantity}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opcions del producte",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar producte") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEditProduct()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDeleteProduct()
                        }
                    )
                }
            }
        }
    }
}
