import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../config/theme.dart';
import '../../config/constants.dart';
import '../../models/profile.dart';
import '../../models/video.dart';
import '../../models/channel.dart';
import '../../services/storage_service.dart';
import '../../services/time_tracker.dart';
import '../../widgets/video_card.dart';
import '../../widgets/time_bar.dart';
import '../profile_select_screen.dart';
import 'player_screen.dart';
import 'channels_screen.dart';
import 'times_up_screen.dart';

class KidHomeScreen extends StatefulWidget {
  const KidHomeScreen({super.key});

  @override
  State<KidHomeScreen> createState() => _KidHomeScreenState();
}

class _KidHomeScreenState extends State<KidHomeScreen> {
  int _currentIndex = 0;
  String _selectedCategory = 'All';
  List<Video> _videos = [];
  List<Channel> _channels = [];
  Profile? _profile;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() {
    _profile = StorageService.getActiveProfile();
    if (_profile == null) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const ProfileSelectScreen()),
        );
      });
      return;
    }

    // Check if time is up
    if (!_profile!.hasTimeRemaining) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const TimesUpScreen()),
        );
      });
      return;
    }

    setState(() {
      _channels = StorageService.getAllChannels();
      _videos = StorageService.getAllVideos();
    });
  }

  List<Video> get _filteredVideos {
    if (_selectedCategory == 'All') {
      return _videos;
    }
    // Filter by channel category
    final categoryChannelIds = _channels
        .where((c) => c.category == _selectedCategory)
        .map((c) => c.youtubeId)
        .toSet();
    return _videos.where((v) => categoryChannelIds.contains(v.channelId)).toList();
  }

  void _playVideo(Video video) {
    final timeTracker = context.read<TimeTracker>();
    if (_profile != null && !_profile!.hasTimeRemaining) {
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const TimesUpScreen()),
      );
      return;
    }

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => PlayerScreen(video: video),
      ),
    ).then((_) {
      // Refresh profile data
      setState(() {
        _profile = StorageService.getActiveProfile();
      });
      if (_profile != null && !_profile!.hasTimeRemaining) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const TimesUpScreen()),
        );
      }
    });
  }

  void _switchProfile() {
    final timeTracker = context.read<TimeTracker>();
    timeTracker.setProfile(null);
    StorageService.setActiveProfileId(null);
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const ProfileSelectScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (_profile == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            // Header
            _buildHeader(),
            // Category pills
            _buildCategoryPills(),
            // Video list
            Expanded(
              child: _videos.isEmpty ? _buildEmptyState() : _buildVideoList(),
            ),
          ],
        ),
      ),
      bottomNavigationBar: _buildBottomNav(),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.all(16),
      color: AppColors.primary,
      child: Row(
        children: [
          // Greeting
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '👋 Hi ${_profile!.name}!',
                  style: const TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: AppColors.white,
                  ),
                ),
                const SizedBox(height: 4),
                TimeBar(profile: _profile!),
              ],
            ),
          ),
          // Profile avatar
          GestureDetector(
            onTap: _switchProfile,
            child: Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: AppColors.white,
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  _profile!.avatar,
                  style: const TextStyle(fontSize: 28),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryPills() {
    return Container(
      height: 50,
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: AppConstants.defaultCategories.length,
        itemBuilder: (context, index) {
          final category = AppConstants.defaultCategories[index];
          final isSelected = category == _selectedCategory;
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: FilterChip(
              label: Text(category),
              selected: isSelected,
              onSelected: (_) {
                setState(() {
                  _selectedCategory = category;
                });
              },
              backgroundColor: AppColors.white,
              selectedColor: AppColors.primary,
              labelStyle: TextStyle(
                color: isSelected ? AppColors.white : AppColors.textPrimary,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
              ),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildVideoList() {
    final videos = _filteredVideos;
    if (videos.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('🔍', style: TextStyle(fontSize: 64)),
            const SizedBox(height: 16),
            Text(
              'No videos in this category',
              style: Theme.of(context).textTheme.titleMedium,
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: videos.length,
      itemBuilder: (context, index) {
        final video = videos[index];
        return Padding(
          padding: const EdgeInsets.only(bottom: 16),
          child: VideoCard(
            video: video,
            onTap: () => _playVideo(video),
          ),
        );
      },
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Text('📺', style: TextStyle(fontSize: 64)),
          const SizedBox(height: 16),
          Text(
            'No videos yet',
            style: Theme.of(context).textTheme.headlineMedium,
          ),
          const SizedBox(height: 8),
          Text(
            'Ask your parent to add some\nchannels or playlists',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyLarge,
          ),
        ],
      ),
    );
  }

  Widget _buildBottomNav() {
    return BottomNavigationBar(
      currentIndex: _currentIndex,
      onTap: (index) {
        if (index == 1) {
          Navigator.of(context).push(
            MaterialPageRoute(builder: (_) => const KidChannelsScreen()),
          );
        } else if (index == 2) {
          _switchProfile();
        } else {
          setState(() {
            _currentIndex = index;
          });
        }
      },
      items: const [
        BottomNavigationBarItem(
          icon: Icon(Icons.home_rounded),
          label: 'Home',
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.tv_rounded),
          label: 'Channels',
        ),
        BottomNavigationBarItem(
          icon: Icon(Icons.face_rounded),
          label: 'Profile',
        ),
      ],
    );
  }
}
