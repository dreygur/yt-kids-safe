package com.ytkidssafe.data.repository

import com.ytkidssafe.data.local.dao.PlaylistDao
import com.ytkidssafe.data.local.entity.PlaylistEntity
import com.ytkidssafe.data.local.entity.ProfilePlaylistCrossRef
import com.ytkidssafe.domain.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao
) {
    fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPlaylistsForProfile(profileId: String): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getPlaylistById(id: String): Playlist? {
        return playlistDao.getPlaylistById(id)?.toDomain()
    }

    suspend fun getPlaylistByYoutubeId(youtubeId: String): Playlist? {
        return playlistDao.getPlaylistByYoutubeId(youtubeId)?.toDomain()
    }

    suspend fun addPlaylist(
        youtubeId: String,
        title: String,
        thumbnailUrl: String,
        videoCount: Int = 0,
        category: String = "All"
    ): Playlist {
        val existing = playlistDao.getPlaylistByYoutubeId(youtubeId)
        if (existing != null) {
            return existing.toDomain()
        }

        val entity = PlaylistEntity(
            id = UUID.randomUUID().toString(),
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            videoCount = videoCount,
            category = category
        )
        playlistDao.insertPlaylist(entity)
        return entity.toDomain()
    }

    suspend fun assignPlaylistToProfile(playlistId: String, profileId: String) {
        playlistDao.insertProfilePlaylistCrossRef(
            ProfilePlaylistCrossRef(profileId, playlistId)
        )
    }

    suspend fun removePlaylistFromProfile(playlistId: String, profileId: String) {
        playlistDao.removePlaylistFromProfile(profileId, playlistId)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.toEntity())
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }

    suspend fun getAssignedPlaylistIds(profileId: String): List<String> {
        return playlistDao.getAssignedPlaylistIds(profileId)
    }

    suspend fun setPlaylistsForProfile(profileId: String, playlistIds: List<String>) {
        playlistDao.clearPlaylistsForProfile(profileId)
        val refs = playlistIds.map { ProfilePlaylistCrossRef(profileId, it) }
        playlistDao.insertProfilePlaylistRefs(refs)
    }

    private fun PlaylistEntity.toDomain(): Playlist {
        return Playlist(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            videoCount = videoCount,
            category = category
        )
    }

    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = id,
            youtubeId = youtubeId,
            title = title,
            thumbnailUrl = thumbnailUrl,
            videoCount = videoCount,
            category = category
        )
    }
}
