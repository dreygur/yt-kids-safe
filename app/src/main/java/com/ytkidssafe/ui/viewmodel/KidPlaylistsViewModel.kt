package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.PlaylistRepository
import com.ytkidssafe.data.repository.ProfileRepository
import com.ytkidssafe.data.repository.SettingsRepository
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.domain.model.Profile
import com.ytkidssafe.domain.model.TimeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class KidPlaylistsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _timeStatus = MutableStateFlow(TimeStatus(60, 60, 0))
    val timeStatus: StateFlow<TimeStatus> = _timeStatus.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private var currentProfileId: String? = null

    fun loadProfile(profileId: String) {
        if (currentProfileId == profileId) return
        currentProfileId = profileId

        viewModelScope.launch {
            val profiles = profileRepository.getAllProfiles().first()
            val p = profiles.find { it.id == profileId }
            _profile.value = p
            p?.let { updateTimeStatus(it) }

            // Load profile-specific playlists
            playlistRepository.getPlaylistsForProfile(profileId).collect { playlists ->
                _playlists.value = playlists
            }
        }
    }

    fun verifyPin(pin: String): Boolean {
        return runBlocking {
            settingsRepository.verifyPin(pin)
        }
    }

    private fun updateTimeStatus(profile: Profile) {
        _timeStatus.value = TimeStatus(
            remainingMinutes = profile.remainingMinutes,
            totalMinutes = profile.dailyLimitMinutes,
            usedMinutes = profile.usedTodayMinutes
        )
    }
}
