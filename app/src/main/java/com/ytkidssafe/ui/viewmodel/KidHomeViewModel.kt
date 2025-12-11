package com.ytkidssafe.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.ChannelRepository
import com.ytkidssafe.data.repository.PlaylistRepository
import com.ytkidssafe.data.repository.ProfileRepository
import com.ytkidssafe.data.repository.SettingsRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.Channel
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.domain.model.Profile
import com.ytkidssafe.domain.model.TimeStatus
import com.ytkidssafe.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class KidHomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val channelRepository: ChannelRepository,
    private val playlistRepository: PlaylistRepository,
    private val videoRepository: VideoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "KidHomeViewModel"
    }

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _timeStatus = MutableStateFlow(TimeStatus(60, 60, 0))
    val timeStatus: StateFlow<TimeStatus> = _timeStatus.asStateFlow()

    private val _profileId = MutableStateFlow<String?>(null)

    // Channels filtered by profile assignment
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    // Playlists filtered by profile assignment
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Combine videos with category filter and profile assignment
    private val allVideos = videoRepository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<Video>> = combine(
        allVideos,
        _channels,
        _playlists,
        _selectedCategory
    ) { videoList, channelList, playlistList, category ->
        Log.d(TAG, "Combining: ${videoList.size} videos, ${channelList.size} channels, ${playlistList.size} playlists, category=$category")
        filterVideosByCategory(videoList, channelList, playlistList, category).shuffled()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var currentProfileId: String? = null

    fun loadProfile(profileId: String) {
        if (currentProfileId == profileId) return // Avoid reloading same profile
        currentProfileId = profileId
        _profileId.value = profileId
        Log.d(TAG, "Loading profile: $profileId")

        viewModelScope.launch {
            try {
                val profiles = profileRepository.getAllProfiles().first()
                val p = profiles.find { it.id == profileId }
                _profile.value = p
                p?.let { updateTimeStatus(it) }
                Log.d(TAG, "Profile loaded: ${p?.name}")

                // Load profile-specific channels and playlists
                channelRepository.getChannelsForProfile(profileId).collect { channels ->
                    _channels.value = channels
                    Log.d(TAG, "Loaded ${channels.size} channels for profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile: ${e.message}")
            }
        }

        viewModelScope.launch {
            try {
                playlistRepository.getPlaylistsForProfile(profileId).collect { playlists ->
                    _playlists.value = playlists
                    Log.d(TAG, "Loaded ${playlists.size} playlists for profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading playlists: ${e.message}")
            }
        }
    }

    fun selectCategory(category: String) {
        Log.d(TAG, "Selecting category: $category")
        _selectedCategory.value = category
    }

    fun verifyPin(pin: String): Boolean {
        return runBlocking {
            settingsRepository.verifyPin(pin)
        }
    }

    private fun filterVideosByCategory(
        videos: List<Video>,
        channels: List<Channel>,
        playlists: List<Playlist>,
        category: String
    ): List<Video> {
        if (category == "All") {
            Log.d(TAG, "Returning all ${videos.size} videos")
            return videos
        }

        // Get channel IDs matching category
        val categoryChannelIds = channels
            .filter { it.category == category }
            .map { it.id }
            .toSet()

        // Get playlist IDs matching category
        val categoryPlaylistIds = playlists
            .filter { it.category == category }
            .map { it.id }
            .toSet()

        // Filter videos that belong to matching channels OR playlists
        val filtered = videos.filter { video ->
            video.channelId in categoryChannelIds || video.playlistId in categoryPlaylistIds
        }
        Log.d(TAG, "Filtered to ${filtered.size} videos for category: $category")
        return filtered
    }

    private fun updateTimeStatus(profile: Profile) {
        _timeStatus.value = TimeStatus(
            remainingMinutes = profile.remainingMinutes,
            totalMinutes = profile.dailyLimitMinutes,
            usedMinutes = profile.usedTodayMinutes
        )
    }
}
