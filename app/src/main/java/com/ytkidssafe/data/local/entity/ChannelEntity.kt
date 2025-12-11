package com.ytkidssafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val category: String = "All"
)

@Entity(tableName = "profile_channel_cross_ref", primaryKeys = ["profileId", "channelId"])
data class ProfileChannelCrossRef(
    val profileId: String,
    val channelId: String
)
