package com.ytkidssafe.video.extractor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching YouTube channel and video information.
 * Uses YouTube's official RSS feeds (most reliable approach).
 */
@Singleton
class YouTubeService @Inject constructor() {

    companion object {
        private const val TAG = "YouTubeService"
        private const val RSS_BASE = "https://www.youtube.com/feeds/videos.xml"
        private const val OEMBED_BASE = "https://www.youtube.com/oembed"
    }

    data class PlaylistInfo(
        val id: String,
        val title: String,
        val videoCount: Int,
        val thumbnailUrl: String,
        val videos: List<VideoInfo>
    )

    data class VideoInfo(
        val youtubeId: String,
        val title: String,
        val thumbnailUrl: String,
        val duration: String,
        val channelName: String
    )

    data class ChannelInfo(
        val id: String,
        val name: String,
        val thumbnailUrl: String,
        val videos: List<VideoInfo>
    )

    /**
     * Parse playlist ID from URL
     */
    fun parsePlaylistId(url: String): String? {
        val patterns = listOf(
            Regex("[?&]list=([a-zA-Z0-9_-]+)"),
            Regex("playlist\\?list=([a-zA-Z0-9_-]+)"),
            Regex("^(PL[a-zA-Z0-9_-]+)$")
        )

        for (pattern in patterns) {
            pattern.find(url)?.let { match ->
                return match.groupValues[1]
            }
        }
        return null
    }

