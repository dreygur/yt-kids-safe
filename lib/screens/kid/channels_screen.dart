import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../config/theme.dart';
import '../../models/channel.dart';
import '../../models/video.dart';
import '../../services/storage_service.dart';
import '../../widgets/video_card.dart';
import 'player_screen.dart';

class KidChannelsScreen extends StatefulWidget {
  const KidChannelsScreen({super.key});

  @override
  State<KidChannelsScreen> createState() => _KidChannelsScreenState();
}

class _KidChannelsScreenState extends State<KidChannelsScreen> {
  List<Channel> _channels = [];
  Channel? _selectedChannel;
  List<Video> _channelVideos = [];

  @override
  void initState() {
    super.initState();
    _loadChannels();
  }

  void _loadChannels() {
    setState(() {
      _channels = StorageService.getAllChannels();
    });
  }

  void _selectChannel(Channel channel) {
    setState(() {
      _selectedChannel = channel;
      _channelVideos = StorageService.getAllVideos()
          .where((v) => v.channelId == channel.youtubeId)
          .toList();
    });
  }

  void _playVideo(Video video) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => PlayerScreen(video: video),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_selectedChannel?.title ?? 'Channels'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_rounded),
          onPressed: () {
            if (_selectedChannel != null) {
              setState(() {
                _selectedChannel = null;
                _channelVideos = [];
              });
            } else {
              Navigator.of(context).pop();
            }
          },
        ),
      ),
      body: _selectedChannel != null
          ? _buildChannelVideos()
          : _buildChannelGrid(),
    );
  }

  Widget _buildChannelGrid() {
    if (_channels.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('📺', style: TextStyle(fontSize: 64)),
            const SizedBox(height: 16),
            Text(
              'No channels yet',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
          ],
        ),
      );
    }

    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 16,
        mainAxisSpacing: 16,
        childAspectRatio: 1,
      ),
      itemCount: _channels.length,
      itemBuilder: (context, index) {
        final channel = _channels[index];
        return _buildChannelCard(channel);
      },
    );
  }

  Widget _buildChannelCard(Channel channel) {
    return GestureDetector(
      onTap: () => _selectChannel(channel),
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.white,
          borderRadius: BorderRadius.circular(16),
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
            // Channel thumbnail
            ClipRRect(
              borderRadius: BorderRadius.circular(40),
              child: CachedNetworkImage(
                imageUrl: channel.thumbnail,
                width: 80,
                height: 80,
                fit: BoxFit.cover,
                placeholder: (context, url) => Container(
                  width: 80,
                  height: 80,
                  color: AppColors.primary.withOpacity(0.2),
                ),
                errorWidget: (context, url, error) => Container(
                  width: 80,
                  height: 80,
                  color: AppColors.primary.withOpacity(0.2),
                  child: const Icon(Icons.tv, color: AppColors.primary),
                ),
              ),
            ),
            const SizedBox(height: 12),
            // Channel name
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 8),
              child: Text(
                channel.title,
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildChannelVideos() {
    if (_channelVideos.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('🎬', style: TextStyle(fontSize: 64)),
            const SizedBox(height: 16),
            Text(
              'No videos loaded yet',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 8),
            Text(
              'Ask your parent to refresh videos',
              style: Theme.of(context).textTheme.bodyLarge,
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: _channelVideos.length,
      itemBuilder: (context, index) {
        final video = _channelVideos[index];
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
}
