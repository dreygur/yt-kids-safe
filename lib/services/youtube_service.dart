import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:uuid/uuid.dart';
import '../config/constants.dart';
import '../models/channel.dart';
import '../models/video.dart';
import '../models/playlist.dart';

class YouTubeService {
  static const _uuid = Uuid();

  // Extract channel ID from various URL formats
  static String? extractChannelId(String url) {
    final patterns = [
      RegExp(r'youtube\.com/channel/([a-zA-Z0-9_-]+)'),
      RegExp(r'youtube\.com/c/([a-zA-Z0-9_-]+)'),
      RegExp(r'youtube\.com/@([a-zA-Z0-9_-]+)'),
      RegExp(r'youtube\.com/user/([a-zA-Z0-9_-]+)'),
    ];

    for (final pattern in patterns) {
      final match = pattern.firstMatch(url);
      if (match != null) {
        return match.group(1);
      }
    }
    return null;
  }

  // Extract playlist ID from URL
  static String? extractPlaylistId(String url) {
    final pattern = RegExp(r'[?&]list=([a-zA-Z0-9_-]+)');
    final match = pattern.firstMatch(url);
    return match?.group(1);
  }

  // Extract video ID from URL
  static String? extractVideoId(String url) {
    final patterns = [
      RegExp(r'youtube\.com/watch\?v=([a-zA-Z0-9_-]+)'),
      RegExp(r'youtu\.be/([a-zA-Z0-9_-]+)'),
      RegExp(r'youtube\.com/embed/([a-zA-Z0-9_-]+)'),
    ];

    for (final pattern in patterns) {
      final match = pattern.firstMatch(url);
      if (match != null) {
        return match.group(1);
      }
    }
    return null;
  }

