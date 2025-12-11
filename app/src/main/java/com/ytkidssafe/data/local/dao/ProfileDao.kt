package com.ytkidssafe.data.local.dao

import androidx.room.*
import com.ytkidssafe.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("UPDATE profiles SET usedTodayMinutes = :minutes WHERE id = :profileId")
    suspend fun updateUsedTime(profileId: String, minutes: Int)

    @Query("UPDATE profiles SET usedTodayMinutes = 0, lastResetDate = :resetDate")
    suspend fun resetAllDailyTime(resetDate: Long)

    @Query("UPDATE profiles SET usedTodayMinutes = 0, lastResetDate = :resetDate WHERE id = :profileId")
    suspend fun resetProfileDailyTime(profileId: String, resetDate: Long)

    @Query("SELECT * FROM profiles")
    suspend fun getAllProfilesSync(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)

    @Query("DELETE FROM profiles")
    suspend fun deleteAllProfiles()
}
