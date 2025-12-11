import 'dart:convert';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../config/constants.dart';
import '../models/profile.dart';
import '../models/channel.dart';
import '../models/video.dart';
import '../models/playlist.dart';

class StorageService {
  static late Box<Profile> _profilesBox;
  static late Box<Channel> _channelsBox;
  static late Box<Video> _videosBox;
  static late Box<Playlist> _playlistsBox;
  static late SharedPreferences _prefs;

  static Future<void> init() async {
    await Hive.initFlutter();

    // Register adapters
    Hive.registerAdapter(ProfileAdapter());
    Hive.registerAdapter(ChannelAdapter());
    Hive.registerAdapter(VideoAdapter());
    Hive.registerAdapter(PlaylistAdapter());

    // Open boxes
    _profilesBox = await Hive.openBox<Profile>(AppConstants.profilesBox);
    _channelsBox = await Hive.openBox<Channel>(AppConstants.channelsBox);
    _videosBox = await Hive.openBox<Video>(AppConstants.videosBox);
    _playlistsBox = await Hive.openBox<Playlist>(AppConstants.playlistsBox);

    _prefs = await SharedPreferences.getInstance();
  }

  // PIN Management
  static String? getPin() => _prefs.getString(AppConstants.pinKey);

  static Future<void> setPin(String pin) async {
    await _prefs.setString(AppConstants.pinKey, pin);
  }

  static bool hasPin() => _prefs.containsKey(AppConstants.pinKey);

  static bool verifyPin(String pin) => getPin() == pin;

  // Active Profile
  static String? getActiveProfileId() =>
      _prefs.getString(AppConstants.activeProfileKey);

  static Future<void> setActiveProfileId(String? id) async {
    if (id == null) {
      await _prefs.remove(AppConstants.activeProfileKey);
    } else {
      await _prefs.setString(AppConstants.activeProfileKey, id);
    }
  }

  // Profiles
  static List<Profile> getAllProfiles() => _profilesBox.values.toList();

  static Profile? getProfile(String id) => _profilesBox.get(id);

  static Profile? getActiveProfile() {
    final id = getActiveProfileId();
    return id != null ? getProfile(id) : null;
  }

  static Future<void> saveProfile(Profile profile) async {
    await _profilesBox.put(profile.id, profile);
  }

  static Future<void> deleteProfile(String id) async {
    await _profilesBox.delete(id);
    if (getActiveProfileId() == id) {
      await setActiveProfileId(null);
    }
  }

  // Channels
  static List<Channel> getAllChannels() => _channelsBox.values.toList();

  static Channel? getChannel(String id) => _channelsBox.get(id);

  static Future<void> saveChannel(Channel channel) async {
    await _channelsBox.put(channel.id, channel);
  }

  static Future<void> deleteChannel(String id) async {
    await _channelsBox.delete(id);
    // Also delete associated videos
    final videosToDelete =
        _videosBox.values.where((v) => v.channelId == id).toList();
    for (final video in videosToDelete) {
      await _videosBox.delete(video.id);
    }
  }

  // Playlists
  static List<Playlist> getAllPlaylists() => _playlistsBox.values.toList();

  static Playlist? getPlaylist(String id) => _playlistsBox.get(id);

  static Future<void> savePlaylist(Playlist playlist) async {
    await _playlistsBox.put(playlist.id, playlist);
  }

  static Future<void> deletePlaylist(String id) async {
    await _playlistsBox.delete(id);
    // Also delete associated videos
    final videosToDelete =
        _videosBox.values.where((v) => v.playlistId == id).toList();
    for (final video in videosToDelete) {
      await _videosBox.delete(video.id);
    }
  }

  // Videos
  static List<Video> getAllVideos() => _videosBox.values.toList();

  static List<Video> getVideosByChannel(String channelId) =>
      _videosBox.values.where((v) => v.channelId == channelId).toList();

  static List<Video> getVideosByPlaylist(String playlistId) =>
      _videosBox.values.where((v) => v.playlistId == playlistId).toList();

  static Future<void> saveVideo(Video video) async {
    await _videosBox.put(video.id, video);
  }

  static Future<void> saveVideos(List<Video> videos) async {
    for (final video in videos) {
      await _videosBox.put(video.id, video);
    }
  }

  static Future<void> deleteVideo(String id) async {
    await _videosBox.delete(id);
  }

  // Backup & Restore
  static Map<String, dynamic> exportData() {
    return {
      'version': 1,
      'exportedAt': DateTime.now().toIso8601String(),
      'profiles': getAllProfiles().map((p) => p.toJson()).toList(),
      'channels': getAllChannels().map((c) => c.toJson()).toList(),
      'playlists': getAllPlaylists().map((p) => p.toJson()).toList(),
      'videos': getAllVideos().map((v) => v.toJson()).toList(),
      'settings': {
        'pin': getPin(),
      },
    };
  }

  static String exportToJson() => jsonEncode(exportData());

  static Future<void> importFromJson(String jsonString) async {
    final data = jsonDecode(jsonString) as Map<String, dynamic>;

    // Clear existing data
    await _profilesBox.clear();
    await _channelsBox.clear();
    await _playlistsBox.clear();
    await _videosBox.clear();

    // Import profiles
    final profiles = (data['profiles'] as List?)
            ?.map((p) => Profile.fromJson(p))
            .toList() ??
        [];
    for (final profile in profiles) {
      await saveProfile(profile);
    }

    // Import channels
    final channels = (data['channels'] as List?)
            ?.map((c) => Channel.fromJson(c))
            .toList() ??
        [];
    for (final channel in channels) {
      await saveChannel(channel);
    }

    // Import playlists
    final playlists = (data['playlists'] as List?)
            ?.map((p) => Playlist.fromJson(p))
            .toList() ??
        [];
    for (final playlist in playlists) {
      await savePlaylist(playlist);
    }

    // Import videos
    final videos =
        (data['videos'] as List?)?.map((v) => Video.fromJson(v)).toList() ?? [];
    await saveVideos(videos);

    // Import settings
    final settings = data['settings'] as Map<String, dynamic>?;
    if (settings != null && settings['pin'] != null) {
      await setPin(settings['pin']);
    }
  }
}
