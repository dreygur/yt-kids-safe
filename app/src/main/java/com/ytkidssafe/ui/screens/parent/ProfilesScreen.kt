package com.ytkidssafe.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ytkidssafe.domain.model.Avatars
import com.ytkidssafe.domain.model.Categories
import com.ytkidssafe.domain.model.Channel
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.domain.model.Profile
import kotlinx.coroutines.launch
import com.ytkidssafe.ui.components.ProfileAvatar
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ProfileSelectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onBack: () -> Unit,
    viewModel: ProfileSelectViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val defaultDailyLimit by viewModel.defaultDailyLimit.collectAsState()
    val allChannels by viewModel.allChannels.collectAsState()
    val allPlaylists by viewModel.allPlaylists.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var deleteProfile by remember { mutableStateOf<Profile?>(null) }

    // For loading assigned content when editing
    var editingChannelIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var editingPlaylistIds by remember { mutableStateOf<List<String>>(emptyList()) }

    // Load assigned content when editing a profile
    LaunchedEffect(editingProfile) {
        editingProfile?.let { profile ->
            editingChannelIds = viewModel.getAssignedChannelIds(profile.id)
            editingPlaylistIds = viewModel.getAssignedPlaylistIds(profile.id)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Manage Profiles") },
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
                Icon(Icons.Default.Add, "Add Profile")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = TextLight.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No profiles yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLight
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap + to create a profile",
                    style = MaterialTheme.typography.bodySmall,
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
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onEdit = { editingProfile = profile },
                        onDelete = { deleteProfile = profile }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingProfile != null) {
        ProfileDialog(
            profile = editingProfile,
            defaultDailyLimit = defaultDailyLimit,
            allChannels = allChannels,
            allPlaylists = allPlaylists,
            initialChannelIds = if (editingProfile != null) editingChannelIds else emptyList(),
            initialPlaylistIds = if (editingProfile != null) editingPlaylistIds else emptyList(),
            onDismiss = {
                showAddDialog = false
                editingProfile = null
                editingChannelIds = emptyList()
                editingPlaylistIds = emptyList()
            },
            onSave = { name, avatar, dailyLimit, categoryFilters, channelIds, playlistIds ->
                if (editingProfile != null) {
                    viewModel.updateProfile(
                        editingProfile!!.copy(
                            name = name,
                            avatar = avatar,
                            dailyLimitMinutes = dailyLimit,
                            categoryFilters = categoryFilters
                        ),
                        channelIds,
                        playlistIds
                    )
                } else {
                    viewModel.createProfile(name, avatar, dailyLimit, categoryFilters, channelIds, playlistIds)
                }
                showAddDialog = false
                editingProfile = null
                editingChannelIds = emptyList()
                editingPlaylistIds = emptyList()
            }
        )
    }

    // Delete Confirmation
    if (deleteProfile != null) {
        AlertDialog(
            onDismissRequest = { deleteProfile = null },
            title = { Text("Delete Profile?") },
            text = { Text("Are you sure you want to delete ${deleteProfile!!.name}'s profile?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProfile(deleteProfile!!)
                        deleteProfile = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteProfile = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    onEdit: () -> Unit,
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
            ProfileAvatar(
                avatar = profile.avatar,
                name = "",
                size = 56.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${profile.dailyLimitMinutes} min/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLight
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = Primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ProfileDialog(
    profile: Profile?,
    defaultDailyLimit: Int,
    allChannels: List<Channel>,
    allPlaylists: List<Playlist>,
    initialChannelIds: List<String>,
    initialPlaylistIds: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, List<String>, List<String>, List<String>) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedAvatar by remember { mutableStateOf(profile?.avatar ?: "bear") }
    var dailyLimitText by remember {
        mutableStateOf((profile?.dailyLimitMinutes ?: defaultDailyLimit).toString())
    }
    val availableCategories = Categories.all.filter { it != "All" }
    var selectedCategories by remember {
        mutableStateOf(profile?.categoryFilters ?: emptyList())
    }
    var selectedChannelIds by remember { mutableStateOf(initialChannelIds) }
    var selectedPlaylistIds by remember { mutableStateOf(initialPlaylistIds) }

    // Update when initial values change (async loading)
    LaunchedEffect(initialChannelIds) {
        selectedChannelIds = initialChannelIds
    }
    LaunchedEffect(initialPlaylistIds) {
        selectedPlaylistIds = initialPlaylistIds
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Add Profile" else "Edit Profile") },
        text = {
            val verticalScrollState = rememberScrollState()
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(verticalScrollState)
                ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Avatar", style = MaterialTheme.typography.bodyLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Avatars.all) { (key, _) ->
                        ProfileAvatar(
                            avatar = key,
                            name = "",
                            size = 48.dp,
                            selected = key == selectedAvatar,
                            onClick = { selectedAvatar = key }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dailyLimitText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        val value = filtered.toIntOrNull() ?: 0
                        dailyLimitText = if (value > 1440) "1440" else filtered
                    },
                    label = { Text("Daily Limit (minutes)") },
                    supportingText = { Text("Max: 1440 minutes (24 hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Assigned Channels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assigned Channels", style = MaterialTheme.typography.bodyLarge)
                    if (allChannels.size > 3) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Swipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLight
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (allChannels.isEmpty()) {
                    Text(
                        "No channels added yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                } else {
                    Text(
                        "Tap to select/deselect",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val channelScrollState = rememberLazyListState()
                    Box {
                        LazyRow(
                            state = channelScrollState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(allChannels) { channel ->
                                SelectableContentItem(
                                    thumbnailUrl = channel.thumbnailUrl,
                                    title = channel.title,
                                    isSelected = selectedChannelIds.contains(channel.id),
                                    isCircle = true,
                                    onClick = {
                                        selectedChannelIds = if (selectedChannelIds.contains(channel.id)) {
                                            selectedChannelIds - channel.id
                                        } else {
                                            selectedChannelIds + channel.id
                                        }
                                    }
                                )
                            }
                        }
                        // Fade indicator on right if more content
                        if (channelScrollState.canScrollForward) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(32.dp)
                                    .height(100.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surface,
                                                androidx.compose.ui.graphics.Color.Transparent
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 50f),
                                            radius = 150f
                                        )
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Assigned Playlists
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Assigned Playlists", style = MaterialTheme.typography.bodyLarge)
                    if (allPlaylists.size > 3) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Swipe",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextLight
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (allPlaylists.isEmpty()) {
                    Text(
                        "No playlists imported yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                } else {
                    Text(
                        "Tap to select/deselect",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val playlistScrollState = rememberLazyListState()
                    Box {
                        LazyRow(
                            state = playlistScrollState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(allPlaylists) { playlist ->
                                SelectableContentItem(
                                    thumbnailUrl = playlist.thumbnailUrl,
                                    title = playlist.title,
                                    isSelected = selectedPlaylistIds.contains(playlist.id),
                                    isCircle = false,
                                    onClick = {
                                        selectedPlaylistIds = if (selectedPlaylistIds.contains(playlist.id)) {
                                            selectedPlaylistIds - playlist.id
                                        } else {
                                            selectedPlaylistIds + playlist.id
                                        }
                                    }
                                )
                            }
                        }
                        // Fade indicator on right if more content
                        if (playlistScrollState.canScrollForward) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .width(32.dp)
                                    .height(100.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surface,
                                                androidx.compose.ui.graphics.Color.Transparent
                                            ),
                                            center = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, 50f),
                                            radius = 150f
                                        )
                                    )
                            )
                        }
                    }
                }
                }
                // Vertical scroll indicator at bottom
                if (verticalScrollState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(32.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(0.5f, Float.POSITIVE_INFINITY),
                                    radius = 200f
                                )
                            )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = dailyLimitText.toIntOrNull()?.coerceIn(1, 1440) ?: defaultDailyLimit
                    onSave(name, selectedAvatar, limit, selectedCategories, selectedChannelIds, selectedPlaylistIds)
                },
                enabled = name.isNotBlank() && dailyLimitText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SelectableContentItem(
    thumbnailUrl: String,
    title: String,
    isSelected: Boolean,
    isCircle: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(if (isCircle) CircleShape else RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) Modifier.border(
                            3.dp,
                            Primary,
                            if (isCircle) CircleShape else RoundedCornerShape(8.dp)
                        ) else Modifier
                    )
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) Primary else TextLight
        )
    }
}
