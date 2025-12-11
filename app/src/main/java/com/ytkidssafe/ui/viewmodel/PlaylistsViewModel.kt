package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.PlaylistRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.video.extractor.YouTubeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val videoRepository: VideoRepository,
    private val youTubeService: YouTubeService
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _importSuccess = MutableStateFlow(false)
    val importSuccess: StateFlow<Boolean> = _importSuccess.asStateFlow()

    fun importPlaylist(url: String, category: String = "All") {
        val ytPlaylistId = youTubeService.parsePlaylistId(url)
        if (ytPlaylistId == null) {
            _error.value = "Invalid playlist URL"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            youTubeService.fetchPlaylist(ytPlaylistId)
                .onSuccess { playlistInfo ->
                    // Save playlist to database
                    val playlist = playlistRepository.addPlaylist(
                        youtubeId = playlistInfo.id,
                        title = playlistInfo.title,
                        thumbnailUrl = playlistInfo.thumbnailUrl,
                        videoCount = playlistInfo.videoCount,
                        category = category
                    )

                    // Save videos linked to this playlist
                    for (video in playlistInfo.videos) {
                        videoRepository.addVideo(
                            youtubeId = video.youtubeId,
                            title = video.title,
                            thumbnailUrl = video.thumbnailUrl,
                            playlistId = playlist.id,
                            duration = video.duration
                        )
                    }

                    _importSuccess.value = true
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to import playlist"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _importSuccess.value = false
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            // Delete videos belonging to this playlist first
            videoRepository.deleteVideosByPlaylist(playlist.id)
            playlistRepository.deletePlaylist(playlist)
        }
    }
}
