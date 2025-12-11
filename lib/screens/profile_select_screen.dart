import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../config/theme.dart';
import '../config/constants.dart';
import '../models/profile.dart';
import '../services/storage_service.dart';
import '../services/time_tracker.dart';
import '../widgets/pin_dialog.dart';
import 'kid/home_screen.dart';
import 'parent/dashboard_screen.dart';

class ProfileSelectScreen extends StatefulWidget {
  const ProfileSelectScreen({super.key});

  @override
  State<ProfileSelectScreen> createState() => _ProfileSelectScreenState();
}

class _ProfileSelectScreenState extends State<ProfileSelectScreen> {
  List<Profile> _profiles = [];

  @override
  void initState() {
    super.initState();
    _loadProfiles();
  }

  void _loadProfiles() {
    setState(() {
      _profiles = StorageService.getAllProfiles();
    });
  }

  void _selectProfile(Profile profile) {
    final timeTracker = context.read<TimeTracker>();
    timeTracker.setProfile(profile);
    StorageService.setActiveProfileId(profile.id);

    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const KidHomeScreen()),
    );
  }

  void _openParentMode() async {
    if (!StorageService.hasPin()) {
      // First time - set up PIN
      final pin = await showDialog<String>(
        context: context,
        barrierDismissible: false,
        builder: (_) => const PinDialog(isSetup: true),
      );
      if (pin != null) {
        await StorageService.setPin(pin);
        _navigateToParentDashboard();
      }
    } else {
      // Verify PIN
      final verified = await showDialog<bool>(
        context: context,
        builder: (_) => const PinDialog(isSetup: false),
      );
      if (verified == true) {
        _navigateToParentDashboard();
      }
    }
  }

  void _navigateToParentDashboard() {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => const ParentDashboardScreen()),
    ).then((_) => _loadProfiles());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Stack(
          children: [
            // Background decoration
            Positioned(
              top: 20,
              left: 20,
              child: Text('☁️', style: TextStyle(fontSize: 40)),
            ),
            Positioned(
              top: 60,
              right: 40,
              child: Text('☁️', style: TextStyle(fontSize: 30)),
            ),
            Positioned(
              bottom: 100,
              left: 30,
              child: Text('⭐', style: TextStyle(fontSize: 24)),
            ),
            Positioned(
              bottom: 150,
              right: 50,
              child: Text('⭐', style: TextStyle(fontSize: 20)),
            ),

            // Main content
            Column(
              children: [
                const SizedBox(height: 60),
                // Title
                Text(
                  '🌈 ${AppConstants.appName} 🌈',
                  style: Theme.of(context).textTheme.headlineLarge,
                ),
                const SizedBox(height: 8),
                Text(
                  'Who\'s watching?',
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
                const SizedBox(height: 40),

                // Profiles grid
                Expanded(
                  child: _profiles.isEmpty
                      ? _buildEmptyState()
                      : _buildProfilesGrid(),
                ),

                // Parent access hint
                Padding(
                  padding: const EdgeInsets.all(16),
                  child: Text(
                    'Long press ⚙️ for parent settings',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ),
              ],
            ),

            // Settings button
            Positioned(
              top: 16,
              right: 16,
              child: GestureDetector(
                onLongPress: _openParentMode,
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.white,
                    borderRadius: BorderRadius.circular(12),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.1),
                        blurRadius: 8,
                        offset: const Offset(0, 2),
                      ),
                    ],
                  ),
                  child: const Icon(
                    Icons.settings,
                    color: AppColors.textSecondary,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('👋', style: TextStyle(fontSize: 64)),
          const SizedBox(height: 16),
          Text(
            'No profiles yet',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 8),
          Text(
            'Long press the settings icon\nto add a profile',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyLarge,
          ),
        ],
      ),
    );
  }

  Widget _buildProfilesGrid() {
    return GridView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 32),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 24,
        mainAxisSpacing: 24,
        childAspectRatio: 0.85,
      ),
      itemCount: _profiles.length,
      itemBuilder: (context, index) {
        final profile = _profiles[index];
        return _buildProfileCard(profile);
      },
    );
  }

  Widget _buildProfileCard(Profile profile) {
    return GestureDetector(
      onTap: () => _selectProfile(profile),
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.white,
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.1),
              blurRadius: 12,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            // Avatar
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                color: AppColors.primary.withOpacity(0.2),
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  profile.avatar,
                  style: const TextStyle(fontSize: 48),
                ),
              ),
            ),
            const SizedBox(height: 12),
            // Name
            Text(
              profile.name,
              style: Theme.of(context).textTheme.titleMedium,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            // Time remaining
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.timer_outlined,
                  size: 16,
                  color: profile.hasTimeRemaining
                      ? AppColors.accent1
                      : AppColors.error,
                ),
                const SizedBox(width: 4),
                Text(
                  '${profile.remainingMinutes} min',
                  style: TextStyle(
                    fontSize: 12,
                    color: profile.hasTimeRemaining
                        ? AppColors.accent1
                        : AppColors.error,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
