package com.barpos.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barpos.BarApplication
import com.barpos.data.database.entity.TransactionType
import com.barpos.ui.theme.NegativeRed
import com.barpos.ui.theme.PositiveGreen
import com.barpos.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    application: BarApplication,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(application))
) {
    val displayTransactions by viewModel.displayTransactions.collectAsStateWithLifecycle()
    val selectedTransactionId by viewModel.selectedTransactionId.collectAsStateWithLifecycle()
    val selectedTransactionItems by viewModel.selectedTransactionItems.collectAsStateWithLifecycle()
    val allMembers by viewModel.allMembers.collectAsStateWithLifecycle()
    val filterMemberId by viewModel.filterMemberId.collectAsStateWithLifecycle()

    var showFilterDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Købshistorik") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Tilbage")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left side: Transaction list
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Filter row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Filter:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = showFilterDropdown,
                        onExpandedChange = { showFilterDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (filterMemberId == null) "Alle medlemmer"
                            else allMembers.find { it.id == filterMemberId }?.name ?: "Alle",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFilterDropdown) },
                            singleLine = true,
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = showFilterDropdown,
                            onDismissRequest = { showFilterDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Alle medlemmer") },
                                onClick = {
                                    viewModel.setFilterMember(null)
                                    showFilterDropdown = false
                                }
                            )
                            allMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.name) },
                                    onClick = {
                                        viewModel.setFilterMember(member.id)
                                        showFilterDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (displayTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ingen transaktioner endnu",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(displayTransactions, key = { it.transaction.id }) { display ->
                            TransactionRow(
                                display = display,
                                isSelected = selectedTransactionId == display.transaction.id,
                                onClick = { viewModel.selectTransaction(display.transaction.id) }
                            )
                        }
                    }
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            // Right side: Transaction details
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text(
                    "Detaljer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTransactionId == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Vælg en transaktion for at se detaljer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val display = displayTransactions.find {
                        it.transaction.id == selectedTransactionId
                    }
                    if (display != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    display.memberName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    DateUtils.formatDateTime(display.transaction.createdAt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val typeLabel = if (display.transaction.type == TransactionType.PURCHASE) "Køb" else "Betaling"
                                Text(
                                    typeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (display.transaction.type == TransactionType.PURCHASE) NegativeRed else PositiveGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedTransactionItems.isNotEmpty()) {
                            Text(
                                "Produkter:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            selectedTransactionItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${item.quantity}x ${item.productName}")
                                    Text("${(item.quantity * item.unitPrice).toInt()} kr")
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Total",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${display.transaction.totalAmount.toInt()} kr",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else if (display.transaction.type == TransactionType.PAYMENT) {
                            Text(
                                "Betaling: ${display.transaction.totalAmount.toInt()} kr",
                                style = MaterialTheme.typography.titleMedium,
                                color = PositiveGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    display: TransactionDisplay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    display.memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    DateUtils.formatDateTime(display.transaction.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isPurchase = display.transaction.type == TransactionType.PURCHASE
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (isPurchase) "Køb" else "Betaling",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isPurchase) NegativeRed.copy(alpha = 0.1f)
                        else PositiveGreen.copy(alpha = 0.1f)
                    )
                )
                Text(
                    "${display.transaction.totalAmount.toInt()} kr",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPurchase) NegativeRed else PositiveGreen
                )
            }
        }
    }
}
