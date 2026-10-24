package com.harf.ai.audio

import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Plays one local recording at a time; [playingPath] tells the UI which one. */
class AudioPlayer {
    private var player: MediaPlayer? = null
    private val _playingPath = MutableStateFlow<String?>(null)
    val playingPath: StateFlow<String?> = _playingPath

    fun toggle(path: String) {
        if (_playingPath.value == path) stop() else play(path)
    }

    private fun play(path: String) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener { stop() }
            prepare()
            start()
        }
        _playingPath.value = path
    }

    fun stop() {
        player?.release()
        player = null
        _playingPath.value = null
    }
}
