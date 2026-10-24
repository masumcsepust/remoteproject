package com.harf.ai.data

/**
 * One letter of the Arabic alphabet.
 *
 * [apiLabel] is the value sent as `target_letter` to the verify API. Only "Ba" has been
 * confirmed against the server so far; adjust the others here if the model uses
 * different spellings.
 */
data class ArabicLetter(
    val key: String,
    val glyph: String,
    val name: String,
    val nameBn: String,
    val apiLabel: String,
    val makhraj: String,
    val makhrajBn: String,
)

/** A small group of letters taught together, ordered by similar shape. */
data class Lesson(val number: Int, val letters: List<ArabicLetter>)

object Letters {
    val all: List<ArabicLetter> = listOf(
        ArabicLetter(
            "alif", "ا", "Alif", "আলিফ", "Alif",
            "Empty space of the mouth and throat (al-jawf); a long open 'aa'.",
            "মুখ ও গলার খালি জায়গা (জাওফ) থেকে; লম্বা খোলা 'আ'।",
        ),
        ArabicLetter(
            "ba", "ب", "Ba", "বা", "Ba",
            "Both lips press together, then release.",
            "দুই ঠোঁট একসাথে চেপে তারপর ছেড়ে দিন।",
        ),
        ArabicLetter(
            "ta", "ت", "Ta", "তা", "Ta",
            "Tip of the tongue against the roots of the upper front teeth.",
            "জিহ্বার আগা উপরের সামনের দাঁতের গোড়ায় লাগিয়ে।",
        ),
        ArabicLetter(
            "tha", "ث", "Tha", "ছা", "Tha",
            "Tip of the tongue lightly touching the edges of the upper front teeth, like 'th' in 'think'.",
            "জিহ্বার আগা উপরের সামনের দাঁতের কিনারায় হালকা লাগিয়ে, ইংরেজি 'think'-এর 'th'-এর মতো।",
        ),
        ArabicLetter(
            "jeem", "ج", "Jeem", "জীম", "Jeem",
            "Middle of the tongue against the roof of the mouth.",
            "জিহ্বার মাঝখান উপরের তালুর সাথে লাগিয়ে।",
        ),
        ArabicLetter(
            "hha", "ح", "Ḥa", "হা", "Hha",
            "Middle of the throat; a breathy, raspless 'h'.",
            "গলার মাঝখান থেকে; খসখসে ভাব ছাড়া নরম 'হ'।",
        ),
        ArabicLetter(
            "kha", "خ", "Kha", "খা", "Kha",
            "Top of the throat, near the mouth; a light rasp.",
            "গলার উপরের অংশ (মুখের কাছে) থেকে; হালকা খসখসে আওয়াজ।",
        ),
        ArabicLetter(
            "dal", "د", "Dal", "দাল", "Dal",
            "Tip of the tongue against the roots of the upper front teeth, voiced.",
            "জিহ্বার আগা উপরের সামনের দাঁতের গোড়ায় লাগিয়ে, কণ্ঠস্বরসহ।",
        ),
        ArabicLetter(
            "dhal", "ذ", "Dhal", "যাল", "Dhal",
            "Tip of the tongue against the edges of the upper front teeth, like 'th' in 'this'.",
            "জিহ্বার আগা উপরের সামনের দাঁতের কিনারায়, ইংরেজি 'this'-এর 'th'-এর মতো।",
        ),
        ArabicLetter(
            "ra", "ر", "Ra", "রা", "Ra",
            "Tip of the tongue near the gum ridge, with a light roll.",
            "জিহ্বার আগা মাড়ির কাছে, হালকা কম্পনসহ।",
        ),
        ArabicLetter(
            "zay", "ز", "Zay", "যা", "Zay",
            "Tip of the tongue just behind the lower front teeth, a buzzing 'z'.",
            "জিহ্বার আগা নিচের সামনের দাঁতের ঠিক পেছনে, গুনগুনে 'য'।",
        ),
        ArabicLetter(
            "seen", "س", "Seen", "সীন", "Seen",
            "Tip of the tongue just behind the lower front teeth, a whistling 's'.",
            "জিহ্বার আগা নিচের সামনের দাঁতের ঠিক পেছনে, শিসের মতো 'স'।",
        ),
        ArabicLetter(
            "sheen", "ش", "Sheen", "শীন", "Sheen",
            "Middle of the tongue toward the roof of the mouth, spread air: 'sh'.",
            "জিহ্বার মাঝখান তালুর দিকে, বাতাস ছড়িয়ে 'শ'।",
        ),
        ArabicLetter(
            "sad", "ص", "Ṣad", "সোয়াদ", "Sad",
            "Like Seen, but with the back of the tongue raised (heavy).",
            "সীনের মতো, তবে জিহ্বার পেছনের অংশ উঁচু করে (মোটা আওয়াজ)।",
        ),
        ArabicLetter(
            "dad", "ض", "Ḍad", "দোয়াদ", "Dad",
            "Side edge of the tongue against the upper molars, heavy.",
            "জিহ্বার পাশের কিনারা উপরের মাড়ির দাঁতের সাথে লাগিয়ে, মোটা আওয়াজ।",
        ),
        ArabicLetter(
            "tta", "ط", "Ṭa", "ত্বোয়া", "Tta",
            "Like Ta, but with the back of the tongue raised (heavy).",
            "তা-এর মতো, তবে জিহ্বার পেছনের অংশ উঁচু করে (মোটা আওয়াজ)।",
        ),
        ArabicLetter(
            "dha", "ظ", "Ẓa", "যোয়া", "Dha",
            "Like Dhal, but with the back of the tongue raised (heavy).",
            "যালের মতো, তবে জিহ্বার পেছনের অংশ উঁচু করে (মোটা আওয়াজ)।",
        ),
        ArabicLetter(
            "ain", "ع", "ʿAin", "আইন", "Ain",
            "Middle of the throat, voiced and squeezed.",
            "গলার মাঝখান থেকে, চাপা কণ্ঠস্বরে।",
        ),
        ArabicLetter(
            "ghain", "غ", "Ghain", "গাইন", "Ghain",
            "Top of the throat, voiced gargle.",
            "গলার উপরের অংশ থেকে, গড়গড়ার মতো কণ্ঠস্বরে।",
        ),
        ArabicLetter(
            "fa", "ف", "Fa", "ফা", "Fa",
            "Inside of the lower lip touching the edges of the upper front teeth.",
            "নিচের ঠোঁটের ভেতরের অংশ উপরের সামনের দাঁতের কিনারায় লাগিয়ে।",
        ),
        ArabicLetter(
            "qaf", "ق", "Qaf", "ক্বফ", "Qaf",
            "Deepest part of the tongue against the soft palate.",
            "জিহ্বার একদম গোড়া নরম তালুর সাথে লাগিয়ে।",
        ),
        ArabicLetter(
            "kaf", "ك", "Kaf", "কাফ", "Kaf",
            "Back of the tongue against the palate, slightly forward of Qaf.",
            "জিহ্বার গোড়া তালুর সাথে লাগিয়ে, ক্বফের একটু সামনে।",
        ),
        ArabicLetter(
            "lam", "ل", "Lam", "লাম", "Lam",
            "Edges of the tongue tip against the gum ridge.",
            "জিহ্বার আগার কিনারা উপরের মাড়ির সাথে লাগিয়ে।",
        ),
        ArabicLetter(
            "meem", "م", "Meem", "মীম", "Meem",
            "Both lips together, sound through the nose.",
            "দুই ঠোঁট একসাথে, আওয়াজ নাক দিয়ে (গুন্নাহ)।",
        ),
        ArabicLetter(
            "noon", "ن", "Noon", "নূন", "Noon",
            "Tip of the tongue against the gum ridge, sound through the nose.",
            "জিহ্বার আগা মাড়িতে লাগিয়ে, আওয়াজ নাক দিয়ে (গুন্নাহ)।",
        ),
        ArabicLetter(
            "ha", "ه", "Ha", "হা", "Ha",
            "Deepest part of the throat; a soft 'h'.",
            "গলার একদম গভীর থেকে; নরম 'হ'।",
        ),
        ArabicLetter(
            "waw", "و", "Waw", "ওয়াও", "Waw",
            "Rounded lips, not fully closed.",
            "ঠোঁট গোল করে, পুরোপুরি বন্ধ না করে।",
        ),
        ArabicLetter(
            "ya", "ي", "Ya", "ইয়া", "Ya",
            "Middle of the tongue toward the roof of the mouth.",
            "জিহ্বার মাঝখান উপরের তালুর দিকে।",
        ),
    )

    private val byKey = all.associateBy { it.key }

    fun byKey(key: String): ArabicLetter = byKey.getValue(key)

    fun byApiLabel(label: String?): ArabicLetter? =
        all.firstOrNull { it.apiLabel.equals(label, ignoreCase = true) }

    /** Seven lessons of 4–5 letters each, in alphabet order. */
    val lessons: List<Lesson> = listOf(4, 3, 4, 4, 4, 4, 5)
        .runningFold(0) { start, size -> start + size }
        .zipWithNext { from, to -> all.subList(from, to) }
        .mapIndexed { i, letters -> Lesson(i + 1, letters) }

    /** Correct AI verifications needed before a letter counts as learned. */
    const val CORRECT_TO_LEARN = 3
}
