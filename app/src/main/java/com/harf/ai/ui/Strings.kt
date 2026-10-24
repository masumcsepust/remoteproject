package com.harf.ai.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.harf.ai.data.ArabicLetter
import com.harf.ai.data.Language
import java.util.Locale

/**
 * All user-facing text, in English and Bangla. Kept in code (not resources) so the
 * language switches instantly in-app without restarting the activity.
 */
interface Strings {
    val language: Language
    val locale: Locale

    fun num(n: Int): String = n.toString()
    fun letterName(letter: ArabicLetter): String = letter.name
    fun makhraj(letter: ArabicLetter): String = letter.makhraj

    // Navigation
    val tabLearn: String
    val tabReviews: String
    val tabHujur: String

    // Language picker
    val chooseLanguageTitle: String
    val chooseLanguageBody: String
    val languageLabel: String

    // Home
    val greeting: String
    val homeSubtitle: String
    val yourProgress: String
    fun learnedOf(learned: Int, total: Int): String
    val statPracticing: String
    val statLearned: String
    val statApproved: String
    val upNext: String
    val startPractice: String
    val allLearnedTitle: String
    val allLearnedBody: String
    val filterAll: String
    val filterToPractice: String
    val filterLearned: String
    fun lesson(n: Int): String
    val noLettersInFilter: String
    val stageNew: String
    val stageLearned: String
    val stageApproved: String

    // Practice
    val back: String
    val makhrajLabel: String
    fun correctToLearn(correct: Int, total: Int): String
    val recordHint: String
    val listeningHint: String
    val startRecording: String
    val stopRecording: String
    val uploadAudio: String
    val uploadHint: String
    val importing: String
    val msgImportFailed: String
    fun learnedTitle(glyph: String): String
    val learnedBody: String
    val sendBest: String
    val yourRecordings: String
    val checking: String
    fun couldNotCheck(error: String): String
    fun correctResult(pct: Int): String
    fun heardResult(glyph: String, name: String, pct: Int): String
    val unrecognized: String
    val askHujurConfirm: String
    val notSureAskHujur: String
    val sentToHujur: String
    val retry: String
    val share: String
    val delete: String
    val play: String
    val stop: String
    fun sendDialogTitle(glyph: String): String
    val sendDialogBody: String
    val noteLabel: String
    val send: String
    val cancel: String
    val msgTooShort: String
    fun msgRecordFailed(error: String): String
    val msgSent: String
    val errNetwork: String
    val errTimeout: String
    val errInterrupted: String
    fun shareText(glyph: String, name: String): String
    val shareChooser: String
    fun seconds(value: Float): String

    // My reviews
    val reviewsTitle: String
    val reviewsEmptyTitle: String
    val reviewsEmptyBody: String
    fun sentAt(time: String): String
    fun youSaid(note: String): String
    val waitingForHujur: String
    val hujurCorrect: String
    val hujurNeedsWork: String
    fun hujurSays(comment: String): String

    // Hujur
    val hujurMode: String
    val hujurModeBody: String
    val pin: String
    val wrongPin: String
    val unlock: String
    val inboxTitle: String
    fun waitingCount(n: Int): String
    fun studentSaid(note: String): String
    val correctionLabel: String
    val markCorrect: String
    val markNeedsWork: String
    val reviewed: String
    val inboxEmpty: String
}

object EnStrings : Strings {
    override val language = Language.EN
    override val locale: Locale = Locale.ENGLISH

    override val tabLearn = "Learn"
    override val tabReviews = "My reviews"
    override val tabHujur = "Hujur"

    override val chooseLanguageTitle = "Choose your language"
    override val chooseLanguageBody = "You can change this any time from the Learn screen."
    override val languageLabel = "Language"

