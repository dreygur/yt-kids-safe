import 'package:hive/hive.dart';

part 'playlist.g.dart';

@HiveType(typeId: 3)
class Playlist extends HiveObject {
  @HiveField(0)
  String id;

  @HiveField(1)
  String youtubeId;

  @HiveField(2)
  String title;

  @HiveField(3)
  String thumbnail;

  @HiveField(4)
  String category;

  @HiveField(5)
  int videoCount;

  @HiveField(6)
  DateTime addedAt;

  Playlist({
    required this.id,
    required this.youtubeId,
    required this.title,
    required this.thumbnail,
    this.category = 'All',
    this.videoCount = 0,
    DateTime? addedAt,
  }) : addedAt = addedAt ?? DateTime.now();

  Map<String, dynamic> toJson() => {
        'id': id,
        'youtubeId': youtubeId,
        'title': title,
        'thumbnail': thumbnail,
        'category': category,
        'videoCount': videoCount,
        'addedAt': addedAt.toIso8601String(),
      };

  factory Playlist.fromJson(Map<String, dynamic> json) => Playlist(
        id: json['id'],
        youtubeId: json['youtubeId'],
        title: json['title'],
        thumbnail: json['thumbnail'],
        category: json['category'] ?? 'All',
        videoCount: json['videoCount'] ?? 0,
        addedAt: DateTime.parse(json['addedAt']),
      );
}
