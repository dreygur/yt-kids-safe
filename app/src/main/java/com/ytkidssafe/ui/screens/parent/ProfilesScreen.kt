package com.ytkidssafe.ui.screens.parent

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.domain.model.Avatars
import com.ytkidssafe.domain.model.Profile
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
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var deleteProfile by remember { mutableStateOf<Profile?>(null) }

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No profiles yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to create a profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextLight
                    )
                }
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
            onDismiss = {
                showAddDialog = false
                editingProfile = null
            },
            onSave = { name, avatar, dailyLimit ->
                if (editingProfile != null) {
                    viewModel.updateProfile(editingProfile!!.copy(
                        name = name,
                        avatar = avatar,
                        dailyLimitMinutes = dailyLimit
                    ))
                } else {
                    viewModel.createProfile(name, avatar, dailyLimit)
                }
                showAddDialog = false
                editingProfile = null
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
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var selectedAvatar by remember { mutableStateOf(profile?.avatar ?: "bear") }
    var dailyLimitText by remember {
        mutableStateOf((profile?.dailyLimitMinutes ?: defaultDailyLimit).toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Add Profile" else "Edit Profile") },
        text = {
            Column {
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
                        // Only allow digits, max 1440
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = dailyLimitText.toIntOrNull()?.coerceIn(1, 1440) ?: defaultDailyLimit
                    onSave(name, selectedAvatar, limit)
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