    override val greeting = "Assalamu Alaikum"
    override val homeSubtitle = "Learn the Arabic letters with AI, then get a Hujur's approval."
    override val yourProgress = "Your progress"
    override fun learnedOf(learned: Int, total: Int) = "$learned of $total letters learned"
    override val statPracticing = "Practicing"
    override val statLearned = "Learned"
    override val statApproved = "Hujur ✓"
    override val upNext = "Up next"
    override val startPractice = "Start practice"
    override val allLearnedTitle = "All letters learned, MashaAllah!"
    override val allLearnedBody = "Send your recordings to your Hujur for final approval."
    override val filterAll = "All"
    override val filterToPractice = "To practice"
    override val filterLearned = "Learned"
    override fun lesson(n: Int) = "Lesson $n"
    override val noLettersInFilter = "No letters here yet."
    override val stageNew = "New"
    override val stageLearned = "Learned"
    override val stageApproved = "Approved"

    override val back = "Back"
    override val makhrajLabel = "Makhraj"
    override fun correctToLearn(correct: Int, total: Int) = "$correct / $total correct to learn"
    override val recordHint = "Tap the mic and recite the letter"
    override val listeningHint = "Listening… tap to stop (max 5 s)"
    override val startRecording = "Start recording"
    override val stopRecording = "Stop recording"
    override val uploadAudio = "Upload audio"
    override val uploadHint = "or choose a recording saved on your phone"
    override val importing = "Preparing audio…"
    override val msgImportFailed = "Couldn't read that audio file. Try another one."
    override fun learnedTitle(glyph: String) = "MashaAllah! You've learned $glyph"
    override val learnedBody = "Send your best recording to your Hujur for a final human check."
    override val sendBest = "Send best recording to Hujur"
    override val yourRecordings = "Your recordings"
    override val checking = "AI is listening…"
    override fun couldNotCheck(error: String) = "Couldn't check: $error"
    override fun correctResult(pct: Int) = "Correct ✓  ($pct%)"
    override fun heardResult(glyph: String, name: String, pct: Int) = "Try again — AI heard $glyph ($name) ($pct%)"
    override val unrecognized = "Try again — AI couldn't recognise a letter"
    override val askHujurConfirm = "Ask Hujur to confirm"
    override val notSureAskHujur = "Not sure? Ask Hujur"
    override val sentToHujur = "Sent to Hujur"
    override val retry = "Retry AI check"
    override val share = "Share with Hujur"
    override val delete = "Delete"
    override val play = "Play"
    override val stop = "Stop"
    override fun sendDialogTitle(glyph: String) = "Send $glyph to Hujur"
    override val sendDialogBody = "Your Hujur will listen and reply with corrections."
    override val noteLabel = "Note for Hujur (optional)"
    override val send = "Send"
    override val cancel = "Cancel"
    override val msgTooShort = "Too short — hold the sound a little longer."
    override fun msgRecordFailed(error: String) = "Recording failed: $error"
    override val msgSent = "Sent to Hujur. You'll see the feedback under My reviews."
    override val errNetwork = "No internet connection"
    override val errTimeout = "Server took too long — try again"
    override val errInterrupted = "Interrupted — tap retry"
    override fun shareText(glyph: String, name: String) =
        "Assalamu alaikum Hujur, please check my pronunciation of $glyph ($name)."
    override val shareChooser = "Send to Hujur"
    override fun seconds(value: Float) = "%.1f s".format(locale, value)

    override val reviewsTitle = "My Hujur reviews"
    override val reviewsEmptyTitle = "No reviews yet"
    override val reviewsEmptyBody =
        "Once you've learned a letter (or have a doubt), send a recording to your Hujur from the letter's page."
    override fun sentAt(time: String) = "Sent $time"
    override fun youSaid(note: String) = "You: $note"
    override val waitingForHujur = "Waiting for Hujur"
    override val hujurCorrect = "Hujur: Correct ✓"
    override val hujurNeedsWork = "Hujur: Needs work"
    override fun hujurSays(comment: String) = "Hujur: $comment"

    override val hujurMode = "Hujur mode"
    override val hujurModeBody = "For the teacher: enter the PIN to review students' recordings."
    override val pin = "PIN"
    override val wrongPin = "Wrong PIN"
    override val unlock = "Unlock"
    override val inboxTitle = "Hujur inbox"
    override fun waitingCount(n: Int) = "$n waiting for review"
    override fun studentSaid(note: String) = "Student: $note"
    override val correctionLabel = "Correction / advice"
    override val markCorrect = "Correct"
    override val markNeedsWork = "Needs work"
    override val reviewed = "Reviewed"
    override val inboxEmpty = "No recordings waiting. Alhamdulillah!"
}

