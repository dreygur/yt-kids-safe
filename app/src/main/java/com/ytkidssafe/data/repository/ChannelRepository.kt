package com.ytkidssafe.data.repository

import com.ytkidssafe.data.local.dao.ChannelDao
import com.ytkidssafe.data.local.entity.ChannelEntity
import com.ytkidssafe.data.local.entity.ProfileChannelCrossRef
import com.ytkidssafe.domain.model.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val channelDao: ChannelDao
) {
    fun getAllChannels(): Flow<List<Channel>> {
        return channelDao.getAllChannels().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getChannelsForProfile(profileId: String): Flow<List<Channel>> {
        return channelDao.getChannelsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getChannelById(id: String): Channel? {
        return channelDao.getChannelById(id)?.toDomain()
    }

    suspend fun getChannelByYoutubeId(youtubeId: String): Channel? {
        return channelDao.getChannelByYoutubeId(youtubeId)?.toDomain()
    }

    suspend fun addChannel(youtubeId: String, title: String, thumbnailUrl: String, category: String = "All"): Channel {
        val existing = channelDao.getChannelByYoutubeId(youtubeId)
        if (existing != null) {
            return existing.toDomain()
        }

        val entity = ChannelEntity(
            id = UUID.randomUUID().toString(),
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            category = category
        )
        channelDao.insertChannel(entity)
        return entity.toDomain()
    }

    suspend fun assignChannelToProfile(channelId: String, profileId: String) {
        channelDao.insertProfileChannelCrossRef(
            ProfileChannelCrossRef(profileId = profileId, channelId = channelId)
        )
    }

    suspend fun removeChannelFromProfile(channelId: String, profileId: String) {
        channelDao.removeChannelFromProfile(profileId, channelId)
    }

    suspend fun deleteChannel(channel: Channel) {
        channelDao.deleteChannel(channel.toEntity())
    }

    suspend fun isChannelAssignedToProfile(channelId: String, profileId: String): Boolean {
        return channelDao.isChannelAssignedToProfile(profileId, channelId)
    }

    private fun ChannelEntity.toDomain(): Channel {
        return Channel(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            category = category
        )
    }

    private fun Channel.toEntity(): ChannelEntity {
        return ChannelEntity(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            category = category
        )
    }
}
