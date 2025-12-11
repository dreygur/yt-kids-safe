package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.PlaylistRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.Playlist
import com.ytkidssafe.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistVideosViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _playlist = MutableStateFlow<Playlist?>(null)
    val playlist: StateFlow<Playlist?> = _playlist.asStateFlow()

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    fun loadPlaylist(playlistId: String) {
        viewModelScope.launch {
            _playlist.value = playlistRepository.getPlaylistById(playlistId)

            videoRepository.getVideosByPlaylist(playlistId).collect { videoList ->
                _videos.value = videoList
            }
        }
    }
}