object BnStrings : Strings {
    override val language = Language.BN
    override val locale: Locale = Locale.forLanguageTag("bn-BD")

    private val digits = "০১২৩৪৫৬৭৮৯"
    private fun bn(s: String) = s.map { if (it in '0'..'9') digits[it - '0'] else it }.joinToString("")
    override fun num(n: Int) = bn(n.toString())
    override fun letterName(letter: ArabicLetter) = letter.nameBn
    override fun makhraj(letter: ArabicLetter) = letter.makhrajBn

    override val tabLearn = "শিখুন"
    override val tabReviews = "আমার রিভিউ"
    override val tabHujur = "হুজুর"

    override val chooseLanguageTitle = "আপনার ভাষা বেছে নিন"
    override val chooseLanguageBody = "শিখুন পাতা থেকে যেকোনো সময় এটি বদলাতে পারবেন।"
    override val languageLabel = "ভাষা"

    override val greeting = "আসসালামু আলাইকুম"
    override val homeSubtitle = "AI-এর সাহায্যে আরবি হরফ শিখুন, তারপর হুজুরের অনুমোদন নিন।"
    override val yourProgress = "আপনার অগ্রগতি"
    override fun learnedOf(learned: Int, total: Int) = "${num(total)}টি হরফের মধ্যে ${num(learned)}টি শেখা হয়েছে"
    override val statPracticing = "অনুশীলন চলছে"
    override val statLearned = "শেখা হয়েছে"
    override val statApproved = "হুজুর ✓"
    override val upNext = "এরপর"
    override val startPractice = "অনুশীলন শুরু"
    override val allLearnedTitle = "মাশাআল্লাহ! সব হরফ শেখা হয়েছে"
    override val allLearnedBody = "চূড়ান্ত অনুমোদনের জন্য রেকর্ডিং হুজুরের কাছে পাঠান।"
    override val filterAll = "সব"
    override val filterToPractice = "অনুশীলন বাকি"
    override val filterLearned = "শেখা হয়েছে"
    override fun lesson(n: Int) = "পাঠ ${num(n)}"
    override val noLettersInFilter = "এখানে এখনো কোনো হরফ নেই।"
    override val stageNew = "নতুন"
    override val stageLearned = "শেখা হয়েছে"
    override val stageApproved = "অনুমোদিত"

