import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';
import '../../config/theme.dart';
import '../../config/constants.dart';
import '../../models/channel.dart';
import '../../services/storage_service.dart';
import '../../services/youtube_service.dart';

class ParentChannelsScreen extends StatefulWidget {
  const ParentChannelsScreen({super.key});

  @override
  State<ParentChannelsScreen> createState() => _ParentChannelsScreenState();
}

class _ParentChannelsScreenState extends State<ParentChannelsScreen> {
  List<Channel> _channels = [];
  bool _isLoading = false;

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

  void _addChannel() {
    final urlController = TextEditingController();
    String selectedCategory = 'All';

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('Add Channel'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              TextField(
                controller: urlController,
                decoration: const InputDecoration(
                  labelText: 'YouTube Channel URL',
                  hintText: 'https://youtube.com/@channelname',
                ),
              ),
              const SizedBox(height: 16),
              const Text('Category'),
              const SizedBox(height: 8),
              DropdownButton<String>(
                value: selectedCategory,
                isExpanded: true,
                items: AppConstants.defaultCategories
                    .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                    .toList(),
                onChanged: (value) {
                  setDialogState(() {
                    selectedCategory = value!;
                  });
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                final url = urlController.text.trim();
                if (url.isEmpty) return;

                Navigator.pop(context);
                await _fetchAndAddChannel(url, selectedCategory);
              },
              child: const Text('Add'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _fetchAndAddChannel(String url, String category) async {
    setState(() => _isLoading = true);

    try {
      final channelIdOrHandle = YouTubeService.extractChannelId(url);
      if (channelIdOrHandle == null) {
        throw Exception('Invalid YouTube channel URL');
      }

      final channel = await YouTubeService.fetchChannelInfo(channelIdOrHandle);
      if (channel == null) {
        throw Exception('Could not fetch channel info');
      }

      channel.category = category;
      await StorageService.saveChannel(channel);

      // Fetch channel videos
      final videos =
          await YouTubeService.fetchChannelVideos(channel.youtubeId);
      await StorageService.saveVideos(videos);

      _loadChannels();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Added "${channel.title}" with ${videos.length} videos'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _refreshChannelVideos(Channel channel) async {
    setState(() => _isLoading = true);

    try {
      final videos =
          await YouTubeService.fetchChannelVideos(channel.youtubeId);
      await StorageService.saveVideos(videos);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Refreshed ${videos.length} videos')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _deleteChannel(Channel channel) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Channel?'),
        content: Text(
          'Are you sure you want to delete "${channel.title}"? All associated videos will also be removed.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.error),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirm == true) {
      await StorageService.deleteChannel(channel.id);
      _loadChannels();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Channels'),
        backgroundColor: AppColors.white,
        foregroundColor: AppColors.textPrimary,
        elevation: 0,
      ),
      body: Stack(
        children: [
          _channels.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Text('📺', style: TextStyle(fontSize: 64)),
                      const SizedBox(height: 16),
                      Text(
                        'No channels yet',
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      const SizedBox(height: 8),
                      const Text('Add YouTube channels for your child'),
                    ],
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _channels.length,
                  itemBuilder: (context, index) {
                    final channel = _channels[index];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      child: ListTile(
                        leading: ClipRRect(
                          borderRadius: BorderRadius.circular(24),
                          child: CachedNetworkImage(
                            imageUrl: channel.thumbnail,
                            width: 48,
                            height: 48,
                            fit: BoxFit.cover,
                            placeholder: (context, url) => Container(
                              width: 48,
                              height: 48,
                              color: AppColors.primary.withOpacity(0.2),
                            ),
                            errorWidget: (context, url, error) => Container(
                              width: 48,
                              height: 48,
                              color: AppColors.primary.withOpacity(0.2),
                              child:
                                  const Icon(Icons.tv, color: AppColors.primary),
                            ),
                          ),
                        ),
                        title: Text(channel.title),
                        subtitle: Text(channel.category),
                        trailing: PopupMenuButton<String>(
                          onSelected: (value) {
                            if (value == 'refresh') {
                              _refreshChannelVideos(channel);
                            } else if (value == 'delete') {
                              _deleteChannel(channel);
                            }
                          },
                          itemBuilder: (context) => [
                            const PopupMenuItem(
                              value: 'refresh',
                              child: Row(
                                children: [
                                  Icon(Icons.refresh),
                                  SizedBox(width: 8),
                                  Text('Refresh Videos'),
                                ],
                              ),
                            ),
                            const PopupMenuItem(
                              value: 'delete',
                              child: Row(
                                children: [
                                  Icon(Icons.delete, color: AppColors.error),
                                  SizedBox(width: 8),
                                  Text('Delete',
                                      style: TextStyle(color: AppColors.error)),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
          if (_isLoading)
            Container(
              color: Colors.black26,
              child: const Center(child: CircularProgressIndicator()),
            ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addChannel,
        child: const Icon(Icons.add),
      ),
    );
  }
}
