package com.harf.ai.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.harf.ai.HarfApp
import com.harf.ai.audio.AudioImporter
import com.harf.ai.audio.WavRecorder
import com.harf.ai.data.Attempt
import com.harf.ai.data.Letters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface PracticeMessage {
    data object TooShort : PracticeMessage
    data object Sent : PracticeMessage
    data class RecordFailed(val error: String) : PracticeMessage
    data object ImportFailed : PracticeMessage
}

/** Holds the recording session so it survives rotation; everything else comes from the repository. */
class PracticeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as HarfApp).repository
    private val recorder = WavRecorder()
    private val importer = AudioImporter(app)

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing

    @Volatile private var stopRequested = false

    private val _recording = MutableStateFlow(false)
    val recording: StateFlow<Boolean> = _recording

    private val _level = MutableStateFlow(0f)
    val level: StateFlow<Float> = _level

    private val _message = MutableStateFlow<PracticeMessage?>(null)
    val message: StateFlow<PracticeMessage?> = _message

    fun attempts(letterKey: String) = repo.attempts(letterKey)
    fun reviewedAttemptIds() = repo.reviewedAttemptIds()

    /** First tap starts recording; second tap (or the 5 s limit) stops it and sends it to the AI. */
    fun toggleRecording(letterKey: String) {
        if (_recording.value) {
            stopRequested = true
            return
        }
        if (_importing.value) return
        stopRequested = false
        _recording.value = true
        val letter = Letters.byKey(letterKey)
        val file = repo.newRecordingFile(letterKey)
        viewModelScope.launch {
            val durationMs = try {
                recorder.record(file, shouldStop = { stopRequested }) { _level.value = it }
            } catch (e: Exception) {
                _message.value = PracticeMessage.RecordFailed(e.message ?: e.javaClass.simpleName)
                0L
            } finally {
                _recording.value = false
                _level.value = 0f
            }
            if (durationMs < MIN_DURATION_MS) {
                file.delete()
                if (durationMs > 0) _message.value = PracticeMessage.TooShort
                return@launch
            }
            repo.submitRecording(letter, file, durationMs)
        }
    }

    /** Converts an audio file picked from the phone to WAV, then checks it like a fresh recording. */
    fun importAudio(letterKey: String, uri: Uri) {
        if (_importing.value || _recording.value) return
        _importing.value = true
        val file = repo.newRecordingFile(letterKey)
        viewModelScope.launch {
            val durationMs = try {
                importer.import(uri, file)
            } catch (e: Exception) {
                file.delete()
                _message.value = PracticeMessage.ImportFailed
                null
            } finally {
                _importing.value = false
            }
            when {
                durationMs == null -> Unit
                durationMs < MIN_DURATION_MS -> {
                    file.delete()
                    _message.value = PracticeMessage.TooShort
                }
                else -> repo.submitRecording(Letters.byKey(letterKey), file, durationMs)
            }
        }
    }

    fun retry(attempt: Attempt) = repo.verify(attempt.id)
    fun delete(attempt: Attempt) = viewModelScope.launch { repo.deleteAttempt(attempt) }
    fun sendToHujur(attempt: Attempt, note: String) = viewModelScope.launch {
        repo.sendToHujur(attempt, note)
        _message.value = PracticeMessage.Sent
    }

    fun messageShown() {
        _message.value = null
    }

    private companion object {
        const val MIN_DURATION_MS = 300L
    }
}
