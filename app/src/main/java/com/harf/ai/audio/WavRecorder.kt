package com.harf.ai.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

/**
 * Records 16 kHz mono 16-bit PCM straight into a .wav file, which is what the
 * verify API expects. Recording runs until [shouldStop] returns true, the
 * coroutine is cancelled, or [maxDurationMs] is reached.
 */
class WavRecorder(
    private val sampleRate: Int = 16_000,
    private val maxDurationMs: Long = 5_000,
) {
    /** Returns the recorded duration in milliseconds. [onLevel] receives a 0..1 loudness value. */
    @SuppressLint("MissingPermission") // Callers check RECORD_AUDIO before recording.
    suspend fun record(
        output: File,
        shouldStop: () -> Boolean,
        onLevel: (Float) -> Unit = {},
    ): Long = withContext(Dispatchers.IO) {
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBuffer, sampleRate / 10 * 2)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        check(recorder.state == AudioRecord.STATE_INITIALIZED) { "Microphone unavailable" }

        val maxBytes = sampleRate * 2L * maxDurationMs / 1000
        var written = 0L
        RandomAccessFile(output, "rw").use { raf ->
            raf.setLength(0)
            raf.write(ByteArray(WAV_HEADER_SIZE)) // placeholder, patched below
            val buffer = ByteArray(bufferSize)
            recorder.startRecording()
            try {
                while (coroutineContext.isActive && !shouldStop() && written < maxBytes) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read <= 0) continue
                    val toWrite = minOf(read.toLong(), maxBytes - written).toInt()
                    raf.write(buffer, 0, toWrite)
                    written += toWrite
                    onLevel(peakLevel(buffer, toWrite))
                }
            } finally {
                recorder.stop()
                recorder.release()
                raf.seek(0)
                raf.write(wavHeader(written, sampleRate))
            }
        }
        written * 1000 / (sampleRate * 2)
    }

    private fun peakLevel(buf: ByteArray, len: Int): Float {
        var peak = 0
        var i = 0
        while (i + 1 < len) {
            val sample = (buf[i].toInt() and 0xFF) or (buf[i + 1].toInt() shl 8)
            peak = maxOf(peak, abs(sample.toShort().toInt()))
            i += 2
        }
        return peak / 32768f
    }

}

const val WAV_HEADER_SIZE = 44

/** 44-byte header for 16-bit mono PCM, the only WAV shape the verify API accepts. */
fun wavHeader(dataSize: Long, rate: Int): ByteArray {
    val byteRate = rate * 2
    fun le32(v: Long) = byteArrayOf(v.toByte(), (v shr 8).toByte(), (v shr 16).toByte(), (v shr 24).toByte())
    fun le16(v: Int) = byteArrayOf(v.toByte(), (v shr 8).toByte())
    return "RIFF".toByteArray() + le32(36 + dataSize) + "WAVE".toByteArray() +
        "fmt ".toByteArray() + le32(16) + le16(1) + le16(1) + le32(rate.toLong()) +
        le32(byteRate.toLong()) + le16(2) + le16(16) +
        "data".toByteArray() + le32(dataSize)
}
