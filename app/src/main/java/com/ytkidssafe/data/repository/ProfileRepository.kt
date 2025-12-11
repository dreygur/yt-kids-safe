package com.ytkidssafe.data.repository

import com.ytkidssafe.data.local.dao.ProfileDao
import com.ytkidssafe.data.local.entity.ProfileEntity
import com.ytkidssafe.domain.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getProfileById(id: String): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }

    suspend fun createProfile(name: String, avatar: String, dailyLimitMinutes: Int): Profile {
        val entity = ProfileEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            avatar = avatar,
            dailyLimitMinutes = dailyLimitMinutes
        )
        profileDao.insertProfile(entity)
        return entity.toDomain()
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.updateProfile(profile.toEntity())
    }

    suspend fun deleteProfile(profile: Profile) {
        profileDao.deleteProfile(profile.toEntity())
    }

    suspend fun updateUsedTime(profileId: String, minutes: Int) {
        profileDao.updateUsedTime(profileId, minutes)
    }

    suspend fun resetDailyTime(profileId: String) {
        profileDao.resetProfileDailyTime(profileId, System.currentTimeMillis())
    }

    suspend fun resetAllDailyTime() {
        profileDao.resetAllDailyTime(System.currentTimeMillis())
    }

    private fun ProfileEntity.toDomain(): Profile {
        val filters = try {
            Json.decodeFromString<List<String>>(categoryFilters)
        } catch (e: Exception) {
            emptyList()
        }
        return Profile(
            id = id,
            name = name,
            avatar = avatar,
            dailyLimitMinutes = dailyLimitMinutes,
            usedTodayMinutes = usedTodayMinutes,
            lastResetDate = lastResetDate,
            categoryFilters = filters
        )
    }

    private fun Profile.toEntity(): ProfileEntity {
        return ProfileEntity(
            id = id,
            name = name,
            avatar = avatar,
            dailyLimitMinutes = dailyLimitMinutes,
            usedTodayMinutes = usedTodayMinutes,
            lastResetDate = lastResetDate,
            categoryFilters = Json.encodeToString(categoryFilters)
        )
    }
}
