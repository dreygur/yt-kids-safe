import 'package:flutter/material.dart';
import '../../config/theme.dart';
import '../../services/storage_service.dart';
import '../../widgets/pin_dialog.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  void _changePin(BuildContext context) async {
    // Verify current PIN
    final verified = await showDialog<bool>(
      context: context,
      builder: (_) => const PinDialog(isSetup: false),
    );

    if (verified != true) return;

    // Set new PIN
    final newPin = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (_) => const PinDialog(isSetup: true),
    );

    if (newPin != null) {
      await StorageService.setPin(newPin);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('PIN changed successfully')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
        backgroundColor: AppColors.white,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: ListTile(
              leading: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: AppColors.primary.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: const Icon(Icons.lock_rounded, color: AppColors.primary),
              ),
              title: const Text('Change PIN'),
              subtitle: const Text('Update your parent access PIN'),
              trailing: const Icon(Icons.chevron_right_rounded),
              onTap: () => _changePin(context),
            ),
          ),
          const SizedBox(height: 24),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 16),
            child: Text(
              'About',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: AppColors.textSecondary,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'YT Kids Safe',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Version 1.0.0',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'A parent-controlled YouTube app that lets you whitelist channels and playlists for your kids to watch safely.',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
