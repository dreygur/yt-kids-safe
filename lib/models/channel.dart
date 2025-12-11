import 'package:hive/hive.dart';

part 'channel.g.dart';

@HiveType(typeId: 1)
class Channel extends HiveObject {
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
  DateTime addedAt;

  Channel({
    required this.id,
    required this.youtubeId,
    required this.title,
    required this.thumbnail,
    this.category = 'All',
    DateTime? addedAt,
  }) : addedAt = addedAt ?? DateTime.now();

  Map<String, dynamic> toJson() => {
        'id': id,
        'youtubeId': youtubeId,
        'title': title,
        'thumbnail': thumbnail,
        'category': category,
        'addedAt': addedAt.toIso8601String(),
      };

  factory Channel.fromJson(Map<String, dynamic> json) => Channel(
        id: json['id'],
        youtubeId: json['youtubeId'],
        title: json['title'],
        thumbnail: json['thumbnail'],
        category: json['category'] ?? 'All',
        addedAt: DateTime.parse(json['addedAt']),
      );
}
