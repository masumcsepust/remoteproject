package com.harf.ai.audio

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

/**
 * Turns any audio file the phone can play (mp3, m4a, ogg/opus voice notes, wav, …) into
 * the 16 kHz mono 16-bit WAV the verify API requires: decode → mix to mono → resample.
 */
class AudioImporter(
    private val context: Context,
    private val targetRate: Int = 16_000,
    private val maxDurationMs: Long = 10_000,
) {
    class UnsupportedAudioException : Exception("Unsupported audio file")

    /** Returns the duration in milliseconds of the WAV written to [output]. */
    suspend fun import(source: Uri, output: File): Long = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, source, null)
            val track = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: throw UnsupportedAudioException()
            extractor.selectTrack(track)
            val mono = decodeToMono(extractor, extractor.getTrackFormat(track))
            val pcm = resample(mono.samples, mono.rate, targetRate)
                .let { it.copyOf(minOf(it.size, (targetRate * maxDurationMs / 1000).toInt())) }
            writeWav(pcm, output)
            pcm.size * 1000L / targetRate
        } finally {
            extractor.release()
        }
    }

    private class Mono(val samples: FloatArray, val rate: Int)

    private suspend fun decodeToMono(extractor: MediaExtractor, format: MediaFormat): Mono {
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        var rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        var floatPcm = false
        // Decode a little past the cap so resampling has enough input.
        val maxFrames = rate.toLong() * (maxDurationMs + 500) / 1000

        val codec = runCatching { MediaCodec.createDecoderByType(mime) }
            .getOrElse { throw UnsupportedAudioException() }
        val out = FloatArrayBuilder()
        try {
            codec.configure(format, null, null, 0)
            codec.start()
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            while (!outputDone && out.size < maxFrames) {
                coroutineContext.ensureActive()
                if (!inputDone) {
                    val inIndex = codec.dequeueInputBuffer(10_000)
                    if (inIndex >= 0) {
                        val buffer = codec.getInputBuffer(inIndex)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            codec.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outIndex = codec.dequeueOutputBuffer(info, 10_000)
                when {
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val f = codec.outputFormat
                        rate = f.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        channels = f.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        floatPcm = f.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                            f.getInteger(MediaFormat.KEY_PCM_ENCODING) == AudioFormat.ENCODING_PCM_FLOAT
                    }
                    outIndex >= 0 -> {
                        val buffer = codec.getOutputBuffer(outIndex)!!.order(ByteOrder.LITTLE_ENDIAN)
                        buffer.position(info.offset).limit(info.offset + info.size)
                        if (floatPcm) {
                            val fb = buffer.asFloatBuffer()
                            while (fb.remaining() >= channels) {
                                var sum = 0f
                                repeat(channels) { sum += fb.get() }
                                out.add(sum / channels)
                            }
                        } else {
                            val sb = buffer.asShortBuffer()
                            while (sb.remaining() >= channels) {
                                var sum = 0f
                                repeat(channels) { sum += sb.get() / 32768f }
                                out.add(sum / channels)
                            }
                        }
                        codec.releaseOutputBuffer(outIndex, false)
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    }
                }
            }
        } finally {
            runCatching { codec.stop() }
            codec.release()
        }
        if (out.size == 0) throw UnsupportedAudioException()
        return Mono(out.toArray(), rate)
    }

    /** Linear-interpolation resampler; plenty for speech going into a classifier. */
    private fun resample(input: FloatArray, from: Int, to: Int): FloatArray {
        if (from == to) return input
        val outSize = (input.size.toLong() * to / from).toInt()
        val step = from.toDouble() / to
        return FloatArray(outSize) { i ->
            val pos = i * step
            val idx = pos.toInt()
            val frac = (pos - idx).toFloat()
            val a = input[idx]
            val b = if (idx + 1 < input.size) input[idx + 1] else a
            a + (b - a) * frac
        }
    }

    private fun writeWav(samples: FloatArray, output: File) {
        val data = ByteArray(samples.size * 2)
        samples.forEachIndexed { i, f ->
            val v = (f.coerceIn(-1f, 1f) * 32767).toInt()
            data[2 * i] = v.toByte()
            data[2 * i + 1] = (v shr 8).toByte()
        }
        output.outputStream().use {
            it.write(wavHeader(data.size.toLong(), targetRate))
            it.write(data)
        }
    }

    private class FloatArrayBuilder {
        private var data = FloatArray(16_384)
        var size = 0
            private set

        fun add(v: Float) {
            if (size == data.size) data = data.copyOf(size * 2)
            data[size++] = v
        }

        fun toArray(): FloatArray = data.copyOf(size)
    }
}
