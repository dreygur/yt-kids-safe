import 'package:hive/hive.dart';

part 'video.g.dart';

@HiveType(typeId: 2)
class Video extends HiveObject {
  @HiveField(0)
  String id;

  @HiveField(1)
  String youtubeId;

  @HiveField(2)
  String title;

  @HiveField(3)
  String thumbnail;

  @HiveField(4)
  String channelId;

  @HiveField(5)
  String channelTitle;

  @HiveField(6)
  String duration;

  @HiveField(7)
  DateTime cachedAt;

  @HiveField(8)
  String? playlistId;

  Video({
    required this.id,
    required this.youtubeId,
    required this.title,
    required this.thumbnail,
    required this.channelId,
    required this.channelTitle,
    this.duration = '',
    DateTime? cachedAt,
    this.playlistId,
  }) : cachedAt = cachedAt ?? DateTime.now();

  String get formattedDuration {
    if (duration.isEmpty) return '';
    // Parse ISO 8601 duration (PT1H2M3S)
    final regex = RegExp(r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?');
    final match = regex.firstMatch(duration);
    if (match == null) return duration;

    final hours = int.tryParse(match.group(1) ?? '') ?? 0;
    final minutes = int.tryParse(match.group(2) ?? '') ?? 0;
    final seconds = int.tryParse(match.group(3) ?? '') ?? 0;

    if (hours > 0) {
      return '$hours:${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
    }
    return '$minutes:${seconds.toString().padLeft(2, '0')}';
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'youtubeId': youtubeId,
        'title': title,
        'thumbnail': thumbnail,
        'channelId': channelId,
        'channelTitle': channelTitle,
        'duration': duration,
        'cachedAt': cachedAt.toIso8601String(),
        'playlistId': playlistId,
      };

  factory Video.fromJson(Map<String, dynamic> json) => Video(
        id: json['id'],
        youtubeId: json['youtubeId'],
        title: json['title'],
        thumbnail: json['thumbnail'],
        channelId: json['channelId'],
        channelTitle: json['channelTitle'],
        duration: json['duration'] ?? '',
        cachedAt: DateTime.parse(json['cachedAt']),
        playlistId: json['playlistId'],
      );
}
