package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.ProfileRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.TimeStatus
import com.ytkidssafe.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _video = MutableStateFlow<Video?>(null)
    val video: StateFlow<Video?> = _video.asStateFlow()

    private val _relatedVideos = MutableStateFlow<List<Video>>(emptyList())
    val relatedVideos: StateFlow<List<Video>> = _relatedVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _timeStatus = MutableStateFlow(TimeStatus(60, 60, 0))
    val timeStatus: StateFlow<TimeStatus> = _timeStatus.asStateFlow()

    private val _youtubeId = MutableStateFlow<String?>(null)
    val youtubeId: StateFlow<String?> = _youtubeId.asStateFlow()

    private var trackingJob: Job? = null
    private var currentProfileId: String? = null
    private var elapsedSeconds = 0
    private var lastSavedMinute = 0
    private val videoHistory = mutableListOf<Video>()

    fun loadVideo(profileId: String, videoId: String) {
        currentProfileId = profileId
        _isLoading.value = true
        elapsedSeconds = 0
        lastSavedMinute = 0

        viewModelScope.launch {
            // Load video details
            val videoData = videoRepository.getVideoById(videoId)
            _video.value = videoData

            // Load profile time status
            val profile = profileRepository.getProfileById(profileId)
            profile?.let {
                _timeStatus.value = TimeStatus(
                    remainingMinutes = it.remainingMinutes,
                    totalMinutes = it.dailyLimitMinutes,
                    usedMinutes = it.usedTodayMinutes
                )
            }

            // Set YouTube ID for the native player
            videoData?.let { video ->
                _youtubeId.value = video.youtubeId

                // Load related videos - try same channel first, then all videos
                val channelVideos = video.channelId?.let { channelId ->
                    videoRepository.getVideosByChannel(channelId).first()
                } ?: emptyList()

                if (channelVideos.size > 1) {
                    // Has other videos from same channel
                    _relatedVideos.value = channelVideos
                        .filter { it.id != videoId }
                        .take(10)
                } else {
                    // Fallback: show all available videos
                    val allVideos = videoRepository.getAllVideos().first()
                    _relatedVideos.value = allVideos
                        .filter { it.id != videoId }
                        .shuffled()
                        .take(10)
                }
            }

            _isLoading.value = false
            startTracking()
        }
    }

    fun playVideo(video: Video) {
        currentProfileId?.let {
            // Save current video to history before switching
            _video.value?.let { current -> videoHistory.add(current) }

            _video.value = video
            _youtubeId.value = video.youtubeId
            elapsedSeconds = 0
            lastSavedMinute = 0

            // Update related videos
            viewModelScope.launch {
                val channelVideos = video.channelId?.let { channelId ->
                    videoRepository.getVideosByChannel(channelId).first()
                } ?: emptyList()

                if (channelVideos.size > 1) {
                    _relatedVideos.value = channelVideos
                        .filter { it.id != video.id }
                        .take(10)
                } else {
                    val allVideos = videoRepository.getAllVideos().first()
                    _relatedVideos.value = allVideos
                        .filter { it.id != video.id }
                        .shuffled()
                        .take(10)
                }
            }
        }
    }

    fun playPreviousVideo() {
        if (videoHistory.isNotEmpty()) {
            val previousVideo = videoHistory.removeAt(videoHistory.lastIndex)
            _video.value = previousVideo
            _youtubeId.value = previousVideo.youtubeId
            elapsedSeconds = 0
            lastSavedMinute = 0

            viewModelScope.launch {
                val channelVideos = previousVideo.channelId?.let { channelId ->
                    videoRepository.getVideosByChannel(channelId).first()
                } ?: emptyList()

                if (channelVideos.size > 1) {
                    _relatedVideos.value = channelVideos
                        .filter { it.id != previousVideo.id }
                        .take(10)
                } else {
                    val allVideos = videoRepository.getAllVideos().first()
                    _relatedVideos.value = allVideos
                        .filter { it.id != previousVideo.id }
                        .shuffled()
                        .take(10)
                }
            }
        }
    }

    fun updateElapsedTime(seconds: Int) {
        elapsedSeconds = seconds

        val currentMinute = seconds / 60
        if (currentMinute > lastSavedMinute) {
            lastSavedMinute = currentMinute
            saveTimeUsed()
        }
    }

    private fun saveTimeUsed() {
        viewModelScope.launch {
            currentProfileId?.let { profileId ->
                val profile = profileRepository.getProfileById(profileId)
                profile?.let {
                    val newUsed = it.usedTodayMinutes + 1
                    profileRepository.updateUsedTime(profileId, newUsed)

                    _timeStatus.value = TimeStatus(
                        remainingMinutes = (it.dailyLimitMinutes - newUsed).coerceAtLeast(0),
                        totalMinutes = it.dailyLimitMinutes,
                        usedMinutes = newUsed
                    )
                }
            }
        }
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
        if (playing) {
            startTracking()
        } else {
            pauseTracking()
        }
    }

    private fun startTracking() {
        if (trackingJob?.isActive == true) return

        trackingJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                elapsedSeconds++

                // Update time status every minute
                if (elapsedSeconds % 60 == 0) {
                    saveTimeUsed()
                }
            }
        }
    }

    private fun pauseTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    fun stopTracking() {
        pauseTracking()

        // Save remaining seconds as partial minute
        if (elapsedSeconds > 0) {
            viewModelScope.launch {
                currentProfileId?.let { profileId ->
                    val totalMinutes = elapsedSeconds / 60
                    val unsavedMinutes = totalMinutes - lastSavedMinute

                    if (unsavedMinutes > 0) {
                        val profile = profileRepository.getProfileById(profileId)
                        profile?.let {
                            val newUsed = it.usedTodayMinutes + unsavedMinutes
                            profileRepository.updateUsedTime(profileId, newUsed)
                        }
                    }
                }
            }
        }
        elapsedSeconds = 0
        lastSavedMinute = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }
}
