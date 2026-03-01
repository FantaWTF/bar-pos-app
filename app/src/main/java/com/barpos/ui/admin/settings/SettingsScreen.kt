@file:OptIn(ExperimentalMaterial3Api::class)

package com.barpos.ui.admin.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.barpos.BarApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    application: BarApplication,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(application))
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var barName by remember(settings) { mutableStateOf(settings?.barName ?: "") }
    var mobilePayNumber by remember(settings) { mutableStateOf(settings?.mobilepayNumber ?: "") }
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            showSuccessSnackbar = true
            viewModel.clearSaveSuccess()
            currentPin = ""
            newPin = ""
            confirmPin = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Indstillinger") },
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
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSuccessSnackbar = false }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text("Indstillinger gemt!")
                }
                LaunchedEffect(showSuccessSnackbar) {
                    kotlinx.coroutines.delay(3000)
                    showSuccessSnackbar = false
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Bar name
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Bar-navn",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Navnet vises i app-headeren og som kommentar i MobilePay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = barName,
                                onValueChange = { barName = it },
                                label = { Text("Bar-navn") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = { viewModel.updateBarName(barName) },
                                enabled = barName.isNotBlank()
                            ) {
                                Text("Gem")
                            }
                        }
                    }
                }

                // MobilePay
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "MobilePay",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Telefonnummeret der bruges til MobilePay QR-koden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = mobilePayNumber,
                                onValueChange = { mobilePayNumber = it },
                                label = { Text("Telefonnummer") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                            )
                            Button(
                                onClick = { viewModel.updateMobilePayNumber(mobilePayNumber) },
                                enabled = mobilePayNumber.isNotBlank()
                            ) {
                                Text("Gem")
                            }
                        }
                    }
                }
            }

            // Right column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Change PIN
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Skift PIN-kode",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = currentPin,
                            onValueChange = {
                                currentPin = it
                                pinError = null
                            },
                            label = { Text("Nuværende PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = {
                                newPin = it
                                pinError = null
                            },
                            label = { Text("Ny PIN (min. 4 cifre)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = {
                                confirmPin = it
                                pinError = null
                            },
                            label = { Text("Bekræft ny PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError != null,
                            supportingText = pinError?.let { { Text(it) } }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                when {
                                    currentPin != settings?.adminPin -> pinError = "Forkert nuværende PIN"
                                    newPin.length < 4 -> pinError = "PIN skal være mindst 4 cifre"
                                    newPin != confirmPin -> pinError = "De to PIN-koder matcher ikke"
                                    else -> viewModel.updatePin(newPin)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = currentPin.isNotBlank() && newPin.isNotBlank() && confirmPin.isNotBlank()
                        ) {
                            Text("Skift PIN-kode")
                        }
                    }
                }

                // Export
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Eksporter data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Eksporter alle transaktioner som CSV-fil",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.exportCsv(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eksporter CSV")
                        }
                    }
                }
            }
        }
    }
}
