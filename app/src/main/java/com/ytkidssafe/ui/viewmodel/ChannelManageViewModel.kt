package com.ytkidssafe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.ChannelRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.Channel
import com.ytkidssafe.video.extractor.YouTubeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelManageViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository,
    private val youTubeService: YouTubeService
) : ViewModel() {

    companion object {
        private const val TAG = "ChannelManageVM"
    }

    val channels: StateFlow<List<Channel>> = channelRepository.getAllChannels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun addChannelFromUrl(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null

            Log.d(TAG, "Adding channel from URL: $url")

            val channelId = parseYoutubeChannelId(url)
            Log.d(TAG, "Parsed channel ID: $channelId")

            if (channelId != null) {
                youTubeService.fetchChannel(channelId)
                    .onSuccess { channelInfo ->
                        Log.d(TAG, "Fetched channel: ${channelInfo.name} with ${channelInfo.videos.size} videos")

                        // Add channel to database
                        val channel = channelRepository.addChannel(
                            youtubeId = channelInfo.id.ifEmpty { channelId },
                            title = channelInfo.name,
                            thumbnailUrl = channelInfo.thumbnailUrl,
                            category = "All"
                        )
                        Log.d(TAG, "Channel saved with internal ID: ${channel.id}")

                        // Add videos from channel
                        var addedCount = 0
                        for (video in channelInfo.videos) {
                            try {
                                videoRepository.addVideo(
                                    youtubeId = video.youtubeId,
                                    title = video.title,
                                    thumbnailUrl = video.thumbnailUrl,
                                    channelId = channel.id, // Use internal channel ID
                                    duration = video.duration
                                )
                                addedCount++
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to add video ${video.youtubeId}: ${e.message}")
                            }
                        }
                        Log.d(TAG, "Added $addedCount videos for channel ${channel.title}")
                        _successMessage.value = "Added ${channelInfo.name} with $addedCount videos"
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Failed to fetch channel: ${e.message}")
                        _error.value = e.message ?: "Failed to add channel"
                    }
            } else {
                Log.e(TAG, "Invalid channel URL: $url")
                _error.value = "Invalid channel URL. Try youtube.com/@channelname or youtube.com/channel/UCxxxxx"
            }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun deleteChannel(channel: Channel) {
        viewModelScope.launch {
            Log.d(TAG, "Deleting channel: ${channel.title}")
            // Delete associated videos first
            videoRepository.deleteVideosByChannel(channel.id)
            // Then delete channel
            channelRepository.deleteChannel(channel)
        }
    }

    fun assignChannelToProfile(channelId: String, profileId: String) {
        viewModelScope.launch {
            channelRepository.assignChannelToProfile(channelId, profileId)
        }
    }

    fun removeChannelFromProfile(channelId: String, profileId: String) {
        viewModelScope.launch {
            channelRepository.removeChannelFromProfile(channelId, profileId)
        }
    }

    private fun parseYoutubeChannelId(url: String): String? {
        val trimmed = url.trim()

        // Handle various YouTube channel URL formats:
        // - https://youtube.com/@username
        // - https://www.youtube.com/@username
        // - https://youtube.com/channel/UC...
        // - https://youtube.com/c/channelname
        // - @username (direct handle)
        // - UC... (direct channel ID)

        val patterns = listOf(
            Regex("youtube\\.com/@([\\w.-]+)"),
            Regex("youtube\\.com/channel/(UC[\\w-]+)"),
            Regex("youtube\\.com/c/([\\w.-]+)"),
            Regex("^@([\\w.-]+)$"),
            Regex("^(UC[\\w-]{22})$")
        )

        for (pattern in patterns) {
            pattern.find(trimmed)?.let { match ->
                val id = match.groupValues[1]
                Log.d(TAG, "Matched pattern: ${pattern.pattern}, extracted: $id")
                return id
            }
        }

        // If no pattern matches, use the input as-is (might be a channel name)
        return trimmed.takeIf { it.isNotBlank() && it.length > 2 }
    }
}
