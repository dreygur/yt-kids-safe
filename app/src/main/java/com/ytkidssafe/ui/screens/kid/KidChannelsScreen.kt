package com.ytkidssafe.ui.screens.kid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.ui.components.BottomNavBar
import com.ytkidssafe.ui.components.ChannelTile
import com.ytkidssafe.ui.components.NavItem
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.components.TimeBar
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.viewmodel.KidHomeViewModel

@Composable
fun KidChannelsScreen(
    profileId: String,
    onChannelClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onSwitchProfile: () -> Unit,
    onParentAccess: () -> Unit,
    viewModel: KidHomeViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val timeStatus by viewModel.timeStatus.collectAsState()

    var currentNav by remember { mutableStateOf(NavItem.CHANNELS) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                currentItem = currentNav,
                onItemSelected = { item ->
                    currentNav = item
                    when (item) {
                        NavItem.HOME -> onHomeClick()
                        NavItem.CHANNELS -> { /* Already here */ }
                        NavItem.PROFILE -> onSwitchProfile()
                        NavItem.SETTINGS -> { }
                    }
                },
                onSettingsLongPress = { showPinDialog = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Channels",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                TimeBar(timeStatus = timeStatus)
            }

            // Channels grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(channels) { channel ->
                    ChannelTile(
                        channel = channel,
                        onClick = { onChannelClick(channel.id) }
                    )
                }
            }
        }
    }

    // PIN Dialog for parent access
    if (showPinDialog) {
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
    }
}
