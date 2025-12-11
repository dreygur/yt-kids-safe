package com.ytkidssafe.service

import com.ytkidssafe.data.repository.ProfileRepository
import com.ytkidssafe.domain.model.TimeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTracker @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var trackingJob: Job? = null
    private var currentProfileId: String? = null
    private var elapsedSeconds = 0

    private val _timeStatus = MutableStateFlow(TimeStatus(60, 60, 0))
    val timeStatus: StateFlow<TimeStatus> = _timeStatus.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    fun startTracking(profileId: String) {
        if (trackingJob?.isActive == true && currentProfileId == profileId) return

        stopTracking()
        currentProfileId = profileId
        elapsedSeconds = 0
        _isTracking.value = true

        trackingJob = scope.launch {
            // Load initial time status
            val profile = profileRepository.getProfileById(profileId)
            profile?.let {
                _timeStatus.value = TimeStatus(
                    remainingMinutes = it.remainingMinutes,
                    totalMinutes = it.dailyLimitMinutes,
                    usedMinutes = it.usedTodayMinutes
                )
            }

            while (isActive) {
                delay(1000)
                elapsedSeconds++

                // Update every minute
                if (elapsedSeconds % 60 == 0) {
                    updateTimeUsed()
                }
            }
        }
    }

    fun pauseTracking() {
        trackingJob?.cancel()
        trackingJob = null
        _isTracking.value = false
        saveElapsedTime()
    }

    fun stopTracking() {
        pauseTracking()
        currentProfileId = null
        elapsedSeconds = 0
    }

    private fun saveElapsedTime() {
        if (elapsedSeconds > 0) {
            scope.launch {
                currentProfileId?.let { profileId ->
                    val additionalMinutes = elapsedSeconds / 60
                    if (additionalMinutes > 0) {
                        val profile = profileRepository.getProfileById(profileId)
                        profile?.let {
                            val newUsed = it.usedTodayMinutes + additionalMinutes
                            profileRepository.updateUsedTime(profileId, newUsed)
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateTimeUsed() {
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

    suspend fun checkAndResetDaily(profileId: String) {
        val profile = profileRepository.getProfileById(profileId) ?: return

        val now = System.currentTimeMillis()
        val lastResetDay = profile.lastResetDate / (24 * 60 * 60 * 1000)
        val currentDay = now / (24 * 60 * 60 * 1000)

        if (currentDay > lastResetDay) {
            profileRepository.resetDailyTime(profileId)
        }
    }
}
