package dev.twobrain.purrsense.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.twobrain.purrsense.ai.CatBrain
import dev.twobrain.purrsense.ai.CatEmbedder
import dev.twobrain.purrsense.data.Analysis
import dev.twobrain.purrsense.data.AppConfig
import dev.twobrain.purrsense.data.Brain
import dev.twobrain.purrsense.data.Cat
import dev.twobrain.purrsense.data.DeepCheck
import dev.twobrain.purrsense.data.Reading
import dev.twobrain.purrsense.data.RemoteConfigService
import dev.twobrain.purrsense.data.Repository
import dev.twobrain.purrsense.data.TraceStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface Screen {
    data object Home : Screen
    data class Result(val photo: Bitmap, val analysis: Analysis) : Screen
    data class AddCat(val photo: Bitmap, val suggestedAppearance: String) : Screen
    data class CatDetail(val cat: Cat) : Screen
}

data class UiState(
    val ready: Boolean = false,
    val busy: String? = null,             // non-null = progress message
    val error: String? = null,
    val onDeviceStatus: String = "checking…",
    val cats: List<Cat> = emptyList(),
    val screen: Screen = Screen.Home,
    val history: List<Reading> = emptyList(),
    val trend: Pair<String, Brain>? = null,
)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository()
    private lateinit var brain: CatBrain
    /** Null if the embedding model failed to load: identification then falls back to text. */
    private var embedder: CatEmbedder? = null
    /** Embedding of the photo on the "add cat" screen, saved with the new cat. */
    private var pendingEmbedding: List<Float> = emptyList()
    private lateinit var config: AppConfig
    private var uid: String = ""

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        viewModelScope.launch {
            runCatching {
                config = RemoteConfigService.load()
                brain = CatBrain(config)
                embedder = runCatching { CatEmbedder(getApplication()) }
                    .onFailure { Log.w("MainViewModel", "Embedder unavailable: ${it.message}") }
                    .getOrNull()
                uid = repo.ensureSignedIn()
                val cats = repo.listCats(uid)
                _state.update { it.copy(ready = true, cats = cats, onDeviceStatus = brain.onDeviceStatus()) }
            }.onFailure { e -> _state.update { it.copy(error = "Startup failed: ${e.message}") } }
        }
    }

    fun downloadModel() = viewModelScope.launch {
        runCatching {
            brain.downloadOnDeviceModel().collect { s -> _state.update { it.copy(onDeviceStatus = s) } }
            _state.update { it.copy(onDeviceStatus = brain.onDeviceStatus()) }
        }.onFailure { e -> _state.update { it.copy(error = "Download failed: ${e.message}") } }
    }

    /** Full pipeline: See -> Who -> Care -> (rules) -> Deep Check -> save. */
    fun analyze(photo: Bitmap) = viewModelScope.launch {
        val trace = mutableListOf<TraceStep>()
        runCatching {
            busy("Reading your cat's face…")
            val quick = brain.quickRead(photo, trace)
            if (!quick.cat || !quick.faceVisible) {
                _state.update { it.copy(busy = null, error = "No cat face found. Try a clear, front-facing photo.") }
                return@launch
            }

            busy("Who's this?")
            val t0 = System.currentTimeMillis()
            val embedding = embed(photo)
            if (embedding != null) trace += TraceStep("Photo embedding", Brain.ON_DEVICE, System.currentTimeMillis() - t0)
            val cat = brain.identify(embedding, quick.appearance, _state.value.cats, trace)

            busy("Writing a note…")
            val note = brain.careNote(cat?.name ?: "your cat", quick, trace)

            var deep: DeepCheck? = null
            if (brain.needsDeepCheck(quick)) {
                trace += TraceStep("Escalate? pain ${quick.fgs.painScore(config.painOffset)} ≥ ${config.grimaceThreshold} or signs", Brain.RULES, 0)
                busy("Score is elevated: asking the cloud for a Deep Check…")
                deep = runCatching { brain.deepCheck(photo, trace) }.getOrNull() // offline: skip gracefully
            }

            val analysis = Analysis(quick, cat, note, deep, trace, config.grimaceThreshold, config.painOffset)
            repo.saveReading(uid, analysis.toReading())
            _state.update { it.copy(busy = null, screen = Screen.Result(photo, analysis)) }
        }.onFailure { e -> _state.update { it.copy(busy = null, error = "Analysis failed: ${e.message}") } }
    }

    /** Register a new cat from a photo: the quick read proposes an appearance signature. */
    fun startAddCat(photo: Bitmap) = viewModelScope.launch {
        runCatching {
            busy("Learning what this cat looks like…")
            val quick = brain.quickRead(photo, mutableListOf())
            if (!quick.cat) error("No cat found in that photo.")
            pendingEmbedding = embed(photo).orEmpty()
            _state.update { it.copy(busy = null, screen = Screen.AddCat(photo, quick.appearance)) }
        }.onFailure { e -> _state.update { it.copy(busy = null, error = e.message) } }
    }

    fun saveCat(name: String, appearance: String) = viewModelScope.launch {
        val cat = repo.addCat(uid, name.trim(), appearance.trim(), pendingEmbedding)
        pendingEmbedding = emptyList()
        _state.update { it.copy(cats = it.cats + cat, screen = Screen.Home) }
    }

    fun openCat(cat: Cat) = viewModelScope.launch {
        _state.update { it.copy(screen = Screen.CatDetail(cat), history = emptyList(), trend = null) }
        runCatching {
            val history = repo.recentReadings(uid, cat.id)
            _state.update { it.copy(history = history) }
            if (history.isNotEmpty()) {
                val fmt = SimpleDateFormat("EEE d MMM", Locale.US)
                val lines = history.take(10).map {
                    "${fmt.format(Date(it.createdAt))}: mood ${it.mood}, pain ${it.painScore}/10" +
                        if (it.signs.isNotEmpty()) ", signs: ${it.signs.joinToString()}" else ""
                }
                _state.update { it.copy(trend = brain.trend(cat.name, lines)) }
            }
        }.onFailure { e -> _state.update { it.copy(error = "Could not load history: ${e.message}") } }
    }

    fun goHome() = _state.update { it.copy(screen = Screen.Home) }
    fun dismissError() = _state.update { it.copy(error = null) }
    private fun busy(msg: String) = _state.update { it.copy(busy = msg, error = null) }

    /** On-device photo embedding off the main thread; null if the model isn't available. */
    private suspend fun embed(photo: Bitmap): List<Float>? = withContext(Dispatchers.Default) {
        embedder?.let { e -> runCatching { e.embed(photo) }.getOrNull() }
    }

    private fun Analysis.toReading() = Reading(
        catId = matchedCat?.id ?: "unknown",
        mood = quick.mood,
        moodConfidence = quick.moodConfidence,
        grimaceTotal = grimace.total,
        painScore = painScore,
        grimace = grimace.let {
            mapOf("ears" to it.ears, "eyes" to it.eyes, "muzzle" to it.muzzle, "whiskers" to it.whiskers, "head" to it.head)
        },
        signs = quick.signs,
        careNote = careNote,
        deepUrgency = deep?.urgency,
        deepSummary = deep?.summary,
        brains = trace.associate { it.step to it.brain.name },
    )
}
