package com.ytkidssafe.ui.screens.kid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
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
import com.ytkidssafe.domain.model.TimeStatus
import com.ytkidssafe.ui.components.BottomNavBar
import com.ytkidssafe.ui.components.CategoryPills
import com.ytkidssafe.ui.components.NavItem
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.components.TimeBar
import com.ytkidssafe.ui.components.VideoCard
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.viewmodel.KidHomeViewModel

@Composable
fun KidHomeScreen(
    profileId: String,
    onVideoClick: (String) -> Unit,
    onChannelsClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
    onSwitchProfile: () -> Unit,
    onParentAccess: () -> Unit,
    onTimeUp: () -> Unit,
    viewModel: KidHomeViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val timeStatus by viewModel.timeStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var currentNav by remember { mutableStateOf(NavItem.HOME) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(profileId) {
        viewModel.loadProfile(profileId)
    }

    LaunchedEffect(timeStatus) {
        if (timeStatus.isTimeUp) {
            onTimeUp()
        }
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            BottomNavBar(
                currentItem = currentNav,
                onItemSelected = { item ->
                    currentNav = item
                    when (item) {
                        NavItem.HOME -> { /* Already here */ }
                        NavItem.CHANNELS -> onChannelsClick()
                        NavItem.PLAYLISTS -> onPlaylistsClick()
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
            // Header with greeting and time
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hi, ${profile?.name ?: "there"}! 👋",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                TimeBar(timeStatus = timeStatus)

                // Subtle hint for parents
                Text(
                    text = "Parent? Long-press navbar for settings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Category pills
            CategoryPills(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Videos list
            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos yet!\nAsk a parent to add channels.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(videos) { video ->
                        val channelName = channels.find { it.id == video.channelId }?.title ?: ""
                        VideoCard(
                            video = video,
                            channelName = channelName,
                            onClick = { onVideoClick(video.id) }
                        )
                    }
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
