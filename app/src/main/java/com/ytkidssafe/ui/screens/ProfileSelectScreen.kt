package com.ytkidssafe.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.domain.model.Profile
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.components.ProfileAvatar
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ProfileSelectViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ProfileSelectScreen(
    onProfileSelected: (String) -> Unit,
    onParentAccess: () -> Unit,
    onAddProfile: () -> Unit,
    autoSkipIfSingle: Boolean = true,
    viewModel: ProfileSelectViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val isPinSet by viewModel.isPinSet.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var showAddProfilePinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Auto-navigate to home if only one profile exists (only on initial launch)
    LaunchedEffect(isLoaded, profiles) {
        if (autoSkipIfSingle && isLoaded && profiles.size == 1) {
            onProfileSelected(profiles.first().id)
        }
    }

    // Don't show UI while checking or if auto-navigating
    if (!isLoaded || (autoSkipIfSingle && profiles.size == 1)) {
        return
    }

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
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    }
                } else {
                    // Random bubble layout from center
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val bubblePositions = remember(profiles.size) {
                            generateBubblePositions(profiles.size)
                        }
                        profiles.forEachIndexed { index, profile ->
                            val (offsetX, offsetY) = bubblePositions.getOrElse(index) { Pair(0.dp, 0.dp) }
                            Box(
                                modifier = Modifier.offset(x = offsetX, y = offsetY)
                            ) {
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
                onClick = { showAddProfilePinDialog = true },
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

    // PIN Dialog for settings access
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

    // PIN Dialog for add profile
    if (showAddProfilePinDialog) {
        if (isPinSet) {
            PinDialog(
                title = "Enter PIN",
                error = pinError,
                onPinEntered = { pin ->
                    if (viewModel.verifyPin(pin)) {
                        showAddProfilePinDialog = false
                        pinError = null
                        onAddProfile()
                    } else {
                        pinError = "Incorrect PIN"
                    }
                },
                onDismiss = {
                    showAddProfilePinDialog = false
                    pinError = null
                }
            )
        } else {
            PinDialog(
                title = "Set up PIN",
                isSetup = true,
                onPinEntered = { pin ->
                    viewModel.setPin(pin)
                    showAddProfilePinDialog = false
                    onAddProfile()
                },
                onDismiss = { showAddProfilePinDialog = false }
            )
        }
    }
}

private fun generateBubblePositions(count: Int): List<Pair<Dp, Dp>> {
    if (count == 0) return emptyList()
    if (count == 1) return listOf(Pair(0.dp, 0.dp))

    val positions = mutableListOf<Pair<Dp, Dp>>()
    val random = Random(count) // Seeded for consistency
    val baseRadius = 80f

    for (i in 0 until count) {
        val angle = (2 * Math.PI * i / count) + random.nextDouble(-0.3, 0.3)
        val radius = baseRadius + random.nextFloat() * 40
        val x = (cos(angle) * radius).toFloat()
        val y = (sin(angle) * radius).toFloat()
        positions.add(Pair(x.dp, y.dp))
    }
    return positions
}
