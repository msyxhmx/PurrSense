@file:OptIn(PublicPreviewAPI::class)

package dev.twobrain.purrsense.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.ondevice.FirebaseAIOnDevice
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import dev.twobrain.purrsense.data.AppConfig
import dev.twobrain.purrsense.data.Brain
import dev.twobrain.purrsense.data.Cat
import dev.twobrain.purrsense.data.DeepCheck
import dev.twobrain.purrsense.data.IdMatch
import dev.twobrain.purrsense.data.Mood
import dev.twobrain.purrsense.data.QuickRead
import dev.twobrain.purrsense.data.TraceStep
import dev.twobrain.purrsense.data.needsDeepCheck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The two brains of PurrSense. ALL Firebase AI Logic calls live in this file.
 *
 * - [hybrid]: Firebase AI Logic hybrid model, PREFER_ON_DEVICE. Runs on Gemini Nano (via ML Kit /
 *   AICore) when the device has it, otherwise transparently falls back to Gemini in the cloud.
 *   Used for: quick read (1 image + text), cat identification, care note, weekly trend.
 * - [deep]: cloud-only Gemini, used for the Deep Check escalation when the grimace score crosses
 *   the Remote Config threshold or visible health signs are reported.
 */
class CatBrain(private val config: AppConfig) {

    private val ai = Firebase.ai(backend = GenerativeBackend.googleAI())

