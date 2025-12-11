package com.ytkidssafe.data.repository

import com.ytkidssafe.data.local.dao.VideoDao
import com.ytkidssafe.data.local.entity.VideoEntity
import com.ytkidssafe.domain.model.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val videoDao: VideoDao
) {
    fun getAllVideos(): Flow<List<Video>> {
        return videoDao.getAllVideos().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVideosByChannel(channelId: String): Flow<List<Video>> {
        return videoDao.getVideosByChannel(channelId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVideosByPlaylist(playlistId: String): Flow<List<Video>> {
        return videoDao.getVideosByPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVideosForProfile(profileId: String): Flow<List<Video>> {
        return videoDao.getVideosForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getVideoById(id: String): Video? {
        return videoDao.getVideoById(id)?.toDomain()
    }

    suspend fun getVideoByYoutubeId(youtubeId: String): Video? {
        return videoDao.getVideoByYoutubeId(youtubeId)?.toDomain()
    }

    suspend fun addVideo(
        youtubeId: String,
        title: String,
        thumbnailUrl: String,
        channelId: String? = null,
        playlistId: String? = null,
        duration: String
    ): Video {
        val existing = videoDao.getVideoByYoutubeId(youtubeId)
        if (existing != null) {
            return existing.toDomain()
        }

        val entity = VideoEntity(
            id = UUID.randomUUID().toString(),
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelId = channelId,
            playlistId = playlistId,
            duration = duration
        )
        videoDao.insertVideo(entity)
        return entity.toDomain()
    }

    suspend fun addVideos(videos: List<Video>) {
        videoDao.insertVideos(videos.map { it.toEntity() })
    }

    suspend fun deleteVideo(video: Video) {
        videoDao.deleteVideo(video.toEntity())
    }

    suspend fun deleteVideosByChannel(channelId: String) {
        videoDao.deleteVideosByChannel(channelId)
    }

    suspend fun deleteVideosByPlaylist(playlistId: String) {
        videoDao.deleteVideosByPlaylist(playlistId)
    }

    suspend fun cleanupOldCache(daysOld: Int = 7) {
        val threshold = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysOld.toLong())
        videoDao.deleteOldVideos(threshold)
    }

    private fun VideoEntity.toDomain(): Video {
        return Video(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelId = channelId,
            playlistId = playlistId,
            duration = duration,
            cachedAt = cachedAt
        )
    }

    private fun Video.toEntity(): VideoEntity {
        return VideoEntity(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            channelId = channelId,
            playlistId = playlistId,
            duration = duration,
            cachedAt = cachedAt
        )
    }
}
