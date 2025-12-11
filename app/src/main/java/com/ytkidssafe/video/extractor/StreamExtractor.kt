package com.ytkidssafe.video.extractor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts video streams from YouTube using NewPipe Extractor.
 * This is the same approach used by NewPipe, SkyTube, and other YouTube clients.
 */
@Singleton
class StreamExtractor @Inject constructor() {

    companion object {
        private const val TAG = "StreamExtractor"
        private var initialized = false
    }

    data class ExtractedStreams(
        val title: String,
        val duration: Long,
        val thumbnailUrl: String,
        val uploaderName: String,
        val videoStreams: List<ExtractedVideoStream>,
        val audioStreams: List<ExtractedAudioStream>
    )

    data class ExtractedVideoStream(
        val url: String,
        val quality: String,
        val format: String,
        val width: Int,
        val height: Int,
        val isVideoOnly: Boolean
    )

    data class ExtractedAudioStream(
        val url: String,
        val averageBitrate: Int,
        val format: String
    )

    init {
        initNewPipe()
    }

    private fun initNewPipe() {
        if (!initialized) {
            try {
                NewPipe.init(NewPipeDownloader.getInstance())
                initialized = true
                Log.d(TAG, "NewPipe Extractor initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize NewPipe: ${e.message}")
            }
        }
    }

    /**
     * Extract streams for a YouTube video using NewPipe Extractor.
     */
    suspend fun extractStreams(youtubeId: String): Result<ExtractedStreams> = withContext(Dispatchers.IO) {
        try {
            initNewPipe()

            val url = "https://www.youtube.com/watch?v=$youtubeId"
            Log.d(TAG, "Extracting streams for: $url")

            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, url)

            val videoStreams = streamInfo.videoStreams
                .filter { !it.content.isNullOrBlank() }
                .map { stream ->
                    ExtractedVideoStream(
                        url = stream.content ?: "",
                        quality = stream.getResolution() ?: "unknown",
                        format = stream.format?.name ?: "unknown",
                        width = stream.width,
                        height = stream.height,
                        isVideoOnly = false
                    )
                }
                .sortedByDescending { it.height }

            val videoOnlyStreams = streamInfo.videoOnlyStreams
                .filter { !it.content.isNullOrBlank() }
                .map { stream ->
                    ExtractedVideoStream(
                        url = stream.content ?: "",
                        quality = stream.getResolution() ?: "unknown",
                        format = stream.format?.name ?: "unknown",
                        width = stream.width,
                        height = stream.height,
                        isVideoOnly = true
                    )
                }
                .sortedByDescending { it.height }

            val audioStreams = streamInfo.audioStreams
                .filter { !it.content.isNullOrBlank() }
                .map { stream ->
                    ExtractedAudioStream(
                        url = stream.content ?: "",
                        averageBitrate = stream.averageBitrate,
                        format = stream.format?.name ?: "unknown"
                    )
                }
                .sortedByDescending { it.averageBitrate }

            val allVideoStreams = videoStreams + videoOnlyStreams

            Log.d(TAG, "Extracted ${videoStreams.size} video, ${videoOnlyStreams.size} video-only, ${audioStreams.size} audio streams")

            // Get thumbnail from thumbnails list or generate default
            val thumbnail = streamInfo.thumbnails.firstOrNull()?.url ?: getThumbnailUrl(youtubeId)

            Result.success(ExtractedStreams(
                title = streamInfo.name ?: "Video",
                duration = streamInfo.duration,
                thumbnailUrl = thumbnail,
                uploaderName = streamInfo.uploaderName ?: "Unknown",
                videoStreams = allVideoStreams,
                audioStreams = audioStreams
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Stream extraction failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get best video stream with audio (combined stream for simple playback)
     */
    fun getBestCombinedStream(info: ExtractedStreams): ExtractedVideoStream? {
        val withAudio = info.videoStreams.filter { !it.isVideoOnly }
        if (withAudio.isEmpty()) return null

        // Prefer 720p or lower for mobile
        return withAudio.find { it.height in 360..720 }
            ?: withAudio.find { it.height <= 720 }
            ?: withAudio.lastOrNull()
    }

    /**
     * Get video-only + audio streams for DASH playback (better quality)
     */
    fun getBestDashStreams(info: ExtractedStreams): Pair<ExtractedVideoStream?, ExtractedAudioStream?> {
        val videoOnly = info.videoStreams.filter { it.isVideoOnly }
        val video = videoOnly.find { it.height in 360..720 }
            ?: videoOnly.find { it.height <= 1080 }
            ?: videoOnly.firstOrNull()

        val audio = info.audioStreams.firstOrNull()
        return Pair(video, audio)
    }

    fun getThumbnailUrl(youtubeId: String): String {
        return "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"
    }
}
