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
                val videos = parseRssFeed(response)
                Log.d(TAG, "Playlist fetched: ${videos.size} videos")

                Result.success(PlaylistInfo(
                    id = playlistId,
                    title = "Playlist",
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
            // Step 1: Resolve to channel ID if needed
            val channelId = if (channelIdOrHandle.startsWith("UC") && channelIdOrHandle.length >= 24) {
                channelIdOrHandle
            } else {
                resolveHandleToChannelId(channelIdOrHandle)
            }

            if (channelId == null) {
                Log.e(TAG, "Could not resolve channel ID for: $channelIdOrHandle")
                return@withContext Result.failure(Exception("Could not find channel. Try using a direct channel ID (UCxxxxx)."))
            }

            Log.d(TAG, "Resolved channel ID: $channelId")

            // Step 2: Fetch videos via RSS
            val rssUrl = URL("$RSS_BASE?channel_id=$channelId")
            val response = fetchUrl(rssUrl)

            if (response == null) {
                return@withContext Result.failure(Exception("Failed to fetch channel feed"))
            }

            // Step 3: Parse RSS feed
            val (channelName, videos) = parseChannelRssFeed(response)
            Log.d(TAG, "Channel fetched: $channelName with ${videos.size} videos")

            Result.success(ChannelInfo(
                id = channelId,
                name = channelName,
                thumbnailUrl = "", // RSS doesn't provide channel thumbnail
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
     * Resolve a handle (@username or username) to a channel ID.
     * Uses YouTube's channel page to extract the channel ID.
     */
    private fun resolveHandleToChannelId(handle: String): String? {
        val cleanHandle = handle.removePrefix("@").trim()
        Log.d(TAG, "Resolving handle: $cleanHandle")

        // Try @handle format first
        val handleUrl = "https://www.youtube.com/@$cleanHandle"
        val channelId = extractChannelIdFromPage(handleUrl)

        if (channelId != null) {
            return channelId
        }

        // Try /c/ format
        val cUrl = "https://www.youtube.com/c/$cleanHandle"
        return extractChannelIdFromPage(cUrl)
    }

    /**
     * Extract channel ID from a YouTube page by looking for canonical URL or channel ID in HTML
     */
    private fun extractChannelIdFromPage(pageUrl: String): String? {
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
                return null
            }

            val response = connection.inputStream.bufferedReader().readText()

            // Look for channel ID in various places
            val patterns = listOf(
                // Canonical URL pattern
                Regex("\"channelId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                // External ID pattern
                Regex("\"externalId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                // Browse ID pattern
                Regex("\"browseId\":\"(UC[a-zA-Z0-9_-]{22})\""),
                // URL pattern in meta tags
                Regex("channel/(UC[a-zA-Z0-9_-]{22})")
            )

            for (pattern in patterns) {
                pattern.find(response)?.let { match ->
                    val channelId = match.groupValues[1]
                    Log.d(TAG, "Found channel ID via pattern: $channelId")
                    return channelId
                }
            }

            Log.w(TAG, "No channel ID found in page")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting channel ID: ${e.message}")
            return null
        }
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
