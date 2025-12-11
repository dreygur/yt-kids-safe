package com.ytkidssafe.ui.screens.parent

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val defaultDailyLimit by viewModel.defaultDailyLimit.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var timeLimitText by remember { mutableStateOf(defaultDailyLimit.toString()) }

    // Sync timeLimitText when defaultDailyLimit loads from DataStore
    LaunchedEffect(defaultDailyLimit) {
        timeLimitText = defaultDailyLimit.toString()
    }

    // File picker for export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    // Handle export result with Snackbar
    LaunchedEffect(exportResult) {
        exportResult?.let { result ->
            result.onSuccess {
                snackbarHostState.showSnackbar("Backup exported successfully")
            }.onFailure { e ->
                snackbarHostState.showSnackbar("Export failed: ${e.message}")
            }
            viewModel.clearExportResult()
        }
    }

    // Handle import result with Snackbar
    LaunchedEffect(importResult) {
        importResult?.let { result ->
            result.onSuccess { count ->
                snackbarHostState.showSnackbar("Imported $count items successfully")
            }.onFailure { e ->
                snackbarHostState.showSnackbar("Import failed: ${e.message}")
            }
            viewModel.clearImportResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Manage Categories
            SettingsCard(
                icon = Icons.AutoMirrored.Filled.Label,
                title = "Manage Categories",
                subtitle = "Add or remove content categories",
                onClick = onNavigateToCategories
            )

            // Change PIN
            SettingsCard(
                icon = Icons.Default.Lock,
                title = "Change PIN",
                subtitle = "Update your parent PIN",
                onClick = { showPinDialog = true }
            )

            // Default Daily Limit
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Default Daily Limit", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = timeLimitText,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            val value = filtered.toIntOrNull() ?: 0
                            timeLimitText = if (value > 1440) "1440" else filtered
                            // Save when valid
                            filtered.toIntOrNull()?.let { mins ->
                                if (mins in 1..1440) {
                                    viewModel.setDefaultDailyLimit(mins)
                                }
                            }
                        },
                        label = { Text("Minutes") },
                        supportingText = { Text("For new profiles (max: 1440 min / 24 hrs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Export Data
            SettingsCard(
                icon = Icons.Default.Upload,
                title = "Export Data",
                subtitle = "Backup profiles, channels, and settings",
                onClick = {
                    val timestamp = System.currentTimeMillis()
                    exportLauncher.launch("ytkids_backup_$timestamp.json")
                }
            )

            // Import Data
            SettingsCard(
                icon = Icons.Default.Download,
                title = "Import Data",
                subtitle = "Restore from backup",
                onClick = {
                    importLauncher.launch(arrayOf("application/json"))
                }
            )
        }
    }

    // PIN Dialog
    if (showPinDialog) {
        PinDialog(
            title = "Set New PIN",
            isSetup = true,
            onPinEntered = { pin ->
                viewModel.setPin(pin)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextLight)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextLight
            )
        }
    }
}
