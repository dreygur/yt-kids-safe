package com.ytkidssafe.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.Surface
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.theme.TextPrimary
import com.ytkidssafe.ui.viewmodel.PlaylistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onBack: () -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val importSuccess by viewModel.importSuccess.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(importSuccess) {
        if (importSuccess) {
            snackbarHostState.showSnackbar("Playlist imported successfully!")
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Import Playlists") },
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
                Icon(Icons.Default.Add, "Import Playlist")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (playlists.isEmpty() && !isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = TextLight.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No playlists imported yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap + to import a YouTube playlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onDelete = { viewModel.deletePlaylist(playlist) }
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }

    // Import Playlist Dialog
    if (showAddDialog) {
        var url by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("All") }
        var expanded by remember { mutableStateOf(false) }
        val categories = listOf("All", "Cartoons", "Learning", "Music", "Stories")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Import Playlist") },
            text = {
                Column {
                    Text(
                        "Paste a YouTube playlist URL",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Playlist URL") },
                        placeholder = { Text("youtube.com/playlist?list=...") },
                        singleLine = true,
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
                    onClick = {
                        viewModel.importPlaylist(url, selectedCategory)
                        showAddDialog = false
                    },
                    enabled = url.isNotBlank()
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Thumbnail
            if (playlist.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = playlist.thumbnailUrl,
                    contentDescription = playlist.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp, 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp, 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    maxLines = 2
                )
                if (playlist.videoCount > 0) {
                    Text(
                        text = "${playlist.videoCount} videos",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                }
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextLight
                )
            }
        }
    }
}
