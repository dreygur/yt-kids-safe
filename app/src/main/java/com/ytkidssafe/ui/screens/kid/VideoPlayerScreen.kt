package com.ytkidssafe.ui.screens.kid

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ytkidssafe.ui.theme.Surface
import com.ytkidssafe.ui.viewmodel.VideoPlayerViewModel
import com.ytkidssafe.video.player.KidsVideoPlayer

@Composable
fun VideoPlayerScreen(
    profileId: String,
    videoId: String,
    onBack: () -> Unit,
    onTimeUp: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val video by viewModel.video.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val timeStatus by viewModel.timeStatus.collectAsState()
    val youtubeId by viewModel.youtubeId.collectAsState()

    // Force landscape orientation for video playback
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    LaunchedEffect(profileId, videoId) {
        viewModel.loadVideo(profileId, videoId)
    }

    LaunchedEffect(timeStatus) {
        if (timeStatus.isTimeUp) {
            viewModel.stopTracking()
            onTimeUp()
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.stopTracking()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Surface
            )
        } else if (youtubeId != null) {
            KidsVideoPlayer(
                youtubeId = youtubeId!!,
                timeStatus = timeStatus,
                onBack = {
                    viewModel.stopTracking()
                    onBack()
                },
                onTimeUpdate = { seconds ->
                    viewModel.updateElapsedTime(seconds)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
