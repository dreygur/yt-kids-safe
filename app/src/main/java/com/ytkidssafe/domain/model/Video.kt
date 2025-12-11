package com.ytkidssafe.domain.model

data class Video(
    val id: String,
    val youtubeId: String,
    val title: String,
    val thumbnailUrl: String,
    val channelId: String? = null,
    val playlistId: String? = null,
    val duration: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    val durationSeconds: Int
        get() = parseDuration(duration)

    companion object {
        fun parseDuration(duration: String): Int {
            // Format: "PT#H#M#S" or "5:32" or "1:05:32"
            return when {
                duration.startsWith("PT") -> parseIsoDuration(duration)
                duration.contains(":") -> parseColonDuration(duration)
                else -> 0
            }
        }

        private fun parseIsoDuration(duration: String): Int {
            var total = 0
            val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
            regex.find(duration)?.let { match ->
                val hours = match.groupValues[1].toIntOrNull() ?: 0
                val minutes = match.groupValues[2].toIntOrNull() ?: 0
                val seconds = match.groupValues[3].toIntOrNull() ?: 0
                total = hours * 3600 + minutes * 60 + seconds
            }
            return total
        }

        private fun parseColonDuration(duration: String): Int {
            val parts = duration.split(":").map { it.toIntOrNull() ?: 0 }
            return when (parts.size) {
                2 -> parts[0] * 60 + parts[1]
                3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
                else -> 0
            }
        }

        fun formatDuration(seconds: Int): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return if (hours > 0) {
                "%d:%02d:%02d".format(hours, minutes, secs)
            } else {
                "%d:%02d".format(minutes, secs)
            }
        }
    }
}
