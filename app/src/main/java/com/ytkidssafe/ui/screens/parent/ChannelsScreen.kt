package com.ytkidssafe.ui.screens.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ytkidssafe.domain.model.Channel
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ChannelManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    onBack: () -> Unit,
    viewModel: ChannelManageViewModel = hiltViewModel()
) {
    val channels by viewModel.channels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deleteChannel by remember { mutableStateOf<Channel?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show error in snackbar
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    // Show success in snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSuccessMessage()
            }
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Manage Channels") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, "Add Channel")
                }
            }
        }
    ) { padding ->
        if (channels.isEmpty() && !isLoading) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No channels added yet",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to add a YouTube channel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextLight
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(channels) { channel ->
                    ChannelCard(
                        channel = channel,
                        onDelete = { deleteChannel = channel }
                    )
                }
            }
        }
    }

    // Add Channel Dialog
    if (showAddDialog) {
        AddChannelDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { url, category ->
                viewModel.addChannelFromUrl(url, category)
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation
    if (deleteChannel != null) {
        AlertDialog(
            onDismissRequest = { deleteChannel = null },
            title = { Text("Delete Channel?") },
            text = { Text("Remove ${deleteChannel!!.title} from the approved channels?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChannel(deleteChannel!!)
                        deleteChannel = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteChannel = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChannelCard(
    channel: Channel,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.thumbnailUrl,
                contentDescription = channel.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(channel.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    channel.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChannelDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("All", "Cartoons", "Learning", "Music", "Stories")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Channel") },
        text = {
            Column {
                Text(
                    "Paste a YouTube channel URL or ID",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Channel URL") },
                    placeholder = { Text("youtube.com/@channelname") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Category",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(url, selectedCategory) },
                enabled = url.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
