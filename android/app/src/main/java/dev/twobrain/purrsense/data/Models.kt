package dev.twobrain.purrsense.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Where a step of the pipeline actually ran. Shown to the user as a badge. */
enum class Brain { ON_DEVICE, CLOUD, RULES }

/** One pipeline step result, for the "TwoBrain trace" shown under every reading. */
data class TraceStep(val step: String, val brain: Brain, val millis: Long)

/** A registered cat. `appearance` is a short text signature used for identification. */
data class Cat(
    val id: String = "",
    val name: String = "",
    val appearance: String = "",
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
    val grimaceTotal: Int = 0,
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
)
