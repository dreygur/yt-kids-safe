package com.ytkidssafe.domain.model

data class Playlist(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val videoCount: Int = 0,
    val category: String = "All"
)
