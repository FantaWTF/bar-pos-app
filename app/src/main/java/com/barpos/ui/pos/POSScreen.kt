package com.barpos.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barpos.BarApplication
import com.barpos.data.database.entity.Member
import com.barpos.data.database.entity.Product
import com.barpos.ui.theme.NegativeRed
import com.barpos.ui.theme.PositiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(
    application: BarApplication,
    onNavigateToHistory: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToPayment: (Long) -> Unit,
    viewModel: POSViewModel = viewModel(factory = POSViewModel.Factory(application))
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val selectedMember by viewModel.selectedMember.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotal by viewModel.cartTotal.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showMemberDropdown by remember { mutableStateOf(false) }
    var memberSearchQuery by remember { mutableStateOf("") }
    var showAddedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshSelectedMember()
    }

    val barName = settings?.barName ?: "Bar"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = barName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Historik")
                    }
                    IconButton(onClick = onNavigateToAdmin) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            if (showAddedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showAddedSnackbar = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text("Tilføjet til konto!")
                }
                LaunchedEffect(showAddedSnackbar) {
                    kotlinx.coroutines.delay(2000)
                    showAddedSnackbar = false
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left side: Member selector + category tabs + product grid
            Column(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Member selector
                MemberSelector(
                    members = members,
                    selectedMember = selectedMember,
                    searchQuery = memberSearchQuery,
                    expanded = showMemberDropdown,
                    onExpandedChange = { showMemberDropdown = it },
                    onSearchQueryChange = { memberSearchQuery = it },
                    onMemberSelected = { member ->
                        viewModel.selectMember(member)
                        showMemberDropdown = false
                        memberSearchQuery = ""
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category tabs
                ScrollableTabRow(
                    selectedTabIndex = if (selectedCategoryId == null) 0
                    else categories.indexOfFirst { it.id == selectedCategoryId }.let { if (it < 0) 0 else it + 1 },
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp
                ) {
                    Tab(
                        selected = selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        text = { Text("Alle") }
                    )
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategoryId == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            text = { Text(category.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Product grid
                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingen produkter. Tilføj produkter via Admin.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onClick = {
                                    if (selectedMember != null) {
                                        viewModel.addToCart(product)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Divider
            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // Right side: Cart
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    "Kurv",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Member balance display
                selectedMember?.let { member ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (member.balance < 0)
                                NegativeRed.copy(alpha = 0.1f)
                            else
                                PositiveGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                member.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Saldo: ${member.balance.toInt()} kr",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (member.balance < 0) NegativeRed else PositiveGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (cartItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (selectedMember == null) "Vælg et medlem først"
                            else "Tryk på produkter for at tilføje",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(cartItems, key = { it.product.id }) { cartItem ->
                            CartItemRow(
                                cartItem = cartItem,
                                onIncrement = { viewModel.incrementQuantity(cartItem.product.id) },
                                onDecrement = { viewModel.decrementQuantity(cartItem.product.id) },
                                onRemove = { viewModel.removeFromCart(cartItem.product.id) }
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total:",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${cartTotal.toInt()} kr",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearCart() },
                        modifier = Modifier.weight(1f),
                        enabled = cartItems.isNotEmpty()
                    ) {
                        Text("Annuller")
                    }
                    Button(
                        onClick = {
                            viewModel.addToAccount {
                                showAddedSnackbar = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cartItems.isNotEmpty() && selectedMember != null
                    ) {
                        Text("Tilføj til konto")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pay balance button
                if (selectedMember != null && (selectedMember?.balance ?: 0.0) < 0) {
                    Button(
                        onClick = { onNavigateToPayment(selectedMember!!.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Betal saldo (${selectedMember?.balance?.toInt()} kr)")
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberSelector(
    members: List<Member>,
    selectedMember: Member?,
    searchQuery: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onMemberSelected: (Member) -> Unit
) {
    val filteredMembers = if (searchQuery.isBlank()) members
    else members.filter { it.name.contains(searchQuery, ignoreCase = true) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = if (expanded) searchQuery else (selectedMember?.name ?: ""),
            onValueChange = { onSearchQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Vælg medlem") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
                onSearchQueryChange("")
            }
        ) {
            filteredMembers.forEach { member ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(member.name)
                            Text(
                                "${member.balance.toInt()} kr",
                                color = if (member.balance < 0) NegativeRed else PositiveGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    onClick = { onMemberSelected(member) }
                )
            }
            if (filteredMembers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Ingen medlemmer fundet") },
                    onClick = {},
                    enabled = false
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${product.price.toInt()} kr",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CartItemRow(
    cartItem: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cartItem.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${cartItem.product.price.toInt()} kr/stk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Fjern en", modifier = Modifier.size(18.dp))
                }
                Text(
                    "${cartItem.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Tilføj en", modifier = Modifier.size(18.dp))
                }
            }

            Text(
                "${cartItem.totalPrice.toInt()} kr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fjern",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