    /**
     * Fetch playlist info using RSS
     */
    suspend fun fetchPlaylist(playlistId: String): Result<PlaylistInfo> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching playlist: $playlistId")
        try {
            val url = URL("$RSS_BASE?playlist_id=$playlistId")
            val response = fetchUrl(url)

            if (response != null) {
                val (playlistTitle, videos) = parsePlaylistRssFeed(response)
                Log.d(TAG, "Playlist fetched: $playlistTitle with ${videos.size} videos")

                Result.success(PlaylistInfo(
                    id = playlistId,
                    title = playlistTitle,
                    videoCount = videos.size,
                    thumbnailUrl = videos.firstOrNull()?.thumbnailUrl ?: "",
                    videos = videos
                ))
            } else {
                Result.failure(Exception("Failed to fetch playlist"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching playlist: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Fetch channel info and videos.
     * Accepts either channel ID (UCxxxxx) or handle (@username/username)
     */
    suspend fun fetchChannel(channelIdOrHandle: String): Result<ChannelInfo> = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching channel: $channelIdOrHandle")

        try {
            // Step 1: Resolve to channel ID and get thumbnail if needed
            val (channelId, thumbnailUrl) = if (channelIdOrHandle.startsWith("UC") && channelIdOrHandle.length >= 24) {
                // Direct channel ID - fetch thumbnail from channel page
                val thumb = fetchChannelThumbnail("https://www.youtube.com/channel/$channelIdOrHandle")
                Pair(channelIdOrHandle, thumb)
            } else {
                resolveHandleToChannelIdWithThumbnail(channelIdOrHandle)
            }

            if (channelId == null) {
                Log.e(TAG, "Could not resolve channel ID for: $channelIdOrHandle")
                return@withContext Result.failure(Exception("Could not find channel. Try using a direct channel ID (UCxxxxx)."))
            }

            Log.d(TAG, "Resolved channel ID: $channelId, thumbnail: $thumbnailUrl")

            // Step 2: Fetch videos via RSS
            val rssUrl = URL("$RSS_BASE?channel_id=$channelId")
            val response = fetchUrl(rssUrl)

            if (response == null) {
                return@withContext Result.failure(Exception("Failed to fetch channel feed"))
            }

            // Step 3: Parse RSS feed
            val (channelName, videos) = parseChannelRssFeed(response)
            Log.d(TAG, "Channel fetched: $channelName with ${videos.size} videos")

            // Use first video thumbnail as fallback if no channel thumbnail
            val finalThumbnail = thumbnailUrl.ifEmpty {
                videos.firstOrNull()?.thumbnailUrl ?: ""
            }

            Result.success(ChannelInfo(
                id = channelId,
                name = channelName,
                thumbnailUrl = finalThumbnail,
                videos = videos
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching channel: ${e.message}")
            Result.failure(Exception("Failed to fetch channel: ${e.message}"))
        }
    }

    /**
     * Fetch basic video info using oEmbed
     */
    suspend fun fetchVideoInfo(videoId: String): Result<VideoInfo> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$OEMBED_BASE?url=https://youtube.com/watch?v=$videoId&format=json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                Result.success(VideoInfo(
                    youtubeId = videoId,
                    title = json.optString("title", "Video"),
                    thumbnailUrl = "https://img.youtube.com/vi/$videoId/mqdefault.jpg",
                    duration = "",
                    channelName = json.optString("author_name", "Unknown")
                ))
            } else {
                Result.failure(Exception("Video not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching video: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Resolve a handle (@username or username) to a channel ID and thumbnail.
     * Uses YouTube's channel page to extract both.
     */
    private fun resolveHandleToChannelIdWithThumbnail(handle: String): Pair<String?, String> {
        val cleanHandle = handle.removePrefix("@").trim()
        Log.d(TAG, "Resolving handle: $cleanHandle")

        // Try @handle format first
        val handleUrl = "https://www.youtube.com/@$cleanHandle"
        val (channelId, thumbnail) = extractChannelIdAndThumbnailFromPage(handleUrl)

        if (channelId != null) {
            return Pair(channelId, thumbnail)
        }

        // Try /c/ format
        val cUrl = "https://www.youtube.com/c/$cleanHandle"
        return extractChannelIdAndThumbnailFromPage(cUrl)
    }

    /**
     * Fetch channel thumbnail from a channel page URL
     */
    private fun fetchChannelThumbnail(pageUrl: String): String {
        return try {
            val url = URL(pageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = true

            if (connection.responseCode != 200) {
                return ""
            }

            val response = connection.inputStream.bufferedReader().readText()
            extractThumbnailFromHtml(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching thumbnail: ${e.message}")
            ""
        }
    }

    /**
     * Extract thumbnail URL from HTML response
     */
    private fun extractThumbnailFromHtml(html: String): String {
        val patterns = listOf(
            // Avatar image patterns
            Regex("\"avatar\":\\{\"thumbnails\":\\[\\{\"url\":\"([^\"]+)\""),
            Regex("\"thumbnails\":\\[\\{\"url\":\"(https://yt3[^\"]+)\""),
            Regex("og:image\" content=\"([^\"]+)\""),
            Regex("\"channelAvatarData\"[^}]*\"url\":\"([^\"]+)\"")
        )

        for (pattern in patterns) {
            pattern.find(html)?.let { match ->
                val url = match.groupValues[1]
                    .replace("\\u0026", "&")
                    .replace("\\/", "/")
                Log.d(TAG, "Found thumbnail: $url")
                return url
            }
        }
        return ""
    }

    /**
     * Extract channel ID and thumbnail from a YouTube page
     */
    private fun extractChannelIdAndThumbnailFromPage(pageUrl: String): Pair<String?, String> {
        try {
            val url = URL(pageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.instanceFollowRedirects = true

            if (connection.responseCode != 200) {
                Log.w(TAG, "Page fetch failed: ${connection.responseCode}")
                return Pair(null, "")
            }

            val response = connection.inputStream.bufferedReader().readText()

            // Look for channel ID in various places
            val idPatterns = listOf(
                Regex("\"channelId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                Regex("\"externalId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                Regex("\"browseId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                Regex("channel/(UC[a-zA-Z0-9_-]{22})")
            )

            var channelId: String? = null
            for (pattern in idPatterns) {
                pattern.find(response)?.let { match ->
                    channelId = match.groupValues[1]
                    Log.d(TAG, "Found channel ID via pattern: $channelId")
                }
                if (channelId != null) break
            }

            // Extract thumbnail
            val thumbnail = extractThumbnailFromHtml(response)

            return Pair(channelId, thumbnail)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting channel info: ${e.message}")
            return Pair(null, "")
        }
    }

    /**
     * Parse playlist RSS feed and return playlist title + videos
     */
    private fun parsePlaylistRssFeed(xml: String): Pair<String, List<VideoInfo>> {
        var playlistTitle = "Playlist"
        val videos = mutableListOf<VideoInfo>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentVideoId: String? = null
            var currentTitle: String? = null
            var currentThumbnail: String? = null
            var currentAuthor: String? = null
            var inEntry = false
            var inAuthor = false
            var foundPlaylistTitle = false

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                inEntry = true
                                currentVideoId = null
                                currentTitle = null
                                currentThumbnail = null
                                currentAuthor = null
                            }
                            "author" -> inAuthor = true
                            "yt:videoId" -> if (inEntry) currentVideoId = parser.nextText()
                            "title" -> {
                                val title = parser.nextText()
                                if (inEntry && !inAuthor) {
                                    currentTitle = title
                                } else if (!inEntry && !foundPlaylistTitle) {
                                    playlistTitle = title
                                    foundPlaylistTitle = true
                                }
                            }
                            "name" -> if (inAuthor) currentAuthor = parser.nextText()
                            "media:thumbnail" -> if (inEntry) {
                                currentThumbnail = parser.getAttributeValue(null, "url")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                if (currentVideoId != null && currentTitle != null) {
                                    videos.add(VideoInfo(
                                        youtubeId = currentVideoId,
                                        title = currentTitle,
                                        thumbnailUrl = currentThumbnail
                                            ?: "https://img.youtube.com/vi/$currentVideoId/mqdefault.jpg",
                                        duration = "",
                                        channelName = currentAuthor ?: "Unknown"
                                    ))
                                }
                                inEntry = false
                            }
                            "author" -> inAuthor = false
                        }
                    }
                }
                parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing playlist RSS: ${e.message}")
        }

        return Pair(playlistTitle, videos.take(50))
    }

    /**
     * Parse YouTube RSS feed and return list of videos
     */
    private fun parseRssFeed(xml: String): List<VideoInfo> {
        val videos = mutableListOf<VideoInfo>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentVideoId: String? = null
            var currentTitle: String? = null
            var currentThumbnail: String? = null
            var currentAuthor: String? = null
            var inEntry = false
            var inAuthor = false

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                inEntry = true
                                currentVideoId = null
                                currentTitle = null
                                currentThumbnail = null
                                currentAuthor = null
                            }
                            "author" -> inAuthor = true
                            "yt:videoId" -> if (inEntry) currentVideoId = parser.nextText()
                            "title" -> if (inEntry && !inAuthor) currentTitle = parser.nextText()
                            "name" -> if (inAuthor) currentAuthor = parser.nextText()
                            "media:thumbnail" -> if (inEntry) {
                                currentThumbnail = parser.getAttributeValue(null, "url")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                if (currentVideoId != null && currentTitle != null) {
                                    videos.add(VideoInfo(
                                        youtubeId = currentVideoId,
                                        title = currentTitle,
                                        thumbnailUrl = currentThumbnail
                                            ?: "https://img.youtube.com/vi/$currentVideoId/mqdefault.jpg",
                                        duration = "",
                                        channelName = currentAuthor ?: "Unknown"
                                    ))
                                }
                                inEntry = false
                            }
                            "author" -> inAuthor = false
                        }
                    }
                }
                parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing RSS: ${e.message}")
        }

