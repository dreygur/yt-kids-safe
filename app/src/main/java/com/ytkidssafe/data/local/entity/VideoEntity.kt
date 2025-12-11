package com.ytkidssafe.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "videos",
    indices = [Index("channelId"), Index("playlistId")]
)
data class VideoEntity(
    @PrimaryKey
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelId: String? = null,
    val playlistId: String? = null,
    val duration: String,
    val cachedAt: Long = System.currentTimeMillis()
)
