package com.ytkidssafe.domain.model

data class Profile(
    val id: String,
    val name: String,
    val avatar: String,
    val dailyLimitMinutes: Int = 60,
    val usedTodayMinutes: Int = 0,
    val lastResetDate: Long = System.currentTimeMillis(),
    val categoryFilters: List<String> = emptyList()
) {
    val remainingMinutes: Int
        get() = (dailyLimitMinutes - usedTodayMinutes).coerceAtLeast(0)

    val hasTimeRemaining: Boolean
        get() = remainingMinutes > 0

    val usagePercentage: Float
        get() = if (dailyLimitMinutes > 0) {
            (usedTodayMinutes.toFloat() / dailyLimitMinutes).coerceIn(0f, 1f)
        } else 0f
}

object Avatars {
    val all = listOf(
        "bear" to "🐻",
        "fox" to "🦊",
        "bunny" to "🐰",
        "panda" to "🐼",
        "lion" to "🦁",
        "frog" to "🐸",
        "cat" to "🐱",
        "dog" to "🐶",
        "unicorn" to "🦄",
        "penguin" to "🐧",
        "butterfly" to "🦋",
        "monkey" to "🐵"
    )

    fun getEmoji(key: String): String {
        return all.find { it.first == key }?.second ?: "🐻"
    }
}