        return videos.take(30) // Limit to 30 videos
    }

    /**
     * Parse channel RSS feed and return channel name + videos
     */
    private fun parseChannelRssFeed(xml: String): Pair<String, List<VideoInfo>> {
        var channelName = "Channel"
        val videos = mutableListOf<VideoInfo>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var currentVideoId: String? = null
            var currentTitle: String? = null
            var currentThumbnail: String? = null
            var currentAuthor: String? = null
            var inEntry = false
            var inAuthor = false
            var foundChannelName = false

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                inEntry = true
                                currentVideoId = null
                                currentTitle = null
                                currentThumbnail = null
                                currentAuthor = null
                            }
                            "author" -> inAuthor = true
                            "yt:videoId" -> if (inEntry) currentVideoId = parser.nextText()
                            "title" -> {
                                val title = parser.nextText()
                                if (inEntry && !inAuthor) {
                                    currentTitle = title
                                } else if (!inEntry && !foundChannelName) {
                                    channelName = title
                                    foundChannelName = true
                                }
                            }
                            "name" -> if (inAuthor) {
                                currentAuthor = parser.nextText()
                                if (!foundChannelName) {
                                    channelName = currentAuthor
                                    foundChannelName = true
                                }
                            }
                            "media:thumbnail" -> if (inEntry) {
                                currentThumbnail = parser.getAttributeValue(null, "url")
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "entry" -> {
                                if (currentVideoId != null && currentTitle != null) {
                                    videos.add(VideoInfo(
                                        youtubeId = currentVideoId,
                                        title = currentTitle,
                                        thumbnailUrl = currentThumbnail
                                            ?: "https://img.youtube.com/vi/$currentVideoId/mqdefault.jpg",
                                        duration = "",
                                        channelName = currentAuthor ?: channelName
                                    ))
                                }
                                inEntry = false
                            }
                            "author" -> inAuthor = false
                        }
                    }
                }
                parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing channel RSS: ${e.message}")
        }

        return Pair(channelName, videos.take(30))
    }

    private fun fetchUrl(url: URL): String? {
        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                Log.w(TAG, "HTTP ${connection.responseCode} for $url")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching $url: ${e.message}")
            null
        }
    }
}
