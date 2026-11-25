package dev.twobrain.purrsense.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

/** Values tunable from Firebase Remote Config without shipping a new build. */
data class AppConfig(
    val cloudQuickModel: String,
    val cloudDeepModel: String,
    val grimaceThreshold: Int,
)

object RemoteConfigService {
    private val defaults = mapOf(
        "cloud_quick_model" to "gemini-3.5-flash-lite",
        "cloud_deep_model" to "gemini-3.8-flash",
        "grimace_alert_threshold" to 4L, // published FGS analgesia threshold: 4/10
    )

    suspend fun load(): AppConfig {
        val rc = Firebase.remoteConfig
        runCatching {
            rc.setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 600 }).await()
            rc.setDefaultsAsync(defaults).await()
            rc.fetchAndActivate().await()
        } // offline: defaults / last activated values are used
        return AppConfig(
            cloudQuickModel = rc.getString("cloud_quick_model").ifBlank { "gemini-3.5-flash-lite" },
            cloudDeepModel = rc.getString("cloud_deep_model").ifBlank { "gemini-3.8-flash" },
            grimaceThreshold = rc.getLong("grimace_alert_threshold").toInt().takeIf { it in 1..10 } ?: 4,
        )
    }
}

/**
 * Firestore layout (see /firebase/firestore.rules):
 *   users/{uid}/cats/{catId}
 *   users/{uid}/cats/{catId}/readings/{readingId}   (catId "unknown" for unmatched cats)
 * Photos are never uploaded; only the structured results are stored.
 */
class Repository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun ensureSignedIn(): String =
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user!!.uid

    private fun cats(uid: String) = db.collection("users").document(uid).collection("cats")

    suspend fun listCats(uid: String): List<Cat> =
        cats(uid).orderBy("createdAt").get().await().documents.map { d ->
            Cat(
                id = d.id,
                name = d.getString("name").orEmpty(),
                appearance = d.getString("appearance").orEmpty(),
                createdAt = d.getLong("createdAt") ?: 0L,
            )
        }

    suspend fun addCat(uid: String, name: String, appearance: String): Cat {
        val doc = cats(uid).document()
        val cat = Cat(id = doc.id, name = name, appearance = appearance)
        // Not awaited: Firestore applies writes to its local cache immediately and syncs when
        // online, so registering a cat and saving readings also work offline.
        doc.set(mapOf("name" to name, "appearance" to appearance, "createdAt" to cat.createdAt))
        return cat
    }

    fun saveReading(uid: String, r: Reading) {
        cats(uid).document(r.catId).collection("readings").add(
            mapOf(
                "mood" to r.mood,
                "moodConfidence" to r.moodConfidence,
                "grimaceTotal" to r.grimaceTotal,
                "grimace" to r.grimace,
                "signs" to r.signs,
                "careNote" to r.careNote,
                "deepUrgency" to r.deepUrgency,
                "deepSummary" to r.deepSummary,
                "brains" to r.brains,
                "createdAt" to r.createdAt,
            )
        ) // not awaited, see addCat
    }

    suspend fun recentReadings(uid: String, catId: String, limit: Long = 14): List<Reading> =
        cats(uid).document(catId).collection("readings")
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
            .get().await().documents.map { d ->
                @Suppress("UNCHECKED_CAST")
                Reading(
                    id = d.id,
                    catId = catId,
                    mood = d.getString("mood").orEmpty(),
                    moodConfidence = d.getDouble("moodConfidence") ?: 0.0,
                    grimaceTotal = (d.getLong("grimaceTotal") ?: 0L).toInt(),
                    signs = (d.get("signs") as? List<String>).orEmpty(),
                    careNote = d.getString("careNote").orEmpty(),
                    deepUrgency = d.getString("deepUrgency"),
                    deepSummary = d.getString("deepSummary"),
                    brains = (d.get("brains") as? Map<String, String>).orEmpty(),
                    createdAt = d.getLong("createdAt") ?: 0L,
                )
            }
}
