package com.harf.ai.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Single entry point for the UI. Reads are exposed as Flows that re-query whenever
 * something is written, so screens stay in sync without a reactive DB library.
 */
class HarfRepository(
    private val db: HarfDatabase,
    private val api: VerifyApi,
    private val recordingsDir: File,
) {
    private val changes = MutableStateFlow(0L)

    // Verification outlives the screen that started it, so leaving the screen never strands an attempt.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Anything still "checking" belongs to a previous process that was killed mid-request.
        appScope.launch {
            db.failStaleChecks(ERR_INTERRUPTED)
            notifyChanged()
        }
    }

    private fun notifyChanged() {
        changes.value++
    }

    private fun <T> observe(query: () -> T): Flow<T> =
        changes.map { query() }.flowOn(Dispatchers.IO)

    fun newRecordingFile(letterKey: String): File {
        recordingsDir.mkdirs()
        return File(recordingsDir, "${letterKey}_${System.currentTimeMillis()}.wav")
    }

    fun progress(): Flow<List<LetterProgress>> = observe {
        val stats = db.attemptStats()
        val approved = db.approvedLetterKeys()
        Letters.all.map { letter ->
            val (attempts, correct) = stats[letter.key] ?: (0 to 0)
            val stage = when {
                letter.key in approved -> LetterStage.HUJUR_APPROVED
                correct >= Letters.CORRECT_TO_LEARN -> LetterStage.AI_LEARNED
                attempts > 0 -> LetterStage.PRACTICING
                else -> LetterStage.NEW
            }
            LetterProgress(letter, attempts, correct, stage)
        }
    }

    fun attempts(letterKey: String): Flow<List<Attempt>> = observe { db.attemptsFor(letterKey) }

    fun reviewedAttemptIds(): Flow<Set<Long>> = observe { db.reviewedAttemptIds() }

    fun reviews(status: ReviewStatus? = null): Flow<List<ReviewRequest>> = observe { db.reviews(status) }

    /** Saves the recording as a new attempt and asks the AI to verify it in the background. */
    suspend fun submitRecording(letter: ArabicLetter, file: File, durationMs: Long): Long {
        val id = withContext(Dispatchers.IO) { db.insertAttempt(letter.key, file.absolutePath, durationMs) }
        notifyChanged()
        verify(id)
        return id
    }

    /** (Re)runs AI verification for an attempt; failures are stored so the user can retry. */
    fun verify(attemptId: Long) {
        appScope.launch {
            val attempt = db.attempt(attemptId) ?: return@launch
            db.markAttemptChecking(attemptId)
            notifyChanged()
            try {
                val result = api.predict(File(attempt.audioPath), Letters.byKey(attempt.letterKey).apiLabel)
                db.markAttemptVerified(attemptId, result.predictedLabel, result.confidence, result.isCorrect)
            } catch (e: Exception) {
                db.markAttemptFailed(attemptId, friendlyError(e))
            }
            notifyChanged()
        }
    }

    /** Known failures are stored as codes so the UI can show them in the user's language. */
    private fun friendlyError(e: Exception): String = when (e) {
        is java.net.UnknownHostException, is java.net.ConnectException -> ERR_NETWORK
        is java.net.SocketTimeoutException -> ERR_TIMEOUT
        else -> e.message ?: e.javaClass.simpleName
    }

    suspend fun deleteAttempt(attempt: Attempt) = withContext(Dispatchers.IO) {
        db.deleteAttempt(attempt.id)
        File(attempt.audioPath).delete()
        notifyChanged()
    }

    suspend fun sendToHujur(attempt: Attempt, note: String) = withContext(Dispatchers.IO) {
        db.insertReview(attempt, note.trim())
        notifyChanged()
    }

    suspend fun submitHujurFeedback(reviewId: Long, verdict: HujurVerdict, comment: String) =
        withContext(Dispatchers.IO) {
            db.submitHujurFeedback(reviewId, verdict, comment.trim())
            notifyChanged()
        }

    companion object {
        const val ERR_NETWORK = "ERR_NETWORK"
        const val ERR_TIMEOUT = "ERR_TIMEOUT"
        const val ERR_INTERRUPTED = "ERR_INTERRUPTED"
    }
}