  // Fetch channel info from YouTube API
  static Future<Channel?> fetchChannelInfo(String channelIdOrHandle) async {
    try {
      String? channelId = channelIdOrHandle;

      // If it's a handle (@username), we need to search for it
      if (channelIdOrHandle.startsWith('@')) {
        final searchUrl = Uri.parse(
          '${AppConstants.youtubeBaseUrl}/search?part=snippet&q=${channelIdOrHandle}&type=channel&key=${AppConstants.youtubeApiKey}',
        );
        final searchResponse = await http.get(searchUrl);
        if (searchResponse.statusCode == 200) {
          final data = jsonDecode(searchResponse.body);
          final items = data['items'] as List?;
          if (items != null && items.isNotEmpty) {
            channelId = items[0]['snippet']['channelId'];
          }
        }
      }

      final url = Uri.parse(
        '${AppConstants.youtubeBaseUrl}/channels?part=snippet&id=$channelId&key=${AppConstants.youtubeApiKey}',
      );

      final response = await http.get(url);
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final items = data['items'] as List?;
        if (items != null && items.isNotEmpty) {
          final snippet = items[0]['snippet'];
          return Channel(
            id: _uuid.v4(),
            youtubeId: items[0]['id'],
            title: snippet['title'],
            thumbnail: snippet['thumbnails']['high']['url'] ??
                snippet['thumbnails']['default']['url'],
          );
        }
      }
    } catch (e) {
      print('Error fetching channel: $e');
    }
    return null;
  }

  // Fetch videos from a channel
  static Future<List<Video>> fetchChannelVideos(
    String channelYoutubeId, {
    int maxResults = 50,
  }) async {
    final videos = <Video>[];
    try {
      final url = Uri.parse(
        '${AppConstants.youtubeBaseUrl}/search?part=snippet&channelId=$channelYoutubeId&type=video&order=date&maxResults=$maxResults&key=${AppConstants.youtubeApiKey}',
      );

      final response = await http.get(url);
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final items = data['items'] as List? ?? [];

        final videoIds = items.map((item) => item['id']['videoId']).join(',');

        // Fetch video details for duration
        final detailsUrl = Uri.parse(
          '${AppConstants.youtubeBaseUrl}/videos?part=contentDetails,snippet&id=$videoIds&key=${AppConstants.youtubeApiKey}',
        );
        final detailsResponse = await http.get(detailsUrl);

        if (detailsResponse.statusCode == 200) {
          final detailsData = jsonDecode(detailsResponse.body);
          final detailItems = detailsData['items'] as List? ?? [];

          for (final item in detailItems) {
            final snippet = item['snippet'];
            final contentDetails = item['contentDetails'];
            videos.add(Video(
              id: _uuid.v4(),
              youtubeId: item['id'],
              title: snippet['title'],
              thumbnail: snippet['thumbnails']['high']['url'] ??
                  snippet['thumbnails']['default']['url'],
              channelId: snippet['channelId'],
              channelTitle: snippet['channelTitle'],
              duration: contentDetails['duration'] ?? '',
            ));
          }
        }
      }
    } catch (e) {
      print('Error fetching channel videos: $e');
    }
    return videos;
  }

  // Fetch playlist info
  static Future<Playlist?> fetchPlaylistInfo(String playlistId) async {
    try {
      final url = Uri.parse(
        '${AppConstants.youtubeBaseUrl}/playlists?part=snippet,contentDetails&id=$playlistId&key=${AppConstants.youtubeApiKey}',
      );

      final response = await http.get(url);
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        final items = data['items'] as List?;
        if (items != null && items.isNotEmpty) {
          final item = items[0];
          final snippet = item['snippet'];
          final contentDetails = item['contentDetails'];
          return Playlist(
            id: _uuid.v4(),
            youtubeId: playlistId,
            title: snippet['title'],
            thumbnail: snippet['thumbnails']['high']['url'] ??
                snippet['thumbnails']['default']['url'],
            videoCount: contentDetails['itemCount'] ?? 0,
          );
        }
      }
    } catch (e) {
      print('Error fetching playlist: $e');
    }
    return null;
  }

  // Fetch videos from a playlist
  static Future<List<Video>> fetchPlaylistVideos(
    String playlistYoutubeId, {
    int maxResults = 50,
  }) async {
    final videos = <Video>[];
    try {
      String? nextPageToken;
      do {
        final url = Uri.parse(
          '${AppConstants.youtubeBaseUrl}/playlistItems?part=snippet,contentDetails&playlistId=$playlistYoutubeId&maxResults=50${nextPageToken != null ? '&pageToken=$nextPageToken' : ''}&key=${AppConstants.youtubeApiKey}',
        );

        final response = await http.get(url);
        if (response.statusCode == 200) {
          final data = jsonDecode(response.body);
          nextPageToken = data['nextPageToken'];
          final items = data['items'] as List? ?? [];

          final videoIds = items
              .map((item) => item['contentDetails']['videoId'])
              .join(',');

          // Fetch video details for duration
          final detailsUrl = Uri.parse(
            '${AppConstants.youtubeBaseUrl}/videos?part=contentDetails&id=$videoIds&key=${AppConstants.youtubeApiKey}',
          );
          final detailsResponse = await http.get(detailsUrl);
          final durations = <String, String>{};

          if (detailsResponse.statusCode == 200) {
            final detailsData = jsonDecode(detailsResponse.body);
            for (final item in detailsData['items'] ?? []) {
              durations[item['id']] = item['contentDetails']['duration'] ?? '';
            }
          }

          for (final item in items) {
            final snippet = item['snippet'];
            final videoId = item['contentDetails']['videoId'];
            videos.add(Video(
              id: _uuid.v4(),
              youtubeId: videoId,
              title: snippet['title'],
              thumbnail: snippet['thumbnails']['high']?['url'] ??
                  snippet['thumbnails']['default']?['url'] ??
                  '',
              channelId: snippet['channelId'] ?? '',
              channelTitle: snippet['channelTitle'] ?? '',
              duration: durations[videoId] ?? '',
              playlistId: playlistYoutubeId,
            ));
          }
        } else {
          break;
        }
      } while (nextPageToken != null && videos.length < maxResults);
    } catch (e) {
      print('Error fetching playlist videos: $e');
    }
    return videos.take(maxResults).toList();
  }
}