    override val back = "ফিরে যান"
    override val makhrajLabel = "মাখরাজ"
    override fun correctToLearn(correct: Int, total: Int) = "শিখতে ${num(total)}টির মধ্যে ${num(correct)}টি সঠিক"
    override val recordHint = "মাইকে চাপ দিয়ে হরফটি পড়ুন"
    override val listeningHint = "শুনছি… থামাতে চাপ দিন (সর্বোচ্চ ৫ সেকেন্ড)"
    override val startRecording = "রেকর্ড শুরু"
    override val stopRecording = "রেকর্ড বন্ধ"
    override val uploadAudio = "অডিও আপলোড"
    override val uploadHint = "অথবা ফোনে সেভ করা রেকর্ডিং বেছে নিন"
    override val importing = "অডিও প্রস্তুত হচ্ছে…"
    override val msgImportFailed = "এই অডিও ফাইলটি পড়া যায়নি। অন্য একটি চেষ্টা করুন।"
    override fun learnedTitle(glyph: String) = "মাশাআল্লাহ! আপনি $glyph শিখে ফেলেছেন"
    override val learnedBody = "চূড়ান্ত যাচাইয়ের জন্য আপনার সেরা রেকর্ডিং হুজুরের কাছে পাঠান।"
    override val sendBest = "সেরা রেকর্ডিং হুজুরকে পাঠান"
    override val yourRecordings = "আপনার রেকর্ডিং"
    override val checking = "AI শুনছে…"
    override fun couldNotCheck(error: String) = "যাচাই করা যায়নি: $error"
    override fun correctResult(pct: Int) = "সঠিক ✓  (${num(pct)}%)"
    override fun heardResult(glyph: String, name: String, pct: Int) =
        "আবার চেষ্টা করুন — AI শুনেছে $glyph ($name) (${num(pct)}%)"
    override val unrecognized = "আবার চেষ্টা করুন — AI কোনো হরফ চিনতে পারেনি"
    override val askHujurConfirm = "হুজুরকে দিয়ে নিশ্চিত করুন"
    override val notSureAskHujur = "সন্দেহ আছে? হুজুরকে জিজ্ঞেস করুন"
    override val sentToHujur = "হুজুরকে পাঠানো হয়েছে"
    override val retry = "আবার যাচাই করুন"
    override val share = "হুজুরের সাথে শেয়ার করুন"
    override val delete = "মুছুন"
    override val play = "চালান"
    override val stop = "থামান"
    override fun sendDialogTitle(glyph: String) = "$glyph হুজুরকে পাঠান"
    override val sendDialogBody = "হুজুর শুনে সংশোধনসহ উত্তর দেবেন।"
    override val noteLabel = "হুজুরের জন্য নোট (ঐচ্ছিক)"
    override val send = "পাঠান"
    override val cancel = "বাতিল"
    override val msgTooShort = "খুব ছোট — আওয়াজটা একটু লম্বা করুন।"
    override fun msgRecordFailed(error: String) = "রেকর্ড করা যায়নি: $error"
    override val msgSent = "হুজুরকে পাঠানো হয়েছে। মতামত 'আমার রিভিউ'-তে দেখতে পাবেন।"
    override val errNetwork = "ইন্টারনেট সংযোগ নেই"
    override val errTimeout = "সার্ভার সাড়া দিতে দেরি করছে — আবার চেষ্টা করুন"
    override val errInterrupted = "বাধাগ্রস্ত হয়েছে — আবার যাচাই করুন"
    override fun shareText(glyph: String, name: String) =
        "আসসালামু আলাইকুম হুজুর, আমার $glyph ($name) উচ্চারণটি একটু দেখে দিন।"
    override val shareChooser = "হুজুরকে পাঠান"
    override fun seconds(value: Float) = bn("%.1f".format(Locale.ENGLISH, value)) + " সে."

    override val reviewsTitle = "হুজুরের রিভিউ"
    override val reviewsEmptyTitle = "এখনো কোনো রিভিউ নেই"
    override val reviewsEmptyBody =
        "কোনো হরফ শেখা হলে (বা সন্দেহ থাকলে) হরফের পাতা থেকে রেকর্ডিং হুজুরকে পাঠান।"
    override fun sentAt(time: String) = "পাঠানো: $time"
    override fun youSaid(note: String) = "আপনি: $note"
    override val waitingForHujur = "হুজুরের অপেক্ষায়"
    override val hujurCorrect = "হুজুর: সঠিক ✓"
    override val hujurNeedsWork = "হুজুর: আরও অনুশীলন দরকার"
    override fun hujurSays(comment: String) = "হুজুর: $comment"

    override val hujurMode = "হুজুর মোড"
    override val hujurModeBody = "শিক্ষকের জন্য: ছাত্রদের রেকর্ডিং দেখতে পিন দিন।"
    override val pin = "পিন"
    override val wrongPin = "ভুল পিন"
    override val unlock = "খুলুন"
    override val inboxTitle = "হুজুরের ইনবক্স"
    override fun waitingCount(n: Int) = "${num(n)}টি রিভিউয়ের অপেক্ষায়"
    override fun studentSaid(note: String) = "ছাত্র: $note"
    override val correctionLabel = "সংশোধন / পরামর্শ"
    override val markCorrect = "সঠিক"
    override val markNeedsWork = "আরও অনুশীলন"
    override val reviewed = "রিভিউ করা হয়েছে"
    override val inboxEmpty = "কোনো রেকর্ডিং অপেক্ষায় নেই। আলহামদুলিল্লাহ!"
}

fun stringsFor(language: Language): Strings = when (language) {
    Language.EN -> EnStrings
    Language.BN -> BnStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { EnStrings }
