package com.ytkidssafe.data.backup

import android.content.Context
import android.net.Uri
import com.ytkidssafe.data.local.dao.ChannelDao
import com.ytkidssafe.data.local.dao.ProfileDao
import com.ytkidssafe.data.local.entity.ChannelEntity
import com.ytkidssafe.data.local.entity.ProfileChannelCrossRef
import com.ytkidssafe.data.local.entity.ProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val profiles: List<ProfileBackup>,
    val channels: List<ChannelBackup>,
    val profileChannelRefs: List<ProfileChannelRefBackup>
)

@Serializable
data class ProfileBackup(
    val id: String,
    val name: String,
    val avatar: String,
    val dailyLimitMinutes: Int,
    val categoryFilters: String
)

@Serializable
data class ChannelBackup(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val category: String
)

@Serializable
data class ProfileChannelRefBackup(
    val profileId: String,
    val channelId: String
)

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profileDao: ProfileDao,
    private val channelDao: ChannelDao
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportToJson(): String {
        val profiles = profileDao.getAllProfilesSync().map { it.toBackup() }
        val channels = channelDao.getAllChannelsSync().map { it.toBackup() }
        val refs = channelDao.getAllProfileChannelRefs().map { it.toBackup() }

        val backup = BackupData(
            profiles = profiles,
            channels = channels,
            profileChannelRefs = refs
        )

        return json.encodeToString(backup)
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> {
        return try {
            val jsonString = exportToJson()
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(jsonString.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromJson(jsonString: String): Result<Int> {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)

            // Clear existing data
            channelDao.deleteAllProfileChannelRefs()
            channelDao.deleteAllChannels()
            profileDao.deleteAllProfiles()

            // Import profiles
            val profileEntities = backup.profiles.map { it.toEntity() }
            profileDao.insertProfiles(profileEntities)

            // Import channels
            val channelEntities = backup.channels.map { it.toEntity() }
            channelDao.insertChannels(channelEntities)

            // Import profile-channel relationships
            val refs = backup.profileChannelRefs.map { it.toEntity() }
            channelDao.insertProfileChannelRefs(refs)

            val count = profileEntities.size + channelEntities.size
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importFromUri(uri: Uri): Result<Int> {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader().readText()
            } ?: return Result.failure(Exception("Could not read file"))

            importFromJson(jsonString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ProfileEntity.toBackup() = ProfileBackup(
        id = id,
        name = name,
        avatar = avatar,
        dailyLimitMinutes = dailyLimitMinutes,
        categoryFilters = categoryFilters
    )

    private fun ProfileBackup.toEntity() = ProfileEntity(
        id = id,
        name = name,
        avatar = avatar,
        dailyLimitMinutes = dailyLimitMinutes,
        usedTodayMinutes = 0,
        lastResetDate = System.currentTimeMillis(),
        categoryFilters = categoryFilters
    )

    private fun ChannelEntity.toBackup() = ChannelBackup(
        id = id,
        youtubeId = youtubeId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        category = category
    )

    private fun ChannelBackup.toEntity() = ChannelEntity(
        id = id,
        youtubeId = youtubeId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        category = category
    )

    private fun ProfileChannelCrossRef.toBackup() = ProfileChannelRefBackup(
        profileId = profileId,
        channelId = channelId
    )

    private fun ProfileChannelRefBackup.toEntity() = ProfileChannelCrossRef(
        profileId = profileId,
        channelId = channelId
    )
}
