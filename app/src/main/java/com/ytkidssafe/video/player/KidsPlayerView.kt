package com.ytkidssafe.video.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import android.view.LayoutInflater
import com.ytkidssafe.R
import com.ytkidssafe.domain.model.TimeStatus
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
    onControlsVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Loading) }
    val window = context.findActivity()?.window

    val streamExtractor = remember { StreamExtractor() }
    var isPlaying by remember { mutableStateOf(false) }

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

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
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

    LaunchedEffect(playerState, isPlaying) {
        val keepOn = when (playerState) {
            is PlayerState.NativePlayer -> isPlaying
            is PlayerState.WebViewPlayer -> true
            else -> false
        }
        if (keepOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(window) {
        onDispose {
            Log.d(TAG, "Disposing player")
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                // ExoPlayer view with custom large play button
                AndroidView(
                    factory = { ctx ->
                        (LayoutInflater.from(ctx).inflate(R.layout.kids_player_view, null) as PlayerView).apply {
                            player = exoPlayer
                            setControllerVisibilityListener(
                                PlayerView.ControllerVisibilityListener { visibility ->
                                    onControlsVisibilityChanged(visibility == android.view.View.VISIBLE)
                                }
                            )
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

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