    private val hybrid: GenerativeModel = ai.generativeModel(
        modelName = config.cloudQuickModel,
        generationConfig = generationConfig { temperature = 0.2f },
        onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE, maxOutputTokens = 256),
    )

    /** Used only if the on-device answer to the quick read cannot be parsed. */
    private val cloudQuick: GenerativeModel = ai.generativeModel(
        modelName = config.cloudQuickModel,
        generationConfig = generationConfig {
            temperature = 0.2f
            responseMimeType = "application/json"
        },
    )

    private val deep: GenerativeModel = ai.generativeModel(
        modelName = config.cloudDeepModel,
        generationConfig = generationConfig {
            temperature = 0.2f
            responseMimeType = "application/json"
        },
    )

    // ---------------------------------------------------------------- on-device model lifecycle

    /** Human-readable on-device status: UNAVAILABLE / DOWNLOADABLE / DOWNLOADING / AVAILABLE. */
    suspend fun onDeviceStatus(): String = runCatching {
        FirebaseAIOnDevice.checkStatus().toString()
    }.getOrElse { "UNAVAILABLE" }

    /** The SDK never auto-downloads Gemini Nano; the user can trigger it from the home screen. */
    fun downloadOnDeviceModel(): Flow<String> = FirebaseAIOnDevice.download().map { it.toString() }

    // ---------------------------------------------------------------- pipeline steps

    /** Step 1 "See": mood + Feline Grimace Scale from one photo. On-device first. */
    suspend fun quickRead(photo: Bitmap, trace: MutableList<TraceStep>): QuickRead {
        val small = photo.scaledTo(768)
        val prompt = content {
            image(small) // on-device supports exactly one in-memory Bitmap
            text(Prompts.QUICK_READ)
        }
        val (response, ms) = timed { hybrid.generateContent(prompt) }
        val brain = response.brain()
        trace += TraceStep("See (mood + grimace)", brain, ms)

        parse<QuickRead>(response.text)?.let { return it.normalized() }

        // Small on-device models occasionally break the JSON format: retry once in the cloud.
        Log.w(TAG, "Quick read unparseable from $brain, retrying in cloud: ${response.text}")
        val (retry, ms2) = timed { cloudQuick.generateContent(prompt) }
        trace += TraceStep("See (cloud retry)", Brain.CLOUD, ms2)
        return parse<QuickRead>(retry.text)?.normalized()
            ?: throw UnreadableAnswerException()
    }

    private fun QuickRead.normalized() = copy(mood = Mood.normalize(mood))

    /**
     * Step 2 "Who". Photo matching first: compare the photo's on-device embedding with each
     * enrolled cat's, and accept the best only above `cat_id_min_similarity`. The research found
     * text descriptions identify cats about 54% of the time versus 85-89% for image embeddings.
     * Cats registered before photo matching have no embedding and fall back to text matching.
     */
    suspend fun identify(
        embedding: List<Float>?,
        observed: String,
        cats: List<Cat>,
        trace: MutableList<TraceStep>,
    ): Cat? {
        if (cats.isEmpty()) return null
        val enrolled = cats.filter { it.embedding.isNotEmpty() }
        if (embedding != null && enrolled.isNotEmpty()) {
            val (best, score) = enrolled.map { it to CatEmbedder.cosine(embedding, it.embedding) }
                .maxBy { it.second }
            trace += TraceStep("Who (photo match ${"%.2f".format(score)})", Brain.ON_DEVICE, 0)
            if (score >= config.catIdMinSimilarity) return best
            // Every cat has an embedding and none matched well enough: this cat isn't registered.
            if (enrolled.size == cats.size) return null
        }
        val legacy = cats.filter { it.embedding.isEmpty() }
        if (legacy.isEmpty() || observed.isBlank()) return null
        if (cats.size == 1) {
            trace += TraceStep("Who (single cat)", Brain.RULES, 0)
            return cats.first()
        }
        val (response, ms) = timed { hybrid.generateContent(Prompts.identify(observed, legacy)) }
        trace += TraceStep("Who (identify)", response.brain(), ms)
        val match = parse<IdMatch>(response.text) ?: return null
        return legacy.firstOrNull { it.id == match.catId && match.confidence >= 0.5 }
    }

    /** Step 3 "Care": owner-friendly explanation. Text-only, on-device first. */
    suspend fun careNote(name: String, read: QuickRead, trace: MutableList<TraceStep>): String {
        val (response, ms) = timed {
            hybrid.generateContent(Prompts.careNote(name, read, config.grimaceThreshold, config.painOffset))
        }
        trace += TraceStep("Care note", response.brain(), ms)
        return response.text?.trim().orEmpty()
    }

    /**
     * Rule-based escalation decision: no model involved, fully explainable. Uses the corrected
     * pain score (raw total + Remote Config offset), because Gemini under-scores FGS pain.
     */
    fun needsDeepCheck(read: QuickRead): Boolean =
        needsDeepCheck(read, config.grimaceThreshold, config.painOffset)

    /** Step 4 "Deep Check": larger cloud model, higher-resolution image. */
    suspend fun deepCheck(photo: Bitmap, trace: MutableList<TraceStep>): DeepCheck? {
        val prompt = content {
            image(photo.scaledTo(1280))
            text(Prompts.DEEP_CHECK)
        }
        val (response, ms) = timed { deep.generateContent(prompt) }
        trace += TraceStep("Deep Check", Brain.CLOUD, ms)
        return parse<DeepCheck>(response.text)
    }

    /** Weekly trend summary from stored readings (text only, on-device first, works offline). */
    suspend fun trend(name: String, lines: List<String>): Pair<String, Brain> {
        val response = hybrid.generateContent(Prompts.trend(name, lines, config.grimaceThreshold))
        return response.text?.trim().orEmpty() to response.brain()
    }

    // ---------------------------------------------------------------- helpers

    private fun GenerateContentResponse.brain(): Brain =
        // inferenceSource is ON_DEVICE or IN_CLOUD for hybrid models.
        if (inferenceSource.toString().contains("DEVICE")) Brain.ON_DEVICE else Brain.CLOUD

    private inline fun <reified T> parse(text: String?): T? =
        ModelJson.decode<T>(text).also { if (it == null) Log.w(TAG, "Could not read model JSON: $text") }

    private inline fun <T> timed(block: () -> T): Pair<T, Long> {
        val t0 = System.currentTimeMillis()
        val r = block()
        return r to (System.currentTimeMillis() - t0)
    }

    private fun Bitmap.scaledTo(maxSide: Int): Bitmap {
        val longest = maxOf(width, height)
        if (longest <= maxSide) return this
        val s = maxSide.toFloat() / longest
        return Bitmap.createScaledBitmap(this, (width * s).toInt(), (height * s).toInt(), true)
    }

    companion object { private const val TAG = "CatBrain" }
}

/** Neither the on-device nor the cloud answer could be read as JSON. */
class UnreadableAnswerException :
    Exception("The AI's answer couldn't be read. Please try the photo again.")
