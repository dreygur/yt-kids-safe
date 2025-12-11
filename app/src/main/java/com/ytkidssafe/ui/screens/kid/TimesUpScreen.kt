package com.ytkidssafe.ui.screens.kid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.ui.components.PinDialog
import com.ytkidssafe.ui.theme.Background
import com.ytkidssafe.ui.theme.Primary
import com.ytkidssafe.ui.theme.TextLight
import com.ytkidssafe.ui.viewmodel.ProfileSelectViewModel

@Composable
fun TimesUpScreen(
    profileId: String,
    onParentOverride: () -> Unit,
    onSwitchProfile: () -> Unit,
    viewModel: ProfileSelectViewModel = hiltViewModel()
) {
    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Background
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp)
        ) {
            // Moon/stars emoji
            Text(
                text = "🌙 ⭐ ✨",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Time to take a break!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You've watched enough videos for today.\nCome back tomorrow for more fun!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Switch profile button
            Button(
                onClick = onSwitchProfile,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.size(width = 200.dp, height = 48.dp)
            ) {
                Text("Switch Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parent override
            OutlinedButton(
                onClick = { showPinDialog = true },
                modifier = Modifier.size(width = 200.dp, height = 48.dp)
            ) {
                Text("Parent Override")
            }
        }
    }

    // PIN Dialog
    if (showPinDialog) {
        PinDialog(
            title = "Enter PIN",
            error = pinError,
            onPinEntered = { pin ->
                if (viewModel.verifyPin(pin)) {
                    showPinDialog = false
                    pinError = null
                    viewModel.addBonusTime(profileId, 30) // Add 30 minutes
                    onParentOverride()
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
