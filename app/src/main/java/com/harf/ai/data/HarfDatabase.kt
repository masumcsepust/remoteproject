package com.harf.ai.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Plain SQLite storage for attempts and Hujur review requests. Audio lives in files; rows hold paths. */
class HarfDatabase(context: Context) : SQLiteOpenHelper(context, "harf.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE attempts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                letter_key TEXT NOT NULL,
                audio_path TEXT NOT NULL,
                duration_ms INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                predicted_label TEXT,
                confidence REAL,
                is_correct INTEGER NOT NULL DEFAULT 0,
                error TEXT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_attempts_letter ON attempts(letter_key)")
        db.execSQL(
            """
            CREATE TABLE reviews (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                attempt_id INTEGER NOT NULL REFERENCES attempts(id) ON DELETE CASCADE,
                letter_key TEXT NOT NULL,
                audio_path TEXT NOT NULL,
                student_note TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                verdict TEXT,
                hujur_comment TEXT,
                reviewed_at INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    // ---- attempts ----

    fun insertAttempt(letterKey: String, audioPath: String, durationMs: Long): Long =
        writableDatabase.insertOrThrow("attempts", null, ContentValues().apply {
            put("letter_key", letterKey)
            put("audio_path", audioPath)
            put("duration_ms", durationMs)
            put("created_at", System.currentTimeMillis())
            put("status", AttemptStatus.CHECKING.name)
        })

    fun markAttemptVerified(id: Long, predictedLabel: String, confidence: Double, isCorrect: Boolean) {
        writableDatabase.update("attempts", ContentValues().apply {
            put("status", AttemptStatus.VERIFIED.name)
            put("predicted_label", predictedLabel)
            put("confidence", confidence)
            put("is_correct", if (isCorrect) 1 else 0)
            putNull("error")
        }, "id = ?", arrayOf(id.toString()))
    }

    fun markAttemptFailed(id: Long, error: String) {
        writableDatabase.update("attempts", ContentValues().apply {
            put("status", AttemptStatus.FAILED.name)
            put("error", error)
        }, "id = ?", arrayOf(id.toString()))
    }

    fun markAttemptChecking(id: Long) {
        writableDatabase.update("attempts", ContentValues().apply {
            put("status", AttemptStatus.CHECKING.name)
            putNull("error")
        }, "id = ?", arrayOf(id.toString()))
    }

    fun failStaleChecks(error: String) {
        writableDatabase.update("attempts", ContentValues().apply {
            put("status", AttemptStatus.FAILED.name)
            put("error", error)
        }, "status = ?", arrayOf(AttemptStatus.CHECKING.name))
    }

    fun attempt(id: Long): Attempt? =
        readableDatabase.rawQuery("SELECT * FROM attempts WHERE id = ?", arrayOf(id.toString()))
            .use { c -> if (c.moveToFirst()) c.toAttempt() else null }

    fun attemptsFor(letterKey: String): List<Attempt> =
        readableDatabase.rawQuery(
            "SELECT * FROM attempts WHERE letter_key = ? ORDER BY created_at DESC",
            arrayOf(letterKey)
        ).use { c -> buildList { while (c.moveToNext()) add(c.toAttempt()) } }

    fun deleteAttempt(id: Long) {
        writableDatabase.delete("attempts", "id = ?", arrayOf(id.toString()))
    }

    /** letter_key -> (attempts, correct) */
    fun attemptStats(): Map<String, Pair<Int, Int>> =
        readableDatabase.rawQuery(
            "SELECT letter_key, COUNT(*), SUM(is_correct) FROM attempts GROUP BY letter_key", null
        ).use { c ->
            buildMap { while (c.moveToNext()) put(c.getString(0), c.getInt(1) to c.getInt(2)) }
        }

    // ---- reviews ----

    fun insertReview(attempt: Attempt, note: String): Long =
        writableDatabase.insertOrThrow("reviews", null, ContentValues().apply {
            put("attempt_id", attempt.id)
            put("letter_key", attempt.letterKey)
            put("audio_path", attempt.audioPath)
            put("student_note", note)
            put("created_at", System.currentTimeMillis())
            put("status", ReviewStatus.PENDING.name)
        })

    fun submitHujurFeedback(reviewId: Long, verdict: HujurVerdict, comment: String) {
        writableDatabase.update("reviews", ContentValues().apply {
            put("status", ReviewStatus.REVIEWED.name)
            put("verdict", verdict.name)
            put("hujur_comment", comment)
            put("reviewed_at", System.currentTimeMillis())
        }, "id = ?", arrayOf(reviewId.toString()))
    }

    fun reviews(status: ReviewStatus? = null): List<ReviewRequest> {
        val (where, args) = if (status == null) "" to null else "WHERE status = ?" to arrayOf(status.name)
        return readableDatabase.rawQuery("SELECT * FROM reviews $where ORDER BY created_at DESC", args)
            .use { c -> buildList { while (c.moveToNext()) add(c.toReview()) } }
    }

    fun approvedLetterKeys(): Set<String> =
        readableDatabase.rawQuery(
            "SELECT DISTINCT letter_key FROM reviews WHERE verdict = ?", arrayOf(HujurVerdict.CORRECT.name)
        ).use { c -> buildSet { while (c.moveToNext()) add(c.getString(0)) } }

    fun reviewedAttemptIds(): Set<Long> =
        readableDatabase.rawQuery("SELECT attempt_id FROM reviews", null)
            .use { c -> buildSet { while (c.moveToNext()) add(c.getLong(0)) } }

    private fun Cursor.str(col: String): String? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.long(col: String): Long? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getLong(it) }
    private fun Cursor.dbl(col: String): Double? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getDouble(it) }

    private fun Cursor.toAttempt() = Attempt(
        id = long("id")!!,
        letterKey = str("letter_key")!!,
        audioPath = str("audio_path")!!,
        durationMs = long("duration_ms")!!,
        createdAt = long("created_at")!!,
        status = AttemptStatus.valueOf(str("status")!!),
        predictedLabel = str("predicted_label"),
        confidence = dbl("confidence"),
        isCorrect = long("is_correct") == 1L,
        error = str("error"),
    )

    private fun Cursor.toReview() = ReviewRequest(
        id = long("id")!!,
        attemptId = long("attempt_id")!!,
        letterKey = str("letter_key")!!,
        audioPath = str("audio_path")!!,
        studentNote = str("student_note").orEmpty(),
        createdAt = long("created_at")!!,
        status = ReviewStatus.valueOf(str("status")!!),
        verdict = str("verdict")?.let(HujurVerdict::valueOf),
        hujurComment = str("hujur_comment"),
        reviewedAt = long("reviewed_at"),
    )
}
