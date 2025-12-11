package com.ytkidssafe.data.local.dao

import androidx.room.*
import com.ytkidssafe.data.local.entity.ChannelEntity
import com.ytkidssafe.data.local.entity.ProfileChannelCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels ORDER BY title ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("""
        SELECT c.* FROM channels c
        INNER JOIN profile_channel_cross_ref pc ON c.id = pc.channelId
        WHERE pc.profileId = :profileId
        ORDER BY c.title ASC
    """)
    fun getChannelsForProfile(profileId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE youtubeId = :youtubeId")
    suspend fun getChannelByYoutubeId(youtubeId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfileChannelCrossRef(crossRef: ProfileChannelCrossRef)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM profile_channel_cross_ref WHERE profileId = :profileId AND channelId = :channelId")
    suspend fun removeChannelFromProfile(profileId: String, channelId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM profile_channel_cross_ref WHERE profileId = :profileId AND channelId = :channelId)")
    suspend fun isChannelAssignedToProfile(profileId: String, channelId: String): Boolean

    @Query("SELECT * FROM channels")
    suspend fun getAllChannelsSync(): List<ChannelEntity>

    @Query("SELECT * FROM profile_channel_cross_ref")
    suspend fun getAllProfileChannelRefs(): List<ProfileChannelCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileChannelRefs(refs: List<ProfileChannelCrossRef>)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()

    @Query("DELETE FROM profile_channel_cross_ref")
    suspend fun deleteAllProfileChannelRefs()

    @Query("SELECT channelId FROM profile_channel_cross_ref WHERE profileId = :profileId")
    suspend fun getAssignedChannelIds(profileId: String): List<String>

    @Query("DELETE FROM profile_channel_cross_ref WHERE profileId = :profileId")
    suspend fun clearChannelsForProfile(profileId: String)
}
