package com.ytkidssafe.domain.model

data class Channel(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val category: String = "All"
)

object Categories {
    val all = listOf("All", "Cartoons", "Learning", "Music", "Stories", "Games")
}
