package com.harf.ai.data

enum class AttemptStatus { CHECKING, VERIFIED, FAILED }

/** One recording of a letter and what the AI said about it. */
data class Attempt(
    val id: Long,
    val letterKey: String,
    val audioPath: String,
    val durationMs: Long,
    val createdAt: Long,
    val status: AttemptStatus,
    val predictedLabel: String?,
    val confidence: Double?,
    val isCorrect: Boolean,
    val error: String?,
)

enum class ReviewStatus { PENDING, REVIEWED }

enum class HujurVerdict { CORRECT, NEEDS_WORK }

/** A recording the student sent to a human teacher (Hujur) for correction. */
data class ReviewRequest(
    val id: Long,
    val attemptId: Long,
    val letterKey: String,
    val audioPath: String,
    val studentNote: String,
    val createdAt: Long,
    val status: ReviewStatus,
    val verdict: HujurVerdict?,
    val hujurComment: String?,
    val reviewedAt: Long?,
)

enum class LetterStage { NEW, PRACTICING, AI_LEARNED, HUJUR_APPROVED }

data class LetterProgress(
    val letter: ArabicLetter,
    val attempts: Int,
    val correct: Int,
    val stage: LetterStage,
)
