package com.ytkidssafe.domain.model

data class TimeStatus(
    val remainingMinutes: Int,
    val totalMinutes: Int,
    val usedMinutes: Int
) {
    val percentage: Float
        get() = if (totalMinutes > 0) {
            (usedMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
        } else 0f

    val isTimeUp: Boolean
        get() = remainingMinutes <= 0

    val isLowTime: Boolean
        get() = remainingMinutes in 1..5

    val formattedRemaining: String
        get() = when {
            remainingMinutes >= 60 -> {
                val hours = remainingMinutes / 60
                val mins = remainingMinutes % 60
                if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
            }
            remainingMinutes > 0 -> "${remainingMinutes}m"
            else -> "0m"
        }
}
