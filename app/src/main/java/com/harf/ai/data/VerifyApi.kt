package com.harf.ai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class VerifyResult(
    val predictedLabel: String,
    val confidence: Double,
    val accepted: Boolean,
    val isCorrect: Boolean,
)

/**
 * Client for the letter verification model:
 *
 *   POST {base}/predict  multipart: audio=<wav>, target_letter=<label>
 *   -> {"predicted_label":"Ba","confidence":0.93,"accepted":true,"target_letter":"Ba","is_correct":true}
 */
class VerifyApi(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        // The container can cold-start, so allow a generous read timeout.
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun predict(audio: File, targetLetter: String): VerifyResult = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", audio.name, audio.asRequestBody("audio/wav".toMediaType()))
            .addFormDataPart("target_letter", targetLetter)
            .build()
        val request = Request.Builder().url("$baseUrl/predict").post(body).build()

        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("Server error ${response.code}")
            val json = JSONObject(text)
            VerifyResult(
                predictedLabel = json.optString("predicted_label", "Unrecognized"),
                confidence = json.optDouble("confidence", 0.0),
                accepted = json.optBoolean("accepted", false),
                isCorrect = json.optBoolean("is_correct", false),
            )
        }
    }
}
