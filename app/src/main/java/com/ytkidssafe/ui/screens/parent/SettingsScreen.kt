package com.ytkidssafe.ui.screens.parent

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ProfileSelectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: ProfileSelectViewModel = hiltViewModel()
) {
    val defaultDailyLimit by viewModel.defaultDailyLimit.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var timeLimit by remember { mutableFloatStateOf(defaultDailyLimit.toFloat()) }

    Scaffold(
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
                        Column {
                            Text("Default Daily Limit", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${timeLimit.toInt()} minutes for new profiles",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextLight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = timeLimit,
                        onValueChange = { timeLimit = it },
                        valueRange = 15f..180f,
                        steps = 10,
                        onValueChangeFinished = {
                            viewModel.setDefaultDailyLimit(timeLimit.toInt())
                        }
                    )
                }
            }

            // Export Data
            SettingsCard(
                icon = Icons.Default.Upload,
                title = "Export Data",
                subtitle = "Backup profiles, channels, and settings",
                onClick = { /* TODO: Export */ }
            )

            // Import Data
            SettingsCard(
                icon = Icons.Default.Download,
                title = "Import Data",
                subtitle = "Restore from backup",
                onClick = { /* TODO: Import */ }
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
