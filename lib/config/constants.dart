class AppConstants {
  static const String appName = 'YT Kids Safe';
  static const int defaultDailyLimitMinutes = 60;
  static const int pinLength = 4;

  // Hive box names
  static const String profilesBox = 'profiles';
  static const String channelsBox = 'channels';
  static const String playlistsBox = 'playlists';
  static const String videosBox = 'videos';
  static const String settingsBox = 'settings';

  // SharedPreferences keys
  static const String pinKey = 'app_pin';
  static const String activeProfileKey = 'active_profile';
  static const String lastResetDateKey = 'last_reset_date';

  // YouTube API
  static const String youtubeApiKey = 'AIzaSyDzPHxSHXSr22veQGUmYKAO5tJrpWHKJ1c'; // TODO: Add your key
  static const String youtubeBaseUrl = 'https://www.googleapis.com/youtube/v3';

  // Avatars
  static const List<String> avatars = [
    '🐻', '🦊', '🐰', '🐼',
    '🦁', '🐸', '🐱', '🐶',
    '🦄', '🐧', '🦋', '🐵',
  ];

  // Categories
  static const List<String> defaultCategories = [
    'All',
    'Cartoons',
    'Learning',
    'Music',
    'Stories',
  ];
}
