package com.ytkidssafe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.domain.model.Profile
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.components.ProfileAvatar
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ProfileSelectViewModel

@Composable
fun ProfileSelectScreen(
    onProfileSelected: (String) -> Unit,
    onParentAccess: () -> Unit,
    viewModel: ProfileSelectViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val isPinSet by viewModel.isPinSet.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // App Title
                Text(
                    text = "Who's watching?",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (profiles.isEmpty()) {
                    // No profiles yet
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No profiles yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextLight
                        )
                        Text(
                            text = "Tap + to add a profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextLight
                        )
                    }
                } else {
                    // Profile grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(profiles) { profile ->
                            ProfileAvatar(
                                avatar = profile.avatar,
                                name = profile.name,
                                size = 100.dp,
                                onClick = {
                                    viewModel.selectProfile(profile.id)
                                    onProfileSelected(profile.id)
                                }
                            )
                        }
                    }
                }
            }

            // Settings button - tap to access parent dashboard
            IconButton(
                onClick = { showPinDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextLight,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Add profile FAB
            FloatingActionButton(
                onClick = { showPinDialog = true },
                containerColor = Primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Profile"
                )
            }
        }
    }

    // PIN Dialog
    if (showPinDialog) {
        if (isPinSet) {
            PinDialog(
                title = "Enter PIN",
                error = pinError,
                onPinEntered = { pin ->
                    if (viewModel.verifyPin(pin)) {
                        showPinDialog = false
                        pinError = null
                        onParentAccess()
                    } else {
                        pinError = "Incorrect PIN"
                    }
                },
                onDismiss = {
                    showPinDialog = false
                    pinError = null
                }
            )
        } else {
            PinDialog(
                title = "Set up PIN",
                isSetup = true,
                onPinEntered = { pin ->
                    viewModel.setPin(pin)
                    showPinDialog = false
                    onParentAccess()
                },
                onDismiss = { showPinDialog = false }
            )
        }
    }
}
