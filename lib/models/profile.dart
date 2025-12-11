import 'package:hive/hive.dart';

part 'profile.g.dart';

@HiveType(typeId: 0)
class Profile extends HiveObject {
  @HiveField(0)
  String id;

  @HiveField(1)
  String name;

  @HiveField(2)
  String avatar;

  @HiveField(3)
  int dailyLimitMinutes;

  @HiveField(4)
  int usedTodayMinutes;

  @HiveField(5)
  DateTime lastResetDate;

  @HiveField(6)
  List<String> allowedChannelIds;

  @HiveField(7)
  List<String> allowedPlaylistIds;

  Profile({
    required this.id,
    required this.name,
    required this.avatar,
    this.dailyLimitMinutes = 60,
    this.usedTodayMinutes = 0,
    DateTime? lastResetDate,
    List<String>? allowedChannelIds,
    List<String>? allowedPlaylistIds,
  })  : lastResetDate = lastResetDate ?? DateTime.now(),
        allowedChannelIds = allowedChannelIds ?? [],
        allowedPlaylistIds = allowedPlaylistIds ?? [];

  int get remainingMinutes {
    _checkAndResetDaily();
    return (dailyLimitMinutes - usedTodayMinutes).clamp(0, dailyLimitMinutes);
  }

  bool get hasTimeRemaining => remainingMinutes > 0;

  void addWatchTime(int minutes) {
    _checkAndResetDaily();
    usedTodayMinutes += minutes;
    save();
  }

  void _checkAndResetDaily() {
    final now = DateTime.now();
    final lastReset = DateTime(lastResetDate.year, lastResetDate.month, lastResetDate.day);
    final today = DateTime(now.year, now.month, now.day);

    if (today.isAfter(lastReset)) {
      usedTodayMinutes = 0;
      lastResetDate = now;
    }
  }

  Map<String, dynamic> toJson() => {
        'id': id,
        'name': name,
        'avatar': avatar,
        'dailyLimitMinutes': dailyLimitMinutes,
        'usedTodayMinutes': usedTodayMinutes,
        'lastResetDate': lastResetDate.toIso8601String(),
        'allowedChannelIds': allowedChannelIds,
        'allowedPlaylistIds': allowedPlaylistIds,
      };

  factory Profile.fromJson(Map<String, dynamic> json) => Profile(
        id: json['id'],
        name: json['name'],
        avatar: json['avatar'],
        dailyLimitMinutes: json['dailyLimitMinutes'] ?? 60,
        usedTodayMinutes: json['usedTodayMinutes'] ?? 0,
        lastResetDate: DateTime.parse(json['lastResetDate']),
        allowedChannelIds: List<String>.from(json['allowedChannelIds'] ?? []),
        allowedPlaylistIds: List<String>.from(json['allowedPlaylistIds'] ?? []),
      );
}
