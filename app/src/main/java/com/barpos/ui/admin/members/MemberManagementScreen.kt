@file:OptIn(ExperimentalMaterial3Api::class)

package com.barpos.ui.admin.members

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
import com.barpos.data.database.entity.Member
import com.barpos.ui.theme.NegativeRed
import com.barpos.ui.theme.PositiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(
    application: BarApplication,
    onNavigateBack: () -> Unit,
    viewModel: MemberManagementViewModel = viewModel(
        factory = MemberManagementViewModel.Factory(application)
    )
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMember by remember { mutableStateOf<Member?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brugerstyring") },
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Tilføj medlem") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "${members.size} medlemmer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(members, key = { it.id }) { member ->
                    MemberRow(
                        member = member,
                        onToggleActive = { viewModel.toggleMemberActive(member) },
                        onEdit = { editingMember = member }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMemberDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, certId ->
                viewModel.addMember(name, certId)
                showAddDialog = false
            }
        )
    }

    editingMember?.let { member ->
        EditMemberDialog(
            member = member,
            onDismiss = { editingMember = null },
            onSave = { updated ->
                viewModel.updateMember(updated)
                editingMember = null
            }
        )
    }
}

@Composable
private fun MemberRow(
    member: Member,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (member.isActive) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (member.isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    member.certificateId?.let {
                        Text(
                            "ID: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                "Saldo: ${member.balance.toInt()} kr",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (member.balance < 0) NegativeRed else PositiveGreen
            )

            Spacer(modifier = Modifier.width(12.dp))

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Rediger")
                }
                Switch(
                    checked = member.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }
        }
    }
}

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var certificateId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tilføj nyt medlem") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Navn *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = certificateId,
                    onValueChange = { certificateId = it },
                    label = { Text("Certifikat ID (valgfrit)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, certificateId.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) {
                Text("Tilføj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuller") }
        }
    )
}

@Composable
private fun EditMemberDialog(
    member: Member,
    onDismiss: () -> Unit,
    onSave: (Member) -> Unit
) {
    var name by remember { mutableStateOf(member.name) }
    var certificateId by remember { mutableStateOf(member.certificateId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rediger medlem") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Navn") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = certificateId,
                    onValueChange = { certificateId = it },
                    label = { Text("Certifikat ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(member.copy(
                        name = name.trim(),
                        certificateId = certificateId.trim().ifBlank { null }
                    ))
                },
                enabled = name.isNotBlank()
            ) {
                Text("Gem")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuller") }
        }
    )
}
