package com.ytkidssafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val videoCount: Int = 0
)

@Entity(tableName = "profile_playlist_cross_ref", primaryKeys = ["profileId", "playlistId"])
data class ProfilePlaylistCrossRef(
    val profileId: String,
    val playlistId: String
)
