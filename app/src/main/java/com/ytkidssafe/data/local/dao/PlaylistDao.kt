package com.ytkidssafe.data.local.dao

import androidx.room.*
import com.ytkidssafe.data.local.entity.PlaylistEntity
import com.ytkidssafe.data.local.entity.ProfilePlaylistCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY title ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("""
        SELECT p.* FROM playlists p
        INNER JOIN profile_playlist_cross_ref pp ON p.id = pp.playlistId
        WHERE pp.profileId = :profileId
        ORDER BY p.title ASC
    """)
    fun getPlaylistsForProfile(profileId: String): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE youtubeId = :youtubeId")
    suspend fun getPlaylistByYoutubeId(youtubeId: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfilePlaylistCrossRef(crossRef: ProfilePlaylistCrossRef)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM profile_playlist_cross_ref WHERE profileId = :profileId AND playlistId = :playlistId")
    suspend fun removePlaylistFromProfile(profileId: String, playlistId: String)
}
