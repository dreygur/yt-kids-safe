package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.ChannelRepository
import com.ytkidssafe.data.repository.VideoRepository
import com.ytkidssafe.domain.model.Channel
import com.ytkidssafe.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelVideosViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _channel = MutableStateFlow<Channel?>(null)
    val channel: StateFlow<Channel?> = _channel.asStateFlow()

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    fun loadChannel(channelId: String) {
        viewModelScope.launch {
            _channel.value = channelRepository.getChannelById(channelId)

            videoRepository.getVideosByChannel(channelId).collect { videoList ->
                _videos.value = videoList
            }
        }
    }
}
