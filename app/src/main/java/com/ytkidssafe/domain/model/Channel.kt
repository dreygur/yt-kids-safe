package com.ytkidssafe.domain.model

data class Channel(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val category: String = "All"
)

object Categories {
    val defaults = listOf("Cartoons", "Learning", "Music", "Stories", "Games")

    // For display purposes - prepends "All" to any category list
    fun withAll(categories: List<String>) = listOf("All") + categories

    // Legacy compatibility
    val all get() = withAll(defaults)
}
