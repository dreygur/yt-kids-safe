import 'package:flutter/material.dart';
import 'dart:convert';
import 'dart:io';
import 'package:file_picker/file_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../../config/theme.dart';
import '../../services/storage_service.dart';
import 'profiles_screen.dart';
import 'channels_screen.dart';
import 'playlists_screen.dart';
import 'settings_screen.dart';

class ParentDashboardScreen extends StatefulWidget {
  const ParentDashboardScreen({super.key});

  @override
  State<ParentDashboardScreen> createState() => _ParentDashboardScreenState();
}

class _ParentDashboardScreenState extends State<ParentDashboardScreen> {
  int _profileCount = 0;
  int _channelCount = 0;
  int _playlistCount = 0;

  @override
  void initState() {
    super.initState();
    _loadCounts();
  }

  void _loadCounts() {
    setState(() {
      _profileCount = StorageService.getAllProfiles().length;
      _channelCount = StorageService.getAllChannels().length;
      _playlistCount = StorageService.getAllPlaylists().length;
    });
  }

  Future<void> _exportBackup() async {
    try {
      final json = StorageService.exportToJson();
      final directory = await getApplicationDocumentsDirectory();
      final file = File(
        '${directory.path}/yt_kids_safe_backup_${DateTime.now().millisecondsSinceEpoch}.json',
      );
      await file.writeAsString(json);

      await Share.shareXFiles(
        [XFile(file.path)],
        subject: 'YT Kids Safe Backup',
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Backup exported successfully')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Export failed: $e')),
        );
      }
    }
  }

  Future<void> _importBackup() async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['json'],
      );

      if (result != null && result.files.single.path != null) {
        final file = File(result.files.single.path!);
        final json = await file.readAsString();

        // Validate JSON
        final data = jsonDecode(json) as Map<String, dynamic>;
        if (!data.containsKey('version') || !data.containsKey('profiles')) {
          throw Exception('Invalid backup file format');
        }

        final confirm = await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Import Backup?'),
            content: const Text(
              'This will replace all current data with the backup. This action cannot be undone.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('Cancel'),
              ),
              ElevatedButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('Import'),
              ),
            ],
          ),
        );

        if (confirm == true) {
          await StorageService.importFromJson(json);
          _loadCounts();
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Backup imported successfully')),
            );
          }
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Import failed: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Parent Settings'),
        backgroundColor: AppColors.white,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Profiles section
          _buildSection(
            icon: Icons.face_rounded,
            title: 'Profiles',
            subtitle: '$_profileCount profile${_profileCount != 1 ? 's' : ''}',
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ProfilesScreen()),
              );
              _loadCounts();
            },
          ),

          // Channels section
          _buildSection(
            icon: Icons.tv_rounded,
            title: 'Channels',
            subtitle: '$_channelCount channel${_channelCount != 1 ? 's' : ''}',
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const ParentChannelsScreen()),
              );
              _loadCounts();
            },
          ),

          // Playlists section
          _buildSection(
            icon: Icons.playlist_play_rounded,
            title: 'Playlists',
            subtitle: '$_playlistCount playlist${_playlistCount != 1 ? 's' : ''}',
            onTap: () async {
              await Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const PlaylistsScreen()),
              );
              _loadCounts();
            },
          ),

          const Divider(height: 32),

          // Settings section
          _buildSection(
            icon: Icons.lock_rounded,
            title: 'Change PIN',
            subtitle: 'Update parent access PIN',
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const SettingsScreen()),
              );
            },
          ),

          const Divider(height: 32),

          // Backup section
          _buildSection(
            icon: Icons.upload_rounded,
            title: 'Export Backup',
            subtitle: 'Save all data to a file',
            onTap: _exportBackup,
          ),

          _buildSection(
            icon: Icons.download_rounded,
            title: 'Import Backup',
            subtitle: 'Restore from a backup file',
            onTap: _importBackup,
          ),
        ],
      ),
    );
  }

  Widget _buildSection({
    required IconData icon,
    required String title,
    required String subtitle,
    required VoidCallback onTap,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: AppColors.primary.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(icon, color: AppColors.primary),
        ),
        title: Text(title),
        subtitle: Text(subtitle),
        trailing: const Icon(Icons.chevron_right_rounded),
        onTap: onTap,
      ),
    );
  }
}
