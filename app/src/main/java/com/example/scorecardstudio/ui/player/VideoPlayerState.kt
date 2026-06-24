package com.example.scorecardstudio.ui.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class VideoPlayRequest(
    val url: String,
    val title: String
)

object VideoPlayerState {
    private val _playRequest = MutableStateFlow<VideoPlayRequest?>(null)
    val playRequest: StateFlow<VideoPlayRequest?> = _playRequest

    fun requestPlay(url: String, title: String) {
        _playRequest.value = VideoPlayRequest(url, title)
    }

    fun clearRequest() {
        _playRequest.value = null
    }
}
