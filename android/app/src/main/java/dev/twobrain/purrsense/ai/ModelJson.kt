package dev.twobrain.purrsense.ai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlin.math.roundToInt

/**
 * Reads the JSON a model writes, which is often slightly off-format: wrapped in prose or code
 * fences, with trailing commas, scores written as 1.0 or "1", or a single sign as a string.
 * Pure Kotlin (no Android classes) so it can be unit-tested.
 */
object ModelJson {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        allowTrailingComma = true
        allowComments = true
    }

    /** Decodes the first JSON object in [text], or null if there is none or it can't be read. */
    inline fun <reified T> decode(text: String?): T? {
        val raw = extractObject(text) ?: return null
        return runCatching {
            val element = json.parseToJsonElement(raw)
            json.decodeFromJsonElement<T>(tidy(element))
        }.getOrNull()
    }

    /**
     * The first balanced `{...}` in [text], ignoring braces inside strings. Taking the first `{`
     * to the last `}` breaks when the model adds a note with braces after the object.
     */
    fun extractObject(text: String?): String? {
        if (text.isNullOrBlank()) return null
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val c = text[i]
            if (inString) {
                when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '"' -> inString = false
                }
                continue
            }
            when (c) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> if (--depth == 0) return text.substring(start, i + 1)
            }
        }
        return null
    }

    private val SCORE_KEYS = setOf("ears", "eyes", "muzzle", "whiskers", "head")
    private val FLAG_KEYS = setOf("cat", "face_visible")
    private val LIST_KEYS = setOf("signs", "findings")
    private val NONE_WORDS = setOf("", "none", "no", "n/a", "na", "null", "[]")

    /** Coerces the fields models most often get wrong into the types the app expects. */
    fun tidy(element: JsonElement, key: String? = null): JsonElement = when {
        element is JsonObject -> JsonObject(element.mapValues { (k, v) -> tidy(v, k) })
        key in SCORE_KEYS -> score(element)
        key in FLAG_KEYS -> flag(element)
        key in LIST_KEYS -> list(element)
        else -> element
    }

    /** 1.0, "1" and "1.4" all become 1; anything unreadable becomes 0. */
    private fun score(e: JsonElement): JsonElement {
        val p = e as? JsonPrimitive ?: return JsonPrimitive(0)
        val n = p.doubleOrNull ?: p.content.trim().toDoubleOrNull() ?: return JsonPrimitive(0)
        return JsonPrimitive(n.roundToInt())
    }

    /** "yes", "true" and 1 become true. */
    private fun flag(e: JsonElement): JsonElement {
        val p = e as? JsonPrimitive ?: return JsonPrimitive(false)
        p.booleanOrNull?.let { return JsonPrimitive(it) }
        val s = p.content.trim().lowercase()
        return JsonPrimitive(s == "yes" || s == "y" || s == "1" || s == "true")
    }

    /** A single string becomes a one-item list; "none" or null becomes an empty list. */
    private fun list(e: JsonElement): JsonElement = when (e) {
        is JsonArray -> JsonArray(e.filter { it is JsonPrimitive && it.content.trim().lowercase() !in NONE_WORDS })
        JsonNull -> JsonArray(emptyList())
        is JsonPrimitive ->
            if (e.content.trim().lowercase() in NONE_WORDS) JsonArray(emptyList()) else JsonArray(listOf(e))
        else -> JsonArray(emptyList())
    }
}
