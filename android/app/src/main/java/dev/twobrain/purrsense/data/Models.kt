package dev.twobrain.purrsense.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where a step of the pipeline actually ran. Shown to the user as a badge. */
enum class Brain { ON_DEVICE, CLOUD, RULES }

/** One pipeline step result, for the "TwoBrain trace" shown under every reading. */
data class TraceStep(val step: String, val brain: Brain, val millis: Long)

/**
 * A registered cat. [embedding] is an image embedding of the enrolment photo, used to recognise
 * the cat; it is empty for cats registered before photo matching existed, which fall back to
 * matching the text [appearance].
 */
data class Cat(
    val id: String = "",
    val name: String = "",
    val appearance: String = "",
    val embedding: List<Float> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Feline Grimace Scale (Evangelista et al., 2019): five action units scored 0-2.
 * Total >= 4/10 is the published threshold suggesting pain / re-assessment.
 */
@Serializable
data class Grimace(
    val ears: Int = 0,
    val eyes: Int = 0,
    val muzzle: Int = 0,
    val whiskers: Int = 0,
    val head: Int = 0,
) {
    val total: Int get() = listOf(ears, eyes, muzzle, whiskers, head).sumOf { it.coerceIn(0, 2) }

    /**
     * Pain score used for the alert: the raw total plus a correction for Gemini under-scoring.
     * A Sci Rep 2025 study found chatbots score FGS pain too low (Gemini 1.5 Pro about 2.4/10
     * low), so a cat truly at 5 would often miss the >= 4 alert. The offset comes from Remote
     * Config (`gemini_pain_offset`) and is an interim fix until a landmark-based scorer replaces
     * Gemini (see docs/RESEARCH_HANDOFF.md).
     */
    fun painScore(offset: Int): Int = (total + offset).coerceIn(0, 10)
}

/**
 * Mood is positive / neutral / negative: the research found no scientific basis for finer cat
 * mood labels. Pain is read from the grimace score, not from mood. [normalize] also maps the
 * older 8 labels, so earlier readings and off-format model answers still display sensibly.
 */
object Mood {
    const val POSITIVE = "positive"
    const val NEUTRAL = "neutral"
    const val NEGATIVE = "negative"

    fun normalize(raw: String): String = when (raw.trim().lowercase()) {
        POSITIVE, "relaxed", "playful", "content", "happy" -> POSITIVE
        NEUTRAL, "alert", "sleepy", "calm" -> NEUTRAL
        NEGATIVE, "anxious", "fearful", "irritated", "uncomfortable", "stressed" -> NEGATIVE
        else -> "unknown"
    }
}

/** Output of the "See" step (quick read). Parsed from model JSON. */
@Serializable
data class QuickRead(
    val cat: Boolean = false,
    @SerialName("face_visible") val faceVisible: Boolean = false,
    val appearance: String = "",
    val mood: String = "unknown",
    @SerialName("mood_confidence") val moodConfidence: Double = 0.0,
    val fgs: Grimace = Grimace(),
    val signs: List<String> = emptyList(),
)

/** Output of the "Who" step. */
@Serializable
data class IdMatch(
    @SerialName("cat_id") val catId: String = "unknown",
    val confidence: Double = 0.0,
)

/** Output of the cloud "Deep Check". */
@Serializable
data class DeepCheck(
    val urgency: String = "monitor", // none | monitor | vet_soon | vet_urgent
    val fgs: Grimace = Grimace(),
    val findings: List<String> = emptyList(),
    val summary: String = "",
)

/** What gets stored in Firestore. The photo itself is never uploaded or stored in the cloud. */
data class Reading(
    val id: String = "",
    val catId: String = "unknown",
    val mood: String = "unknown",
    val moodConfidence: Double = 0.0,
    val grimaceTotal: Int = 0,        // raw model total
    val painScore: Int = 0,           // grimaceTotal + pain offset, used for the alert
    val grimace: Map<String, Int> = emptyMap(),
    val signs: List<String> = emptyList(),
    val careNote: String = "",
    val deepUrgency: String? = null,
    val deepSummary: String? = null,
    val brains: Map<String, String> = emptyMap(), // step -> ON_DEVICE / CLOUD
    val createdAt: Long = System.currentTimeMillis(),
)

/** Full in-memory result for the result screen. */
data class Analysis(
    val quick: QuickRead,
    val matchedCat: Cat?,
    val careNote: String,
    val deep: DeepCheck?,
    val trace: List<TraceStep>,
    val alertThreshold: Int,
    val painOffset: Int,
) {
    /** The Deep Check's scores win when it ran. */
    val grimace: Grimace get() = deep?.fgs ?: quick.fgs
    val painScore: Int get() = grimace.painScore(painOffset)
}
