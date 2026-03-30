package com.example.newaudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioPlayerViewModel : ViewModel() {
    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress = _progress.asStateFlow()

    private var currentIndex = 0

    fun loadTracks(newTracks: List<AudioTrack>) {
        _tracks.value = newTracks
        if (newTracks.isNotEmpty()) _currentTrack.value = newTracks[0]
    }

    fun playPause() { _isPlaying.value = !_isPlaying.value }
    fun next() {
        if (tracks.value.isEmpty()) return
        currentIndex = (currentIndex + 1) % tracks.value.size
        _currentTrack.value = tracks.value[currentIndex]
    }
    fun previous() {
        if (tracks.value.isEmpty()) return
        currentIndex = if (currentIndex == 0) tracks.value.size - 1 else currentIndex - 1
        _currentTrack.value = tracks.value[currentIndex]
    }
    fun seekTo(position: Long) { _progress.value = position }
}
