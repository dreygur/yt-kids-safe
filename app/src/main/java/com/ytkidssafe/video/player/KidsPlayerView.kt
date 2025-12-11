package com.ytkidssafe.video.player

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.ytkidssafe.domain.model.TimeStatus
import com.ytkidssafe.ui.theme.OverlayDark
import com.ytkidssafe.video.extractor.StreamExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "KidsVideoPlayer"

sealed class PlayerState {
    data object Loading : PlayerState()
    data object NativePlayer : PlayerState()
    data object WebViewPlayer : PlayerState()
    data class Error(val message: String) : PlayerState()
}

/**
 * Hybrid video player for kids.
 * Tries native ExoPlayer first, falls back to WebView if stream extraction fails.
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(UnstableApi::class)
@Composable
fun KidsVideoPlayer(
    youtubeId: String,
    timeStatus: TimeStatus,
    onBack: () -> Unit,
    onTimeUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Loading) }

    val streamExtractor = remember { StreamExtractor() }

    // Audio manager for volume control
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var currentVolume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }

    // ExoPlayer instance (created but may not be used)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        Log.d(TAG, "ExoPlayer ready")
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "ExoPlayer error: ${error.message}, falling back to WebView")
                    playerState = PlayerState.WebViewPlayer
                }
            })
        }
    }

    // Extract streams using NewPipe, fall back to WebView on failure
    LaunchedEffect(youtubeId) {
        playerState = PlayerState.Loading
        Log.d(TAG, "Loading video: $youtubeId")

        val result = withContext(Dispatchers.IO) {
            streamExtractor.extractStreams(youtubeId)
        }

        result.onSuccess { info ->
            Log.d(TAG, "Got ${info.videoStreams.size} video streams, ${info.audioStreams.size} audio streams")

            // Try combined stream first (has both video and audio)
            val combinedStream = streamExtractor.getBestCombinedStream(info)
            if (combinedStream != null) {
                Log.d(TAG, "Using combined stream: ${combinedStream.quality} (${combinedStream.height}p)")
                exoPlayer.setMediaItem(MediaItem.fromUri(combinedStream.url))
                exoPlayer.prepare()
                playerState = PlayerState.NativePlayer
                return@onSuccess
            }

            // Try DASH (separate video + audio for better quality)
            val (video, audio) = streamExtractor.getBestDashStreams(info)
            if (video != null && audio != null) {
                Log.d(TAG, "Using DASH: video=${video.quality}, audio=${audio.averageBitrate}kbps")
                setupDashPlayback(exoPlayer, video.url, audio.url)
                playerState = PlayerState.NativePlayer
                return@onSuccess
            }

            // No streams available, fall back to WebView
            Log.w(TAG, "No usable streams found, using WebView")
            playerState = PlayerState.WebViewPlayer

        }.onFailure { error ->
            Log.w(TAG, "NewPipe extraction failed: ${error.message}, using WebView")
            playerState = PlayerState.WebViewPlayer
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing player")
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (playerState) {
            is PlayerState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }

            is PlayerState.NativePlayer -> {
                // ExoPlayer view
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            useController = true
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                            setShowFastForwardButton(false)
                            setShowRewindButton(false)
                            controllerShowTimeoutMs = 3000
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PlayerState.WebViewPlayer -> {
                // WebView fallback with YouTube embed
                WebViewPlayer(
                    youtubeId = youtubeId,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PlayerState.Error -> {
                Text(
                    text = (playerState as PlayerState.Error).message,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Back button (always visible)
        IconButton(
            onClick = {
                exoPlayer.stop()
                onBack()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(OverlayDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Time remaining
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(OverlayDark, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${timeStatus.formattedRemaining} left",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Volume controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    val newVolume = (currentVolume + 1).coerceAtMost(maxVolume.toFloat())
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.toInt(), 0)
                    currentVolume = newVolume
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(OverlayDark, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume Up",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    val newVolume = (currentVolume - 1).coerceAtLeast(0f)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume.toInt(), 0)
                    currentVolume = newVolume
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(48.dp)
                    .background(OverlayDark, CircleShape)
            ) {
                Icon(
                    imageVector = if (currentVolume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeDown,
                    contentDescription = "Volume Down",
                    tint = Color.White
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewPlayer(
    youtubeId: String,
    modifier: Modifier = Modifier
) {
    // Use mobile YouTube directly (not embed) to avoid error 152/153
    val videoUrl = "https://m.youtube.com/watch?v=$youtubeId"

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        val url = request?.url?.toString() ?: return false
                        // Allow YouTube and Google video URLs
                        if (url.contains("youtube.com") || url.contains("googlevideo.com") || url.contains("ytimg.com")) {
                            return false
                        }
                        return true // Block other URLs
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Hide distracting elements and try to auto-play
                        view?.evaluateJavascript("""
                            (function() {
                                var style = document.createElement('style');
                                style.innerHTML = `
                                    ytm-pivot-bar-renderer,
                                    ytm-comments-entry-point-header-renderer,
                                    ytm-item-section-renderer[section-identifier="comments-entry-point"],
                                    ytm-compact-video-renderer,
                                    ytm-rich-item-renderer,
                                    .related-chips-slot-wrapper,
                                    #related-chips { display: none !important; }
                                `;
                                document.head.appendChild(style);

                                setTimeout(function() {
                                    var video = document.querySelector('video');
                                    if (video) video.play();
                                }, 1500);
                            })();
                        """.trimIndent(), null)
                    }
                }

                loadUrl(videoUrl)
            }
        },
        modifier = modifier
    )
}

@OptIn(UnstableApi::class)
private fun setupDashPlayback(exoPlayer: ExoPlayer, videoUrl: String, audioUrl: String) {
    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0")
        .setConnectTimeoutMs(15000)
        .setReadTimeoutMs(15000)

    val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(videoUrl))

    val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory)
        .createMediaSource(MediaItem.fromUri(audioUrl))

    exoPlayer.setMediaSource(MergingMediaSource(videoSource, audioSource))
    exoPlayer.prepare()
}

@Composable
fun FullscreenVideoPlayer(
    youtubeId: String,
    timeStatus: TimeStatus,
    onBack: () -> Unit,
    onTimeUpdate: (Int) -> Unit
) {
    KidsVideoPlayer(
        youtubeId = youtubeId,
        timeStatus = timeStatus,
        onBack = onBack,
        onTimeUpdate = onTimeUpdate,
        modifier = Modifier.fillMaxSize()
    )
}
