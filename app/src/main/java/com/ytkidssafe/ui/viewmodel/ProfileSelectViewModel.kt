package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.ProfileRepository
import com.ytkidssafe.data.repository.SettingsRepository
import com.ytkidssafe.domain.model.Profile
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
class ProfileSelectViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _isPinSet = MutableStateFlow(false)
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    val defaultDailyLimit: StateFlow<Int> = settingsRepository.defaultDailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    init {
        viewModelScope.launch {
            _isPinSet.value = settingsRepository.isPinSet()
        }
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profileList ->
                _profiles.value = profileList
                _isLoaded.value = true
            }
        }
    }

    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            settingsRepository.setCurrentProfile(profileId)
            // Check if daily reset needed
            val profile = profileRepository.getProfileById(profileId)
            profile?.let {
                if (needsDailyReset(it.lastResetDate)) {
                    profileRepository.resetDailyTime(profileId)
                }
            }
        }
    }

    fun verifyPin(pin: String): Boolean {
        return runBlocking {
            settingsRepository.verifyPin(pin)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            settingsRepository.setPin(pin)
            _isPinSet.value = true
        }
    }

    fun createProfile(name: String, avatar: String, dailyLimitMinutes: Int) {
        viewModelScope.launch {
            profileRepository.createProfile(name, avatar, dailyLimitMinutes)
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.deleteProfile(profile)
        }
    }

    fun setDefaultDailyLimit(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultDailyLimit(minutes)
        }
    }

    fun addBonusTime(profileId: String, minutes: Int) {
        viewModelScope.launch {
            val profile = profileRepository.getProfileById(profileId)
            profile?.let {
                val newUsed = (it.usedTodayMinutes - minutes).coerceAtLeast(0)
                profileRepository.updateUsedTime(profileId, newUsed)
            }
        }
    }

    private fun needsDailyReset(lastResetDate: Long): Boolean {
        val now = System.currentTimeMillis()
        val lastResetDay = lastResetDate / (24 * 60 * 60 * 1000)
        val currentDay = now / (24 * 60 * 60 * 1000)
        return currentDay > lastResetDay
    }
}
