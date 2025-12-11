package com.ytkidssafe.data.local.dao

import androidx.room.*
import com.ytkidssafe.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY cachedAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY cachedAt DESC")
    fun getVideosByChannel(channelId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId ORDER BY cachedAt DESC")
    fun getVideosByPlaylist(playlistId: String): Flow<List<VideoEntity>>

    @Query("""
        SELECT v.* FROM videos v
        LEFT JOIN channels c ON v.channelId = c.id
        LEFT JOIN profile_channel_cross_ref pc ON c.id = pc.channelId
        LEFT JOIN playlists p ON v.playlistId = p.id
        LEFT JOIN profile_playlist_cross_ref pp ON p.id = pp.playlistId
        WHERE pc.profileId = :profileId OR pp.profileId = :profileId
        ORDER BY v.cachedAt DESC
    """)
    fun getVideosForProfile(profileId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE youtubeId = :youtubeId")
    suspend fun getVideoByYoutubeId(youtubeId: String): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE channelId = :channelId")
    suspend fun deleteVideosByChannel(channelId: String)

    @Query("DELETE FROM videos WHERE playlistId = :playlistId")
    suspend fun deleteVideosByPlaylist(playlistId: String)

    @Query("DELETE FROM videos WHERE cachedAt < :timestamp AND channelId IS NOT NULL")
    suspend fun deleteOldVideos(timestamp: Long)
}
